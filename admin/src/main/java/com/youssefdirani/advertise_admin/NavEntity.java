package com.youssefdirani.advertise_admin;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nav")
class NavEntity {
    @PrimaryKey  (autoGenerate = true)// is same as autoincrement.
    int uid = 0;

    @ColumnInfo(name = "index1") //I named "order" because "index" might cause trouble.
    int index; //may be needed for it to be auto-incremented

    @ColumnInfo(name = "title")
    String title;

    @ColumnInfo(name = "icon_tag")
    String iconTag;

    @ColumnInfo(name = "statusbar_backgroundcolortag")
    String statusBar_backgroundColorTag;

    @ColumnInfo(name = "statusbar_dark")
    boolean statusBar_dark;

    @ColumnInfo(name = "topbar_backgroundcolortag")
    String topBar_backgroundColorTag;

    @ColumnInfo(name = "topbar_hamburgercolortag")
    String topBar_hamburgerColorTag;

    @ColumnInfo(name = "topbar_titlecolortag")
    String topBar_titleColorTag;

    @ColumnInfo(name = "topbar_3dotscolortag")
    String topBar_3dotsColorTag;

    @ColumnInfo(name = "bottombar_backgroundcolortag")
    String bottombar_backgroundColorTag;
}
