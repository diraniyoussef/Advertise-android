package com.youssefdirani.advertise_admin;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "db_info") //actually this table will contain only 1 entity (record)
public class DatabaseInfo {
    @PrimaryKey  //(autoGenerate = true)// is same as autoincrement.
    public int uid = 0;

    @ColumnInfo(name = "last_update")
    public long lastUpdate;

    @ColumnInfo(name = "owner_name")
    public String ownerName;

    @ColumnInfo(name = "owner_phonenumber")
    public String ownerPhoneNumber;
}
