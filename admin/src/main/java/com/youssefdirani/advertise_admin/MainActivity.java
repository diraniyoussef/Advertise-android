package com.youssefdirani.advertise_admin;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

public class MainActivity extends AppCompatActivity {

    public boolean isMemoryLow= false; //this can be checked before we enter or enlarge the database
    //https://developer.android.com/topic/performance/memory
    @Override
    public void onTrimMemory(int level) { //implemented by ComponentCallbacks2 automatically in AppCompatActivity
        // Determine which lifecycle or system event was raised.
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */
                //break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                //break;
            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                toast("Warning !\nMemory is low ! Please consider resolving the problem so the data remains " +
                        "consistent.", Toast.LENGTH_LONG);
                isMemoryLow = true;
                System.gc();
                break;
        }
    }

    Menu navMenu;
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    Menu bottomMenu;

    DbOperations dbOperations;
    NavOperations navOperations;
    BottomNavOperations bottomNavOperations;
    public MutableLiveData<String> lastBottomNav = new MutableLiveData<>();
    OptionsMenu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Youssef", "inside MainActivity : onCreate");
        dbOperations = new DbOperations( this );
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navOperations = new NavOperations( MainActivity.this, toolbar );
        navMenu = navOperations.getNavigationView().getMenu();
        navOperations.startingSetup( navMenu );

        bottomNavigationView = findViewById( R.id.bottomnavview);
        bottomMenu = bottomNavigationView.getMenu();
        optionsMenu = new OptionsMenu( MainActivity.this, bottomMenu );
        bottomNavOperations = new BottomNavOperations( MainActivity.this, bottomMenu );
        bottomNavOperations.setupBottomNavigation();
        bottomNavigationView.setVisibility( BottomNavigationView.INVISIBLE );

        bottomNavOperations.keepOnly1Item();
        dbOperations.onCreate(); //setting initial stuff if not yet set, and loading from tables all relevant data.

    }

    public void setFirstOptionsMenuIcon() { //reason of why is this needed : when we get back from another fragment like set icon e.g., the icon shows by default (which we don't always want)
        Log.i("set icon", "before setFirstOptionsMenuIcon");
        optionsMenu.setFirstOptionsMenuIcon();
        /*
        new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("set icon", "before setFirstOptionsMenuIcon");
                        optionsMenu.setFirstOptionsMenuIcon();
                    }
                }, 20); //unfortunately needed.

         */
        Log.i("Youssef", "after setFirstOptionsMenuIcon handler");
    }

    //Called when the user presses on the stack navigation icon in order to navigate https://developer.android.com/reference/android/support/v7/app/AppCompatActivity#onSupportNavigateUp()
    @Override
    public boolean onSupportNavigateUp() {
        //Log.i("Youssef", "MainActivity - inside onSupportNavigateUp");
        return navOperations.onSupportNavigateUp()
                || super.onSupportNavigateUp();
    }

    public void hideOptionsMenuAndBottomMenu() {
        toolbar.getMenu().setGroupVisible( R.id.optionsmenu_actionitemgroup,false );
        bottomNavigationView.setVisibility( BottomNavigationView.INVISIBLE );
    }

    public void showOptionsMenuAndBottomMenu( final int indexOfNavMenuItem ) {
        toolbar.getMenu().setGroupVisible( R.id.optionsmenu_actionitemgroup,true );
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                Looper.prepare();
                dbOperations.loadBb( indexOfNavMenuItem, false ); //we need to get it from the database to know whether  to show it or not.
                Looper.loop();
            }
        }.start();
    }

    public void updateToolbarTitle( int indexOfNewMenuItem ) {
        //Log.i("Youssef", "updateToolbarTitle to " + navMenu.getItem( indexOfNewMenuItem ).getTitle() );
        toolbar.setTitle( navMenu.getItem( indexOfNewMenuItem ).getTitle().toString() );
    }

    void toast( String textToToast, int duration ) {
        Toast.makeText(MainActivity.this, textToToast, duration).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("Youssef", "inside MainActivity : onStart");


    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Youssef", "inside MainActivity : onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("Youssef", "inside MainActivity : onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i("Youssef", "inside MainActivity : onStop");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbOperations.onDestroy();
        Log.i("Youssef", "inside MainActivity : onDestroy");
    }

    //Called when user presses the 3 vertical dots on the right. Initiated when the user presses the 3 vertical dots.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);//this should be before adding menus maybe. findItem https://stackoverflow.com/questions/16500415/findviewbyid-for-menuitem-returns-null (and using menu.findItem may be better than findViewById ?, not sure)
        Log.i("Youssef", "MainActivity - inside onCreateOptionsMenu");
        navOperations.setNavHeader();
        return true;
    }

    public void setLayoutColor( int linearlayout_id, final String tag ) {
        navOperations.setLayoutColor( linearlayout_id, tag );
        dbOperations.saveNavHeaderBackgroundColor( tag );//saving into the database
    }

    public void setIconOfCheckedMenuItem( final String tag, final int nav_menuitem_index, String menu ) {
        Log.i("setIcon..", "item index is " + nav_menuitem_index);
        if( menu.equals("nav menu") ) {
            navOperations.setIconOfCheckedMenuItem( tag, nav_menuitem_index);
            new Thread() { //opening the database needs to be on a separate thread.
                public void run() {
                    dbOperations.setIconOfCheckedNavMenuItem( tag, nav_menuitem_index );
                }
            }.start();
        } else if( menu.equals("bottom nav menu") ) {
            bottomNavOperations.setIconOfCheckedMenuItem( tag, bottomNavOperations.getCheckedItemOrder() );
            new Thread() { //opening the database needs to be on a separate thread.
                public void run() {
                    dbOperations.setIconOfCheckedBottomNavMenuItem( tag, nav_menuitem_index );
                }
            }.start();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.i("Youssef", "MainActivity - inside onPrepareOptionsMenu");
        return true;
    }

    //Called when the user presses a menu item below the 3 vertical dots.
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item ) {
        //Log.i("Youssef", "MainActivity - inside onOptionsItemSelected");
        return optionsMenu.onOptionsItemSelected( item, super.onOptionsItemSelected(item) );
    }

    //These here are callbacks from other classes.
    public void setStatusBarColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor( ContextCompat.getColor(this, color_id ) );
        }
    }
    public void setStatusBarColorInDb( final int navIndex, final String tag ) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                //Log.i("Youssef", "Setting BB background color of " + navIndex  + " to " + tag);
                dbOperations.setStatusBarColorTag( navIndex, tag );
            }
        }.start();
    }

    public void setTopBarBackgroundColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        toolbar.setBackgroundColor( ContextCompat.getColor(this, color_id) );
    }
    public void setTopBarHamburgerColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        toolbar.getNavigationIcon().setColorFilter(
                ContextCompat.getColor(this, color_id ), PorterDuff.Mode.SRC_ATOP );
    }

    public void setTopBarTitleColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        toolbar.setTitleTextColor( getResources().getColor( color_id ) ); //the action bar text
    }
    public void setTopBar3DotsColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        toolbar.getOverflowIcon().setColorFilter(
                ContextCompat.getColor(this, color_id ), PorterDuff.Mode.SRC_ATOP );
    }
    public void setBottomBarBackgroundColor( String tag ) {
        int color_id = getResources().getIdentifier( tag, "color", getPackageName() );
        bottomNavigationView.setBackgroundColor( ContextCompat.getColor(this, color_id) );
    }
    public void setBottomBarBackgroundColorInDb( final int navIndex, final String tag ) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                //Log.i("Youssef", "Setting BB background color of " + navIndex  + " to " + tag);
                dbOperations.updateBbBackgroundColorTag( navIndex, tag );
            }
        }.start();
    }

    //Related to the navigation menu. Used to retrieve the image from gallery and save it
    @Override
    public void onActivityResult( int reqCode, int resultCode, Intent data ) {
        super.onActivityResult(reqCode, resultCode, data);
        navOperations.onActivityResult( reqCode, resultCode, data );
    }

    @Override
    public void onBackPressed() {
        if( navOperations.getCheckedItemOrder() != -1 ) { //root (top-level). IDK how universal this is
            finish();
        }
        super.onBackPressed();  // optional depending on your needs
    }

    void onBottomBarRemove() {
        bottomNavigationView.setVisibility( BottomNavigationView.INVISIBLE );
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                int navIndex = navOperations.getCheckedItemOrder();
                dbOperations.updateBbBackgroundColorTag( navIndex,
                        "none" ); //this is correlated to loadBb in DbOperations in my convention, so it's important.
                dbOperations.deleteBbTable( navIndex );
                dbOperations.deleteBottomNavContentTablesButKeepUpTo(0, navIndex);
            }
        }.start();
    }

    void onBB_Add() {
        bottomNavOperations.setDefault();
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                dbOperations.updateBbBackgroundColorTag( navOperations.getCheckedItemOrder(),
                        "colorWhite" );
                dbOperations.setBottomBarTable();
            }
        }.start();
    }

    void setStatusBarIconTint( boolean isChecked ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            if( isChecked ) { //make dark
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(0);
            }
        }
    }

    void setStatusBarIconTintMenuItem( final boolean isChecked ) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                final MenuItem menuItem = toolbar.getMenu().findItem(R.id.statusbar_icontint);
                menuItem.setChecked( isChecked ); //redundant in case the user clicked, but needed in case the user navigates and tint is set from database e.g.
            }
        }, 100); //unfortunately needed.
    }

    public void setTopBarColorInDb( final int indexOfNavMenuItem, final String tag) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                //Log.i("Youssef", "Setting BB background color of " + navIndex  + " to " + tag);
                dbOperations.setTopBarBackgroundColorTag( indexOfNavMenuItem, tag );
            }
        }.start();
    }

    public void setTopBarHamburgerColorInDb( final int indexOfNavMenuItem, final String tag) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                //Log.i("Youssef", "Setting BB background color of " + navIndex  + " to " + tag);
                dbOperations.setTopBarHamburgerColorTag( indexOfNavMenuItem, tag );
            }
        }.start();
    }

    public void setTopBarTitleColorInDb( final int indexOfNavMenuItem, final String tag) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                dbOperations.setTopBarTitleColorTag( indexOfNavMenuItem, tag );
            }
        }.start();
    }

    public void setTopBar3DotsColorInDb( final int indexOfNavMenuItem, final String tag) {
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                dbOperations.setTopBar3DotsColorTag( indexOfNavMenuItem, tag );
            }
        }.start();
    }
/*
    public void freezeUI() {

    }

 */
}
