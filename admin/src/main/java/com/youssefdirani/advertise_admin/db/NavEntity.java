package com.youssefdirani.advertise_admin.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nav")
public class NavEntity {
    @PrimaryKey  (autoGenerate = true)// is same as autoincrement.
    public int uid = 0;

    @ColumnInfo(name = "index1") //I named "order" because "index" might cause trouble.
    public int index; //may be needed for it to be auto-incremented

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "icon_tag")
    public String iconTag;

    @ColumnInfo(name = "statusbar_backgroundcolortag")
    public String statusBar_backgroundColorTag;

    @ColumnInfo(name = "statusbar_dark")
    public boolean statusBar_dark;

    @ColumnInfo(name = "topbar_backgroundcolortag")
    public String topBar_backgroundColorTag;

    @ColumnInfo(name = "topbar_hamburgercolortag")
    public String topBar_hamburgerColorTag;

    @ColumnInfo(name = "topbar_titlecolortag")
    public String topBar_titleColorTag;

    @ColumnInfo(name = "topbar_3dotscolortag")
    public String topBar_3dotsColorTag;

    @ColumnInfo(name = "bottombar_backgroundcolortag")
    public String bottombar_backgroundColorTag;
}
