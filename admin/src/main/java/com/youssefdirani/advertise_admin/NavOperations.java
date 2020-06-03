package com.youssefdirani.advertise_admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static android.app.Activity.RESULT_OK;

class NavOperations {
    private MainActivity activity;
    private final String Delete_Item = "delete item";
    private final String Move_Item_Up = "move item up";
    private final String Move_Item_Down = "move item down";
    private String navItemAction = "";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer; //This is the "super large" layout element.
    NavController navController; //a real (host) fragment
    private NavigationView navigationView;
    private Menu navMenu;
    private Toolbar toolbar;

    final int[] FragmentId = { R.id.nav_home, R.id.nav_fragment1, R.id.nav_fragment2, R.id.nav_fragment3,
            R.id.nav_fragment4, R.id.nav_fragment5 };

    void setupNavigation() {
        final int size = getNavMenuItemsCount();
        int[] itemsId = new int[ size ];
        for( int i = 0; i < size; i++ ) {
            itemsId[i] = navMenu.getItem(i).getItemId();
        }
        mAppBarConfiguration = new AppBarConfiguration.Builder( itemsId )
                .setDrawerLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(activity, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );

        //private final int[] FragmentId = { R.id.nav_0, R.id.nav_1, R.id.nav_2  };

        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() { //must be after setupWithNavController
            @Override
            public boolean onNavigationItemSelected( @NonNull MenuItem menuItem ) { //MenuItem.OnMenuItemClickListener is any better
                String returnValue = actUponNavMenuItemSelection( menuItem.getItemId() );
                if( !returnValue.equals("pass") ) { //whether pass or not to pass really matters.
                    return returnValue.equals("true");//whether true of false actually doesn't matter.
                }

                if( menuItem.getGroupId() != R.id.main_drawer_group ) {
                    return false;
                }
                //Log.i("Youssef", "OnNavigationItemSelectedListener where title is " + menuItem.getTitle() );
                navigateToMenuItem( menuItem.getItemId(), menuItem.getTitle().toString() ); //needed I guess, it's not automatic
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            private String actUponNavMenuItemSelection( int menuItemId ) {
                switch( menuItemId ) {
                    case R.id.nav_color:
                        Bundle bundle = new Bundle();
                        bundle.putInt( "id_of_layout" , R.id.linearlayout_navheader ); //for technical reasons, I have to pass in the checked menu item now. That is because after getting out of ChooseNavMenuIconFragment class, we cannot determine the checked menu item (weird but this is what happens)
                        bundle.putInt( "index_of_navmenuitem" , getCheckedItemOrder() );
                        bundle.putString( "action", "navigation layout background" );
                        navController.navigate( R.id.nav_color, bundle );
                        drawer.closeDrawer(GravityCompat.START);
                        return "true";
                    case R.id.nav_addnewitem:
                        //Log.i("Youssef", "add New Menu Item is clicked.");
                        if( FragmentId.length <= getNavMenuItemsCount() ) { //the idea is == actually
                            Toast.makeText(activity, "This is the maximum menu items you may have " +
                                            "for this version.\n" +
                                            "Please contact the developer for another version of the app.",
                                    Toast.LENGTH_LONG).show();
                            return "false";
                        }
                        createMenuItem_AlertDialog();
                        drawer.closeDrawer(GravityCompat.START);
                        return "true";
                    case R.id.nav_menuicon:
                        bundle = new Bundle();
                        bundle.putInt( "index_of_navmenuitem" , getCheckedItemOrder() ); //for technical reasons, I have to pass in the checked menu item now. That is because after getting out of ChooseNavMenuIconFragment class, we cannot determine the checked menu item (weird but this is what happens)
                        bundle.putString( "action", "navigation menu icon" );
                        navController.navigate( R.id.nav_menuicon, bundle );
                        drawer.closeDrawer(GravityCompat.START);
                        return "true";
                    case R.id.nav_renameitem:
                        //we want to rename the item
                        drawer.closeDrawer(GravityCompat.START);

                        int checkedItemOrder = getCheckedItemOrder();
                        ////Log.i("Youssef", "checkedItemOrder is " + checkedItemOrder);
                        ////Log.i("Youssef", "menu size is " + menu.size() );
                        if( checkedItemOrder != -1 ) {
                            rename_AlertDialog( navMenu.getItem( checkedItemOrder ) );
                        }
                        //surprisingly enough, navigationView.getCheckedItem().getOrder() always returns 0 thus not working right.
                        //invalidateOptionsMenu(); //https://stackoverflow.com/questions/28042070/how-to-change-the-actionbar-menu-items-icon-dynamically/35911398
                        return "true";
                    case R.id.nav_deleteitem:
                        //Log.i("Youssef", "deleting...");
                        //the following 2 lines are a hack. Without this hack, it works unreliably
                        drawer.closeDrawer(GravityCompat.START);
                        navItemAction = Delete_Item;
                        return "true";
                    case R.id.nav_moveupitem:
                        drawer.closeDrawer(GravityCompat.START);
                        navItemAction = Move_Item_Up;
                        return "true";
                    case R.id.nav_movedownitem:
                        drawer.closeDrawer(GravityCompat.START);
                        navItemAction = Move_Item_Down;
                        return "true";
                    default:
                        ////Log.i("Youssef", "menu item id is " + item.getItemId() );
                        return "pass";
                }
            }

        });
    }

