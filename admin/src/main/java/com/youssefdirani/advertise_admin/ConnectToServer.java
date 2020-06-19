package com.youssefdirani.advertise_admin;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.lang.UProperty;
import android.util.Log;
import android.view.accessibility.AccessibilityRecord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.util.List;
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
            } catch (Exception e) {//unexpectedly if couldn't connect to socket, but expectedly when the buffered reader is closed.
                e.printStackTrace();
                Log.e("Update", "exception.", e);
            }

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

        private void processReader() {
            int count; //https://stackoverflow.com/questions/5113914/large-file-transfer-with-sockets?rq=1
            char[] buffer = new char[8192];
            String s = "", message = "", action = "";
            /*The following strings are like headers the server will send to the app.*/
            final String InfoTableReply_str = "table_last_update:", /*same meaning as "R?update:"
                    * After the colon ':' comes the table itself.
                    * This is the case where server's last_update date is older than the app's. Here the server
                    * sends its table_last_update table so that te app updates the server.
                    */
                    InfoRequest_str = "R?table_last_update", /*This will be preceded by the app sending its table_last_update
                    * to the server, then the server will know which tables to send to the app, and will actually send them
                    * according to the following string constant.
                    */
                        Updating_str = "Updating:", /*After  the colon ':' comes the tables with their whole values.*/
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
                    } else if( message.equals("") && action.equals("") &&
                            s.substring( 0, Done_str.length() ).equalsIgnoreCase( Done_str ) ) {
                        //action = InfoTableReply_str;//not so needed
                        break;
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

        private void analyzeServerInfoTable_AndUpdateServer( String dbTables_str ) {

        }

        private void updateAppDb( String dbTables_str ) {

        }

        private void send__table_last_update() { //which is send_AppInfoTable
            String message = "Updating:";
            //now the table
            convertTableToJsonArrayString( "table_last_update", message );

        }

        void convertTableToJsonArrayString( final String tableName, String s ) {
            s = s + tableName + ":";

            Cursor cursor = db2.rawQuery("SELECT * FROM '" + tableName + "'",null);
            if( cursor != null ) {
                int columnsCount = cursor.getColumnCount();
                JsonObject jObj;
                JsonArray jArray = new JsonArray();

                while( cursor.moveToNext() ) {
                    for( int i = 1 ; i < columnsCount ; i++ ) { //i = 1 because I don't want to send the uid
                        jObj = new JsonObject();
                        String value_str;
                        //Not sure, how persistently the following if will behave, but it is worth a shot.
                        if( cursor.getType( i ) == FIELD_TYPE_INTEGER ) { //https://developer.android.com/reference/android/database/AbstractWindowedCursor.html#getType(int)
                            value_str = String.valueOf( cursor.getLong( i ) );
                        } else {
                            value_str = cursor.getString( i );
                        }
                        if( value_str != null ) {
                            jObj.addProperty( cursor.getColumnName(i) , value_str );
                        }
                        if( jObj.size() != 0 && i == columnsCount - 1 ) {
                            jArray.add( jObj );
                        }
                    }
                }
                cursor.close();
                Gson gson = new Gson();
                if( jArray.size() != 0 ) {
                    s = s + gson.toJson(jArray);
                }
                //jArray = gson.fromJson(jsonStr, JsonArray.class);//JsonObject jObj = gson.fromJson(jsonStr, JsonObject.class);
            }

            final String trailor = "/`~<';";
            s = s + trailor;
            printWriter.print( s );
        }

        void createSocket() throws Exception {

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

