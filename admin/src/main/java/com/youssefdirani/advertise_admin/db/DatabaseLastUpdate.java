package com.youssefdirani.advertise_admin.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "db_last_update") //actually this table will contain only 1 entity (record)
public class DatabaseLastUpdate {
    @PrimaryKey  //(autoGenerate = true)// is same as autoincrement.
    public int uid = 0;

    @ColumnInfo(name = "last_update")
    public long lastUpdate;
}