    //Related to the navigation menu. Used to rename the checked navigation menu item
    private void rename_AlertDialog( MenuItem selectedMenuItem ) {
        final MenuItem menuItem = selectedMenuItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Renaming");

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT); // | InputType.TYPE_TEXT_VARIATION_PASSWORD); //this sets the input as a password, and will mask the text
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInputText = input.getText().toString();
                if( isUserInputTextNotFine( userInputText ) ) {
                    return;
                }
                menuItem.setTitle( userInputText );
                toolbar.setTitle(userInputText); //necessary. Another way (probably) is to change the label of the corresponding fragment.
                activity.dbOperations.setNameOfNavItem( getCheckedItemOrder(), userInputText );
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createMenuItem_AlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Please enter the name of the new window");

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT); // | InputType.TYPE_TEXT_VARIATION_PASSWORD); //this sets the input as a password, and will mask the text
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String userInputText = input.getText().toString();
                if( isUserInputTextNotFine( userInputText ) ) {
                    return;
                }

                //Now creating the new menuItem
                //int idOfNewMenuItem = FragmentId[ menu.size() - 2 ]; //2 because we have the Home and the add-new-item menu items
                final int id;
                id = getAFreeId();
                addNavMenuItem( id,navMenu.getItem(getNavMenuItemsCount() - 1).getOrder() + 1,
                        userInputText );
                Log.i("Youssef", "createMenuItem where title is " + userInputText);
                activity.dbOperations.addNavRecord( userInputText );
                activity.bottomNavOperations.setDefault();
                navigateToMenuItem( id, userInputText ); //inside this we have we setChecked to true
                Toast.makeText(activity, "Navigation menu item is successfully added.",
                        Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private int getAFreeId() { //that has not been used in any of the menu items
        final int size;
        size = getNavMenuItemsCount();
        for (int fragmentId : FragmentId) {
            ////Log.i("getAFreeId", "FragmentId[j] " + fragmentId );
            int i;
            for (i = 0; i < size; i++) {
                if( navMenu.getItem(i).getItemId() == fragmentId ) {
                    break;
                }
            }
            if (i == size) { //id not used
                return fragmentId;
            }
        }
        return 0; //should not be reached
    }

    private boolean isUserInputTextNotFine( @org.jetbrains.annotations.NotNull String userInputText ) {
        final int Max_Menu_Item_Chars = 25;
        if( userInputText.equals("") ) {
            return true; //we won't make a change
        }
        if( userInputText.contains("_") ) {
            Toast.makeText(activity, "We're not allowed to use underscore \"_\" in naming.",
                    Toast.LENGTH_LONG).show();
            return true; //we won't make a change
        }
        if( userInputText.length() > Max_Menu_Item_Chars ) {
            Toast.makeText(activity, "The name you entered is too long.",
                    Toast.LENGTH_LONG).show();
            return true; //we won't make a change
        }
        if( isNavigationItemNameAlreadyExisting( userInputText ) ) {
            Toast.makeText(activity, "The name you entered already exists.",
                    Toast.LENGTH_LONG).show();
            return true; //we won't make a change
        }
        return false;
    }

    private boolean isNavigationItemNameAlreadyExisting( String newName ) {
        final int size;
        size = getNavMenuItemsCount();
        for (int i = 0; i < size; i++) {
            MenuItem item;
            item = navMenu.getItem(i);
            if( newName.equalsIgnoreCase( item.getTitle().toString() ) ) {
                return true;
            }
        }
        return false;
    }

    private void addNavMenuItem( int id, int order, String title ) {
        final MenuItem createdMenuItem = navMenu.add( R.id.main_drawer_group, id,
                order, title ); //the order is in coherence with orderInCategory inside activity_main_drawer.xml. It may get larger than getMenuItemsCount(), this is because we delete items without fixing the order then when we may add again a new menu item.
        final int checkedItemOrder = getCheckedItemOrder();
        if( checkedItemOrder >= 0 ) {
            navMenu.getItem( checkedItemOrder ).setChecked(false);
        }
        //Log.i("Youssef", "order of newly created item is " + createdMenuItem.getOrder() );
        createdMenuItem.setChecked(true);
        setupNavigation(); //making it top-level (root) destination.
        toolbar.setTitle( title );
    }

    //Related to the navigation menu. Used to know the checked navigation menu item. It's really needed, especially for onNavigationItemSelected
    int getCheckedItemOrder() { //this is different than OnNavigationItemSelectedListener in that in the latter, it's not yet checked, but in the process of being so. While with getCheckedItemOrder it's already checked.
        final int size = getNavMenuItemsCount();
        for( int i = 0; i < size; i++ ) {
            MenuItem item = navMenu.getItem(i);
            if( item.isChecked() ) {
                return i;
            }
        }
        return -1;
    }
    private int getItemOrderFromTitle( String title ) { //this is different than OnNavigationItemSelectedListener in that in the latter, it's not yet checked, but in the process of being so. While with getCheckedItemOrder it's already checked.
        final int size = getNavMenuItemsCount();
        for( int i = 0; i < size; i++ ) {
            if( navMenu.getItem(i).getTitle().toString().equals(title) ) {
                return i;
            }
        }
        return -1;
    }
    int getNavMenuItemsCount() { //will always be menu.size() - 1, according to the structure of activity_main_drawer.xml file
        int count = 0;
        //Log.i("Youssef", "menu size is " + navMenu.size() );
        for( int i = 0; i < navMenu.size(); i++ ) { //going till i < menu.size() - 1 is also fine, but anyway.
            MenuItem item = navMenu.getItem(i);
            if( item.getGroupId() == R.id.main_drawer_group ) {
                //Log.i("Youssef", "order of item is " + item.getOrder());
                //Log.i("Youssef", "id of item is " + item.getItemId());
                count++;
            }
        }
        Log.i("Youssef", "getNavMenuItemsCount is " + count);
        return count;
    }

    private void setupDrawer() {
        drawer.addDrawerListener( new ActionBarDrawerToggle( activity, drawer,
                R.string.drawer_open, R.string.drawer_close ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                actOnDrawerClosed();
                hideKeyboard();
            }

            private void actOnDrawerClosed() {
                //Log.i("Youssef", "Drawer has finished closing");
                switch( navItemAction ) {
                    case Delete_Item:
                        navItemAction = ""; //must be first thing to do, to avoid infinite recursive calls.
                        //The following doesn't need the hack if it was launched from the options menu, but when removing an
                        // item and the drawer is still open then it is unreliable
                        int checkedItemOrder = getCheckedItemOrder();
                        if( checkedItemOrder != -1 ) {
                            int size = getNavMenuItemsCount();
                            if( size < 2 ) { //actually it's == 1
                                Toast.makeText(activity, "Cannot delete the last window !", Toast.LENGTH_LONG ).show();
                                return;
                            }
                            navMenu.removeItem( navMenu.getItem( checkedItemOrder ).getItemId() );
                            activity.optionsMenu.setFirstOptionsMenuIcon();
                            activity.dbOperations.removeNavRecord( checkedItemOrder );
                            Log.i("Youssef", "Delete_Item");
                            navigateToMenuItem( navMenu.getItem(0).getItemId(), navMenu.getItem(0).getTitle().toString() ); //this may cause a problem, so it must be in 'removeNavRecord'. I had a workaround by calling join() in removeNavRecord. Let's try it...
                            Toast.makeText(activity, "Successful Deletion", Toast.LENGTH_SHORT ).show();
                            //Log.i("Youssef", "After nav menu item deletion");
                        }
                        return;
                    case Move_Item_Up:
                        navItemAction = "";
                        int size = getNavMenuItemsCount();
                        checkedItemOrder = getCheckedItemOrder();
                        if( checkedItemOrder != -1 ) {
                            if( size < 2 ) {
                                Toast.makeText(activity,"Cannot reorder.\nJust one item exists !", Toast.LENGTH_LONG ).show();
                                return;
                            }
                            if( checkedItemOrder == 0 ) {
                                Toast.makeText(activity,"Menu Item is already on top !", Toast.LENGTH_LONG ).show();
                                return;
                            }
                            switchItems_Upwards( checkedItemOrder );
                            activity.dbOperations.switchNavItems_Upwards( checkedItemOrder );

                            //now to specify which menu item to check
                            MenuItem menuItem = navMenu.getItem(checkedItemOrder - 1 );
                            menuItem.setChecked(true);
                            activity.toolbar.setTitle( menuItem.getTitle().toString() );
                            //navigateToMenuItem( menuItem.getItemId(), menuItem.getTitle().toString() );

                            if( checkedItemOrder - 1 == 0 ) {
                                activity.optionsMenu.setFirstOptionsMenuIcon();
                            }
                            //I won't be navigating. Since we already see a fragment, and no need to reenter in it again
                            Toast.makeText(activity, "Successful reordering", Toast.LENGTH_SHORT ).show();
                        }
                        drawer.openDrawer(GravityCompat.START);
                        return;
                    case Move_Item_Down:
                        navItemAction = "";
                        size = getNavMenuItemsCount();
                        checkedItemOrder = getCheckedItemOrder();
                        if( checkedItemOrder != -1 ) {
                            if( size < 2 ) {
                                Toast.makeText(activity,"Cannot reorder.\nJust one item exists !", Toast.LENGTH_LONG ).show();
                                return;
                            }
                            if( checkedItemOrder == size - 1 ) {
                                Toast.makeText( activity,"Menu Item is already below all !", Toast.LENGTH_LONG ).show();
                                return;
                            }
                            switchItems_Upwards( checkedItemOrder + 1 );
                            activity.dbOperations.switchNavItems_Upwards( checkedItemOrder + 1 );

                            //now to specify which menu item to check
                            MenuItem menuItem = navMenu.getItem(checkedItemOrder + 1 );
                            menuItem.setChecked(true);
                            activity.toolbar.setTitle( menuItem.getTitle().toString() );
                            //I won't be navigating. Since we already see a fragment, and no need to reenter in it again
                            //navigateToMenuItem( menuItem.getItemId(), menuItem.getTitle().toString() );

                            if( checkedItemOrder == 0 ) {
                                activity.optionsMenu.setFirstOptionsMenuIcon();
                            }
                            Toast.makeText(activity, "Successful reordering", Toast.LENGTH_SHORT ).show();
                        }
                        drawer.openDrawer(GravityCompat.START);
                        return;
                    default:
                        //return;
                }
            }

            private void switchItems_Upwards( final int lowerItemOrder ) { //lowerItemOrder is the old lower item index
                final String title2 = navMenu.getItem( lowerItemOrder ).getTitle().toString();
                final Drawable drawable2 = navMenu.getItem( lowerItemOrder ).getIcon();
                final int id2 = navMenu.getItem( lowerItemOrder ).getItemId();
                final int order2 = navMenu.getItem( lowerItemOrder ).getOrder();
                final String title1 = navMenu.getItem( lowerItemOrder - 1 ).getTitle().toString();
                final Drawable drawable1 = navMenu.getItem( lowerItemOrder - 1 ).getIcon();
                final int id1 = navMenu.getItem( lowerItemOrder - 1 ).getItemId();
                final int order1 = navMenu.getItem( lowerItemOrder - 1 ).getOrder();
                navMenu.removeItem( id1 );
                navMenu.removeItem( id2 );
                addNavMenuItem( id1, order2, title1 ); //the important thing is the order.
                addNavMenuItem( id2, order1, title2 ); //this will be checked, and the other one will be unchecked.
                navMenu.findItem( id2 ).setIcon( drawable2 );
                navMenu.findItem( id1 ).setIcon( drawable1 );
            }

            private void hideKeyboard() {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                if( imm != null) {
                    //Log.i("Youssef", "inside hideKeyboard. hiding...");
                    View view = activity.getCurrentFocus();
                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                    if (view == null) {
                        //Log.i("Youssef", "inside hideKeyboard. view is null");
                        view = activity.findViewById(R.id.editText_navheadertitle); //any edit text works actually. https://stackoverflow.com/questions/1109022/close-hide-android-soft-keyboard
                    }
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

        });

    }
    
    void navigateToMenuItem( int idOfNewMenuItem, String title ) { //to navigate to menu items, not to fragments related to background color or icon choice.
        Bundle bundle = new Bundle();
        bundle.putInt( "nav_order", getItemOrderFromTitle( title ) ); //getCheckedItemOrder() is reliable after navController.navigate, not before, even if you did navMenu.findItem( idOfNewMenuItem ).setChecked(true); it still isn't reliable
        bundle.putInt( "bottombar_order", activity.bottomNavOperations.getCheckedItemOrder() );
        navigateToMenuItem( idOfNewMenuItem, bundle );
        Log.i("Youssef", "changing toolbar title in navigateToMenuItem to " + title);
        toolbar.setTitle( title );
        drawer.closeDrawer(GravityCompat.START); //after this, onResume in the fragment is called. Tested. Still, it's better to make sure using a timer or something.
    }

    private void navigateToMenuItem( final int idOfNewMenuItem, Bundle bundle ) {
         navController.navigate( idOfNewMenuItem, bundle );
        new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                activity.dbOperations.loadOnNavigate(
                    getItemOrderFromTitle(
                            navMenu.findItem(idOfNewMenuItem).getTitle().toString() ) );
            }
        }.start();
    }

    void setIconOfCheckedMenuItem( String tag, int indexOfNavMenuItem ) {
        if( tag == null ) {
            Log.i("setIcon..", "in navOperations - tag is null. indexOfNavMenuItem is " + indexOfNavMenuItem);
            return;
        }
        if( tag.equalsIgnoreCase("ic_no_icon") ) {
            Log.i("setIcon..", "in navOperations - it was no icon. indexOfNavMenuItem is " + indexOfNavMenuItem);
            MenuItem menuItem = navMenu.getItem( indexOfNavMenuItem );
            menuItem.setIcon(0);
            if( indexOfNavMenuItem == 0 ) {
                new Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            activity.optionsMenu.setFirstOptionsMenuIcon();
                        }
                }, 100); //unfortunately needed.
            }
            return;
        }
        //Log.i("addAnItem", "nav icon is set in ui of index " + nav_menuitem_index);
        int icon_drawable_id = activity.getResources().getIdentifier( tag, "drawable", activity.getPackageName() );
        Drawable icon = activity.getResources().getDrawable( icon_drawable_id );
        navMenu.getItem( indexOfNavMenuItem ).setIcon( icon );
        Log.i("setIcon..", "in navOperations - we have an icon now. indexOfNavMenuItem is " + indexOfNavMenuItem);
        if( indexOfNavMenuItem == 0 ) {
            new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        activity.optionsMenu.setFirstOptionsMenuIcon();
                    }
                }, 100); //unfortunately needed.
        }
    }

