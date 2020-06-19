package com.youssefdirani.advertise_admin.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                NavHeaderEntity.class,
                NavEntity.class,
                DatabaseInfo.class,
                TableLastUpdate.class,
                DatabaseLastUpdate.class
        },
        version = 1,
        exportSchema = false )
public abstract class AppDatabase extends RoomDatabase {
    public abstract PermanentDao permanentDao();
}

