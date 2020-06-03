package com.youssefdirani.advertise_admin;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;

class BottomNavOperations {
    private MainActivity activity;
    private Menu bottomMenu;

    BottomNavOperations(MainActivity activity, Menu bottomMenu ) {
        this.activity = activity;
        this.bottomMenu = bottomMenu;
    }

    void setupBottomNavigation() {
        activity.bottomNavigationView.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) { //MenuItem.OnMenuItemClickListener is any better
                //Log.i("Youssef", "bottom tab " + getCheckedItemOrder() + " is clicked.");
                switch( menuItem.getItemId() ) {
                    case R.id.navigation_bottom_1:
                        //Log.i("Youssef", "bottom tab " + getCheckedItemOrder_ofBottomBar() + " is clicked.");
                        activity.lastBottomNav.setValue("0");
                        return true;
                    case R.id.navigation_bottom_2:
                        //Log.i("Youssef", "second bottom tab is clicked.");
                        activity.lastBottomNav.setValue("1");
                        return true;
                    case R.id.navigation_bottom_3:
                        //Log.i("Youssef", "third bottom tab is clicked.");
                        activity.lastBottomNav.setValue("2");
                        return true;
                    case R.id.navigation_bottom_4:
                        //Log.i("Youssef", "fourth bottom tab is clicked.");
                        activity.lastBottomNav.setValue("3");
                        return true;
                    default:

                        return false;
                }
            }
        });
    }

    int getCheckedItemOrder() {
        for( int i = 0; i < bottomMenu.size(); i++ ) {
            if( bottomMenu.getItem(i).isChecked() ) {
                return i;
            }
        }
        return -1;
    }

    void keepOnly1Item() { //called in MainActivity onResumme //this is made so the database keeps track on every change the user makes.
        int size = bottomMenu.size();
        for( int i = size - 1 ; i > 0 ; i-- ) { //you have to do it in this reversed order because the indexes change on every item removal.
            bottomMenu.removeItem( bottomMenu.getItem( i ).getItemId() );
        }
    }

    void setIconOfCheckedMenuItem( String tag, int checkedItemOrder ) {
        if( tag.equalsIgnoreCase("ic_no_icon" ) ) {
            //it's good that the bottom bar still remembers the checked item order after returning back from ChooseNavMenuIconFragment. Here we won't be using nav_menuitem_index, it's been useful for the toolbar
            MenuItem menuItem = bottomMenu.getItem( checkedItemOrder );
            menuItem.setIcon(0);
            return;
        }
        int icon_drawable_id = activity.getResources().getIdentifier( tag, "drawable", activity.getPackageName() );
        Drawable icon = activity.getResources().getDrawable( icon_drawable_id );
        bottomMenu.getItem( checkedItemOrder ).setIcon( icon ); //it's good that the bottom bar still remembers the checked item order after returning back from ChooseNavMenuIconFragment
    }

    void setDefault() {
        activity.bottomNavigationView.setVisibility(BottomNavigationView.VISIBLE);
        keepOnly1Item(); //has to be before setIconOfCheckedMenuItem
        bottomMenu.getItem(0).setChecked(true);
        //Now let's show the bottombar in coherence with DbOperations (especially addNavRecord method)
        setIconOfCheckedMenuItem( "ic_no_icon",0 );
        activity.setBottomBarBackgroundColor( "colorWhite" );
        bottomMenu.getItem(0).setTitle("Option 1");
    }

}