//##########################################################################################################################
//####################### onCreate #################################################################################
//##########################################################################################################################
    NavOperations( MainActivity activity, Toolbar toolbar ) {
        this.activity = activity;
        this.toolbar = toolbar;
    }

    NavigationView getNavigationView() {
        navigationView = activity.findViewById(R.id.nav_view);
        return navigationView;
    }

    void startingSetup( Menu navMenu ) {
        this.navMenu = navMenu;
        drawer = activity.findViewById( R.id.drawer_layout );
        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        setupNavigation();
        setupDrawer();// Set the drawer toggle as the DrawerListener
    }

    void addAnItem( NavEntity navEntity ) { //navMenu.getItem( navOperations.getNavMenuItemsCount() - 1 ).getOrder() + 1
        //Log.i("addAnItem", "order is " + (getNavMenuItemsCount() + 1) );
        navMenu.add( R.id.main_drawer_group, getAFreeId(),getNavMenuItemsCount() + 1, navEntity.title );
        setIconOfCheckedMenuItem( navEntity.iconTag,getNavMenuItemsCount() - 1 );
    }

    void updateNavItem( int navIndex, String title, String iconTag ) { //useful to update either the title or the icon
        if( title != null && !title.equalsIgnoreCase("") ) {
            navMenu.getItem(navIndex).setTitle(title);
            Log.i("Youssef", "Title is updated to " + title);
        }
        setIconOfCheckedMenuItem( iconTag, navIndex );
    }

