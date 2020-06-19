package com.youssefdirani.advertise_admin.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PermanentDao { //Dao is Data Access Object

    /*
    String column1 = "a_certain_column_name";
    String column2 = "another_column_name";

    @Update
    public void updateGroceries(AnnouncementEntity... announcementEntity); //and then db.groceryItemDao().updateGroceries(groceryItems.toArray(new GroceryItem[groceryItems.size()])

    @Query("SELECT * FROM " + tableName)
    List<AnnouncementEntity> getAll();

    @Query("SELECT * FROM " + tableName + " WHERE " + uid + " IN (:userIds)")
    List<AnnouncementEntity> loadAllByIds( int[] userIds );

    @Query("SELECT * FROM " + tableName + " WHERE " + column1 + " LIKE :first AND " +
            column2 + " LIKE :last LIMIT 1")
    AnnouncementEntity findByName(double first, double last);

    @Insert
    void insertAll(AnnouncementEntity... users);

    @Update
     public int updateSongs(List<Song> songs);

     @Delete
    void delete(AnnouncementEntity announcementEntity);
    */
//for the navheader table
    @Insert
    void insertNavHeader(NavHeaderEntity navHeaderEntity); //only going to be used once

    @Query( "SELECT * FROM nav_header LIMIT 1" )
    NavHeaderEntity getNavHeader();

    @Update
    void updateNavHeader(NavHeaderEntity navHeaderEntity); // it updates every field of the entity where it matches primary key value of navHeaderEntity. https://stackoverflow.com/questions/45789325/update-some-specific-field-of-an-entity-in-android-room

//Now for the nav table
    @Insert
    void insertNav(NavEntity navEntity);

    @Query( "SELECT * FROM nav WHERE index1 LIKE :index LIMIT 1" )
    NavEntity getNav( int index );

    @Query( "SELECT * FROM nav" )
    List<NavEntity> getAllNav();

    @Update
    void updateNav( NavEntity navEntity );

    @Delete
    void deleteNavEntity(NavEntity navEntity);

//for the DatabaseInfo table
    @Insert
    void insertDbInfoRecord( DatabaseInfo databaseInfo ); //only going to be used once

    @Query( "SELECT * FROM db_info LIMIT 1" )
    DatabaseInfo getDatabaseInfo();

//for the DatabaseLastUpdate table
    @Insert
    void insertDbLastUpdateRecord( DatabaseLastUpdate databaseLastUpdate ); //only going to be used once

    @Query( "SELECT * FROM db_last_update LIMIT 1" )
    DatabaseLastUpdate getDatabaseLastUpdate();

    @Update
    void updateDatabaseLastUpdateRecord( DatabaseLastUpdate databaseLastUpdate );

    //for the TablesLastUpdate table
    @Insert
    void insertTableLastUpdateRecord( TableLastUpdate tableLastUpdate ); //only going to be used once

    @Query( "SELECT * FROM table_last_update" )
    List<TableLastUpdate> getAllTableLastUpdate();

    @Update
    void updateTableLastUpdateRecord( TableLastUpdate tableLastUpdate ); // it updates every field of the entity where it matches primary key value of navHeaderEntity. https://stackoverflow.com/questions/45789325/update-some-specific-field-of-an-entity-in-android-room

    @Delete
    void deleteTablesLastUpdate(TableLastUpdate tableLastUpdate);

}
