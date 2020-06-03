package com.youssefdirani.advertise_admin;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "table_last_update") //actually this table will contain only 1 entity (record)
public class TableLastUpdate {
    @PrimaryKey  (autoGenerate = true)// is same as autoincrement.
    public int uid = 0;

    @ColumnInfo(name = "table_name")
    public String tableName;

    @ColumnInfo(name = "last_update")
    public long lastUpdate;

}