//##########################################################################################################################
//####################### Nav Header Stuff #################################################################################
//##########################################################################################################################
    void setLayoutColor( int linearlayout_id, final String tag ) { //this is same as saying setNavHeaderBackgroundColor
        LinearLayout linearLayout = activity.findViewById( linearlayout_id );
        int color_id = activity.getResources().getIdentifier( tag, "color", activity.getPackageName() );
        linearLayout.setBackgroundColor( activity.getResources().getColor( color_id ) );
    }

    boolean onSupportNavigateUp() {
        activity.dbOperations.loadNavHeaderStuff();
        return NavigationUI.navigateUp( navController, mAppBarConfiguration );
    }

    void setNavHeader() { //if this caused trouble, you may call it from onSupportNavigateUp()
        setNavHeaderImage();
        setNavHeaderTitles();
    }

    private void setNavHeaderTitles() {
        final EditText editText_navHeaderTitle = activity.findViewById(R.id.editText_navheadertitle);
        final EditText editText_navHeaderSubtitle = activity.findViewById(R.id.editText_navheadersubtitle);
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( ( v.equals( editText_navHeaderTitle ) || v.equals( editText_navHeaderSubtitle ) ) && !hasFocus ) {
                    // code to execute when EditText loses focus
                    activity.dbOperations.saveNavHeaderTitles( (EditText) v, editText_navHeaderTitle, editText_navHeaderSubtitle );
                }
            }
        };
        editText_navHeaderTitle.setOnFocusChangeListener( onFocusChangeListener );
        editText_navHeaderSubtitle.setOnFocusChangeListener( onFocusChangeListener );
    }

    //Related to the navigation menu (header actually)
    private final int REQUEST_CODE_LOAD_IMG = 1;
    private void setNavHeaderImage() {
        final ImageButton imageButton_navheadermain = activity.findViewById(R.id.imagebutton_navheadermain);
        if( imageButton_navheadermain != null ) { //won't be null I believe
            //for the client app, it's best to store the image to sd-card and the path to shared preferences https://stackoverflow.com/questions/8586242/how-to-store-images-using-sharedpreference-in-android
            imageButton_navheadermain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    activity.startActivityForResult(photoPickerIntent, REQUEST_CODE_LOAD_IMG);
                }
            });
        }
    }

    void onActivityResult(int reqCode, int resultCode, Intent data) {
        //Log.i("onActivityResult", "inside");
        if( reqCode == REQUEST_CODE_LOAD_IMG && resultCode == RESULT_OK ) {
            try {
                final Uri imageUri = data.getData();

                if (imageUri == null) {//should never happen
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG).show();
                    return;
                }
                //final SharedPreferences.Editor prefs_editor = client_app_data.edit();

                final InputStream imageStream = activity.getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = Bitmap.createScaledBitmap(selectedImage, 220, 220, false);
                //saving the image in the scaled size
                final String imagePath = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + File.separator + "navmenu.jpg";
                File f = new File(imagePath); //https://stackoverflow.com/questions/57116335/environment-getexternalstoragedirectory-deprecated-in-api-level-29-java and https://developer.android.com/reference/android/content/Context#getExternalFilesDirs(java.lang.String)
                OutputStream fOut = new FileOutputStream(f);
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();

                final Bitmap bitmap = selectedImage;
                activity.dbOperations.saveNavHeaderImg( imagePath );
                ImageButton imageButton_navheadermain = activity.findViewById(R.id.imagebutton_navheadermain);
                imageButton_navheadermain.setImageBitmap( bitmap );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG).show();
            } catch (IOException e) { //for the sake of fOut.flush and .close
                e.printStackTrace();
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

//##########################################################################################################################
//#######################                        ###########################################################################
//##########################################################################################################################

}
