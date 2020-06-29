package com.youssefdirani.advertise_admin;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.lang.UProperty;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.accessibility.AccessibilityRecord;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_STRING;

public class ConnectToServer {
    private MainActivity activity;
    private DbOperations dbOperations; //for diagnosis
    private SQLiteDatabase db2;
    Update update; //update.isAlive()

    ConnectToServer( MainActivity activity, DbOperations dbOperations ) {
        this.activity = activity;
        this.dbOperations = dbOperations;
        update = new Update();
    }

    void setup() { /*hopefully we make ANOTHER CONNECTION to the database (so we can make use of WAL and the principle of
    * isolation in SQLite), this is
    * because I communicate to the server and might download data from the server db, and when I begin a transaction in db2
    * and make changes to the database (which are not yet committed), the old db connection can still work on its own pace
    * and make usual changes which are reflected in UI, then when all db and UI work is idle, we freeze the UI and update the
    * database using db2, then unfreeze the UI.
    * Does it have to be idle for db2 to commit ? No, directly db2 freezes UI and commits. Of course, I have to watch the
    * checked items if they are compatible or not, but this is fine.
    */
        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteOpenHelper(activity, "my_db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db2) { //not called
                Log.i("new_channel", "on create db2");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db2, int oldVersion, int newVersion) { //no called
                Log.i("new_channel", "on upgrade db2");
            }
        };
        db2 = sqLiteOpenHelper.getWritableDatabase(); //Log.i("new_channel", "path for db2 is " + db2.getPath()); //the same path as for db.getPath()
        db2.enableWriteAheadLogging(); //had to be called even though it was called for db.
    }

    void start() {
        if( update.isFree() ) {
            update.start();
        }
    }

    private void listAllTables() { //for diagnosis
        Cursor cursor = db2.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if( cursor.getCount() > 0 ) {
            Log.i("listall", "from db2, fetching tables.");
            while( cursor.moveToNext() ) {
                Log.i("listall", "table name is " +
                        cursor.getString( 0 ) );
            }
            cursor.close();
        } else {
            Log.i("listall", "from db2, no table could had been able to be fetched.");
        }
        dbOperations.listAllTables();
    }

    class Update extends Thread {
        volatile Socket client;
        PrintWriter printWriter;
        BufferedReader bufferedReader;
        //boolean busy = false;

        Update() {

        }

        boolean isFree() { //1 connection at a time
            return ( client == null ||
                    ( client.isClosed() && !client.isConnected() && client.isInputShutdown() && client.isOutputShutdown() ) );
        }

        @Override
        public void run() { //in this thread we make necessary changes to the db, which is good to WAL, since in WAL we probably need another thread.
            try {
                createSocket();
                printDate();
                processReader();
                closeAll();
                listAllTables();
            } catch (Exception e) {//unexpectedly if couldn't connect to socket, but expectedly when the buffered reader is closed.
                e.printStackTrace();
                Log.e("Update", "exception.", e);
            }

        }

        private void closeAll() throws Exception{
            printWriter.close();
            bufferedReader.close();
            client.close();
        }

        private void printDate() {
            Cursor cursor = db2.rawQuery("SELECT 'last_update' FROM 'db_last_update'",null);
            if( cursor != null ) {
                if( cursor.moveToNext() ) {
                    long last_update = cursor.getLong(0);
                    String message = "last_update:" + last_update;
                    printWriter.print(message); //I think it'll automatically add a null char. //out.write(buffer, 0, count);
                }
                cursor.close();
            }
        }

        private final String Updating_str = "Updating:"; /*After  the colon ':' comes the tables with their whole values.*/
        private void processReader() {
            int count; //https://stackoverflow.com/questions/5113914/large-file-transfer-with-sockets?rq=1
            char[] buffer = new char[8192];
            String s = "", message = "", action = "";
            /*The following strings are like headers the server will send to the app.*/
            final String InfoRequest_str = "R?table_last_update", /*This will be preceded by the app sending its table_last_update
                    * to the server, then the server will know which tables to send to the app, and will actually send them
                    * according to the following string constant.
                    */
                    InfoTableReply_str = "table_last_update:"; /*same meaning as "R?update:"
                     * After the colon ':' comes the table itself.
                     * This is the case where server's last_update date is older than the app's. Here the server
                     * sends its table_last_update table so that the app updates the server.
                     */
                    /*
                    Done_str = "Done";/*This is similar to not receiving anything from the server and the latter
                    * closing the connection.*/
            try {
                while( ( count = bufferedReader.read(buffer) ) > 0 ) {//this waits 2500 ms then throws a java.net.SocketTimeoutException
                    /* We enter in this loop for 2 reasons, either for a new incoming message, or for the same
                     * message that was lengthy (like longer than buffer).
                     * *********
                     * Communication here is like a handshake that ends when info transceiving is over.
                     * The server first decides which of the 2 will update the other. The update is then made automatically.
                     * ((It is worthwhile to note : automatic update assumes that there is only 1 admin. Having 2 admins
                     * requires an advanced mechanism.))
                     * If it is up to the server to update the
                     * app, it asks the app to send its update_table to the server, so the server examines the dates and sends the
                     * new tables.
                     * If it is up to the app to update the server, the server replies back with its update_table, then the
                     * app examines the dates and sends the new tables.
                     * ***********
                     * * After that communication is done, the server (or the app) closes the connection,
                     * so we will usually get an exception in read(). I believe it is better for the receiver
                     * to close the connection instead of the sender, in order to avoid corruption of data, or some data not
                     * being sent at all.
                     * */
                    s = String.valueOf( buffer, 0, count );
                    if( message.equals("") && action.equals("") && s.equalsIgnoreCase( InfoRequest_str ) ) { //message.equals("") is because : although it is rare and probably impossible that this conflicts with an user entry but to be more safe.
                        send__table_last_update();
                        //action won't be set to anything, because another loop in this while is for another message, not the same.
                    } else if( message.equals("") && action.equals("") &&
                            s.substring( 0, Updating_str.length() ).equalsIgnoreCase( Updating_str ) ) {//.substring() won't throw IndexOutOfBoundsException since the following condition isn't valid "- "if beginIndex is negative or larger than the length of this String object."
                        action = Updating_str;
                        message = s.substring( Updating_str.length() );
                    } else if( message.equals("") && action.equals("") &&
                            s.substring( 0, InfoTableReply_str.length() ).equalsIgnoreCase( InfoTableReply_str ) ) {
                        action = InfoTableReply_str;
                        message = s.substring( InfoTableReply_str.length() );
                        /*
                    } else if( message.equals("") && action.equals("") &&
                            s.substring( 0, Done_str.length() ).equalsIgnoreCase( Done_str ) ) {
                        //action = InfoTableReply_str;//not so needed
                        break;
                         */
                    } else {
                        message = message + s;
                    }
                }
            } catch( Exception e ) {

            }
            //here we shall process the gotten message
            if( action.equals( Updating_str ) ) {
                updateAppDb( message );
            } else if( action.equals( InfoTableReply_str ) ) {
                analyzeServerInfoTable_AndUpdateServer( message );
            }

        }

        private final String Delete_Str = "tobedeleted";
        private void updateAppDb( String dbTables_str ) {
            if( dbTables_str.equals("") ) {
                return;
            }
            db2.beginTransactionNonExclusive();
            try {
                int lastIndex = dbTables_str.indexOf(Trailor);
                while( lastIndex > 0 ) {
                    String usefulTableInfo = dbTables_str.substring(0, lastIndex);
                    updateTable(usefulTableInfo);
                    dbTables_str = dbTables_str.substring(lastIndex);
                    lastIndex = dbTables_str.indexOf(Trailor);
                }

                //activity.freezeUI(); //Now disabling (freezing) the UI before committing. Cancelled since committing can be fast enough. I'm ignoring the critical race.
                db2.setTransactionSuccessful(); //committing
                //Now enabling the UI once again //cancelled as probably not needed.

            } catch( Exception e ) {
                Log.e("fatal", "I got an error", e);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.toast("Unable to save data internally. Data integrity is not guaranteed.", Toast.LENGTH_LONG);
                    }
                });
            } finally {
                db2.endTransaction();
            }

        }

        private void updateTable( String usefulTableInfo ) {
            //usefulTableInfo is like tableName:JsonString.
            /*We will check if table already exists or not.
            * If it exists, we will just delete the content and (carefully) put the new content instead.
            * If it does not exist, we create it and add (carefully) necessary things to it.
            * */
            if( usefulTableInfo.equals("") ) { //null check is not needed
                return;
            }
            int indexOfColon = usefulTableInfo.indexOf(":");
            if( indexOfColon <= 0 ) {
                return;
            }
            String tableName = usefulTableInfo.substring( 0 , indexOfColon );
            String tableContent = usefulTableInfo.substring( indexOfColon );
            if( tableContent.equals( Delete_Str ) ) {
                db2.execSQL("DROP TABLE IF EXISTS '" + tableName + "';");
                return;
            }

            Gson gson = new Gson();
            JsonArray jArray_table = gson.fromJson( tableContent, JsonArray.class); //JsonArray jArray = gson.fromJson(jsonStr, JsonArray.class);
            if( jArray_table == null || !jArray_table.isJsonArray() ) { //should not happen
                return;
            }
            if( isTableExists( tableName ) ) {
                db2.execSQL( "DELETE FROM '" + tableName + "'" );
                //we won't bother with the columns. We know they're already well set. So we just add the content.
            } else {
                //getting the column names
                db2.execSQL("CREATE TABLE IF NOT EXISTS '" + tableName + "' ( " +
                        "uid INTEGER PRIMARY KEY AUTOINCREMENT);");
                JsonObject jObj = (JsonObject) jArray_table.get( 0 );
                if( jObj == null || !jObj.isJsonObject() ) { //should never happen
                    return;
                }
                for( int i = 0 ; i < jObj.size() ; i++ ) { //i = 0 does not refer to the uid column (because it was already stored in the server this way)
                    JsonElement colName = jObj.get( String.valueOf( i ) );
                    if( colName == null ) { //should not happen
                        return;
                    }
                    String colName_str = colName.toString();
                    String colType = getColumnType( colName_str );
                    db2.execSQL("ALTER TABLE '" + tableName + "' ADD '" + colName_str + "' " + colType + ";");
                }
            }

            //now to add the records
            for( int i = 1 ; i < jArray_table.size() ; i++ ) {
                JsonObject jObj = (JsonObject) jArray_table.get( i );
                if( !jObj.isJsonObject() ) {
                    return;
                }
                Set<String> keys = jObj.keySet();
                ContentValues contentValues = new ContentValues();
                for( String key : keys ) { //for( int j = 0 ; j < jObj.size() ; j++ ) {
                    JsonElement colName = jObj.get( key );
                    if( colName == null ) { //won't happen... It's null when the key-value pair of the String.valueOf( j ) key does not exist. But in our case the key is there.
                        continue;
                    }
                    String colName_str = colName.toString();
                    if( getColumnType( colName_str ).equalsIgnoreCase("INTEGER") ) {
                        contentValues.put( key, Long.parseLong( colName_str ));
                    } else {
                        contentValues.put( key, colName_str );
                    }
                    //contentValues.put( "uid", 0); //won't happen for the uid
                }
                db2.insert( tableName, null, contentValues );
            }

        }

        private String getColumnType( String colName_str ) {
            if( colName_str.equals("index1") //actually this is the only possible case
                    || colName_str.equals("last_update") || colName_str.equals("statusbar_dark") ) {
                return  "INTEGER";
            } else {
                return  "TEXT";
            }
        }

        private boolean isTableExists( String tableName ) {
            String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
            try {
                Cursor cursor = db2.rawQuery( query, null );
                if(cursor!=null) {
                    cursor.close();
                    return cursor.getCount() > 0;
                }
                return false;
            } catch(Exception e) {
                Log.i("isTableExists", "Table " + tableName + " does not exist.");
                return false;
            }
        }

        private final String Trailor = "/`~<';"; //a separator between table strings
        private void analyzeServerInfoTable_AndUpdateServer( final String tableLastUpdate_str ) { //app is newer than server
            /*We compare our table_last_update with that of the server. We compare the date of each table name stated in our
            * table_last_update.*/
            if( tableLastUpdate_str == null ) {
                return;
            }
            int endIndex = tableLastUpdate_str.indexOf( Trailor );
            if( endIndex <= 0 ) {
                return;
            }
            final String tableLastUpdate_OfServer_UsefulStr = tableLastUpdate_str.substring( 0 , endIndex );
            Gson gson = new Gson();
            JsonArray jArray_tableLastUpdate_OfServer = gson.fromJson( tableLastUpdate_OfServer_UsefulStr, JsonArray.class); //JsonArray jArray = gson.fromJson(jsonStr, JsonArray.class);
            if( jArray_tableLastUpdate_OfServer != null && jArray_tableLastUpdate_OfServer.isJsonArray()
                    && jArray_tableLastUpdate_OfServer.size() != 0 ) {
                String message = Updating_str; //building our message to send
                final String tableName = "table_last_update";
                Cursor cursor = db2.rawQuery("SELECT * FROM '" + tableName + "'",null );
                if( cursor != null ) {
                    while( cursor.moveToNext() ) {
                        String tableName_record =  cursor.getString( cursor.getColumnIndex("table_name") );
                        long lastUpdate_record = cursor.getLong( cursor.getColumnIndex("last_update") );
                        int compareResult = compareRecords_appStarting( tableName_record, lastUpdate_record,
                                jArray_tableLastUpdate_OfServer ); //return value is a convention
                        switch( compareResult ) {
                            case -2 : //not found in server's table_last_update. Possible, where the admin has recently created the table.
                            case 1 : //found, and needs to be updated. The app's is more recent than the server's
                                convertTableToJsonArrayString( tableName_record , message );
                                break;
                            case 0 : //found and equal dates (do nothing)
                            case -1 : //found. The server's is more recent than the app's (this shouldn't happen in our version where it is assumed only one admin modifies the server).
                            default:
                        }
                    }
                    cursor.moveToFirst();
                    compareRecords_serverStarting( cursor, jArray_tableLastUpdate_OfServer, message );
                    cursor.close();
                }
                if( message.equals( Updating_str ) ) { //meaning nothing was added to our message,so there's really nothing to update. But this should not happen since the last_update field of db_last_update has indicated that the app is newer than the server
                    return;
                }
                convertTableToJsonArrayString( "table_last_update", message ); //it is sort of a coincidence that the last table is table_last_update
                printWriter.print( message );
            }
        }

        private void compareRecords_serverStarting( Cursor cursor, JsonArray jArray_tableLastUpdate_OfServer , String message ) {
            //just to name (thus add to message) which tables are to be deleted in the server
            for( int i = 1 ; i < jArray_tableLastUpdate_OfServer.size() ; i++ ) { //i starts at 1 because we don't care about the column names.
                JsonObject jObj = (JsonObject) jArray_tableLastUpdate_OfServer.get( i );
                if( jObj == null || !jObj.isJsonObject() ) {//should never happen
                    continue;
                }
                JsonElement tableName_serverRecord = jObj.get("table_name");
                if( tableName_serverRecord == null ) { //should not happen
                    continue;
                }
                String tableName_serverRecord_str = tableName_serverRecord.toString();
                boolean isFound = false;
                while( cursor.moveToNext() ) {
                    String tableName_appRecord =  cursor.getString( cursor.getColumnIndex("table_name") );
                    if( tableName_serverRecord_str.equalsIgnoreCase( tableName_appRecord ) ) {
                        isFound = true;
                        //continue;
                    }
                }
                if( !isFound ) { //we then add the table name to the message and mark it to be deleted
                    message = message + tableName_serverRecord_str + ":" + Delete_Str + Trailor;
                }
            }
        }

        private int compareRecords_appStarting( String tableName_record, long lastUpdate_record,
                                               JsonArray jArray_tableLastUpdate_OfServer ) {
            /*This method compares the date of the record in the app's table_last_update with the one found in the
            * server's. Description is found in the usage.*/
            for( int i = 1 ; i < jArray_tableLastUpdate_OfServer.size() ; i++ ) { //i starts at 1 because we don't care about the column names.
                JsonObject jObj = (JsonObject) jArray_tableLastUpdate_OfServer.get(i);
                if( jObj == null || !jObj.isJsonObject() ) { //should never happen
                    continue;
                }
                JsonElement tableName_serverRecord = jObj.get("table_name");
                if (tableName_serverRecord == null) { //should not happen
                    continue;
                }
                String tableName_serverRecord_str = tableName_serverRecord.toString();
                if( tableName_serverRecord_str.equalsIgnoreCase( tableName_record ) ) {
                    JsonElement date_serverRecord = jObj.get("last_update");
                    if( date_serverRecord == null ) { //should not happen
                        return 1; //instructing the caller to update. As if the app's is more recent than the server's.
                    }
                    long date_serverRecord_long = Long.parseLong( tableName_serverRecord.toString() );
                    if( date_serverRecord_long <= 0 ) { //should not happen
                        return 1; //instructing the caller to update. As if the app's is more recent than the server's.
                    }
                    long dateDifference = lastUpdate_record - date_serverRecord_long;
                    if( dateDifference > 0 ) {
                        return 1;
                    } else if( dateDifference < 0 ) {
                        return -1;
                    } else { //if( dateDifference == 0 )
                        return 0;
                    }
                }
            }
            return -2; //not found
        }

        private void send__table_last_update() { //which is send_AppInfoTable.
            /*This method is for when the mobile app is outdated.*/
            String message = "";
            //now the table
            convertTableToJsonArrayString( "table_last_update", message );
            printWriter.print( message );
        }

        private void convertTableToJsonArrayString( final String tableName, String s ) { //the value of s, when changed here, is important to the caller
            s = s + tableName + ":";

            Cursor cursor = db2.rawQuery("SELECT * FROM '" + tableName + "'",null);
            if( cursor != null ) {
                int columnsCount = cursor.getColumnCount();
                JsonObject jObj = new JsonObject();
                JsonArray jArray = new JsonArray();
                //adding the column names as the first row.
                for( int i = 1 ; i < columnsCount ; i++ ) { //i = 1 because I don't want to send the uid
                    jObj.addProperty( String.valueOf( i ) , cursor.getColumnName( i ) );
                }
                jArray.add( jObj );

                //adding the records
                while( cursor.moveToNext() ) {
                    for( int i = 1 ; i < columnsCount ; i++ ) { //i = 1 because I don't want to send the uid values
                        jObj = new JsonObject();
                        String value_str;
                        //Not sure, how persistent the following will behave, but it is worth a shot. I think it's not needed.
                        if( cursor.getType( i ) == FIELD_TYPE_INTEGER ) { //https://developer.android.com/reference/android/database/AbstractWindowedCursor.html#getType(int)
                            value_str = String.valueOf( cursor.getLong( i ) );
                        } else {
                            value_str = cursor.getString( i );
                        }
                        if( value_str != null ) {
                            jObj.addProperty( cursor.getColumnName(i) , value_str ); //String.valueOf( i )
                        }
                        /*
                        else {
                            jObj.addProperty( cursor.getColumnName(i) ,ToBeNulled); //private final String ToBeNulled = "null";
                        }
                         */
                    }
                    if( jObj.size() != 0 ) {
                        jArray.add( jObj );
                    }
                }
                cursor.close();
                Gson gson = new Gson();
                if( jArray.size() != 0 ) {
                    s = s + gson.toJson(jArray);
                }

            }

            s = s + Trailor;
            Log.i("converyTable..", "String now is : " + s );
        }

        private void createSocket() throws Exception {

            client = new Socket();
            //client.connect(new InetSocketAddress(staticIP, port), 1500);
            client.connect(new InetSocketAddress( "192.168.1.21", 11359) );
            client.setSoTimeout(2500);//This timeout is actually for read() //https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html#setSoTimeout(int)
            Log.i("CreateSocket", "client is connected.");
            printWriter = new PrintWriter( client.getOutputStream() );
            Log.i("CreateSocket", "client printwriter is fine.");
            bufferedReader = new BufferedReader(
                    new InputStreamReader( client.getInputStream() ), 256 );
            Log.i("CreateSocket", "client bufferedreader is fine.");
        }

    }
}

