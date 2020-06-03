package com.youssefdirani.advertise_admin.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youssefdirani.advertise_admin.MainActivity;
import com.youssefdirani.advertise_admin.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ChooseColorFragment extends Fragment {
    private MainActivity activity;
    private int indexOfNavMenuItem = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("Youssef", "ChooseColorFragment - no arguments");

        View root = inflater.inflate( R.layout.fragment_colors, container, false );

        final Bundle args = getArguments();
        if( args == null ) {
            Log.i("Youssef", "inside ChooseColorFragment : no arguments");
        } else {
            indexOfNavMenuItem = getArguments().getInt("index_of_navmenuitem");
            Log.i("Youssef", "inside ChooseColorFragment : index_of_navmenuitem is " + indexOfNavMenuItem);
            activity = (MainActivity) getActivity();
            activity.hideOptionsMenuAndBottomMenu();
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ImageButton imageButton = (ImageButton) v;
                    //Drawable icon_drawable = imageButton.getDrawable(); //R.drawable.ic_remove_red_eye_black_24dp; //R.drawable.ic_action_home
                    String tag = v.getTag().toString();
                    Log.i("Youssef", "ChooseColorFragment - inside onClick " + tag );
                    switch( getArguments().getString("action") ) {
                        case "navigation layout background":
                            final int idOfLayout = getArguments().getInt("id_of_layout");
                            activity.setLayoutColor( idOfLayout, tag );//this is same as saying setNavHeaderBackgroundColor. It has a database call within.
                            break;
                        case "status bar background color":
                            activity.setStatusBarColor( tag );
                            activity.setStatusBarColorInDb( indexOfNavMenuItem, tag );
                            break;
                        case "top bar background color":
                            activity.setTopBarBackgroundColor( tag );
                            activity.setTopBarColorInDb( indexOfNavMenuItem, tag );
                            break;
                        case "top bar hamburger color":
                            activity.setTopBarHamburgerColor( tag );
                            activity.setTopBarHamburgerColorInDb( indexOfNavMenuItem, tag );
                            break;
                        case "top bar title color":
                            activity.setTopBarTitleColor( tag );
                            activity.setTopBarTitleColorInDb( indexOfNavMenuItem, tag );
                            break;
                        case "top bar 3-dots color":
                            activity.setTopBar3DotsColor( tag );
                            activity.setTopBar3DotsColorInDb( indexOfNavMenuItem, tag );
                            break;
                        case "bottom bar background color":
                            //Log.i("Youssef", "inside ChooseColorFragment : position 1");
                            activity.setBottomBarBackgroundColor( tag );
                            //Log.i("Youssef", "inside ChooseColorFragment : position 2");
                            activity.setBottomBarBackgroundColorInDb( indexOfNavMenuItem, tag );
                            //Log.i("Youssef", "inside ChooseColorFragment : position 3");
                            break;
                        default:
                    }

                    activity.onBackPressed(); //better than activity.getSupportFragmentManager().popBackStack(); //https://stackoverflow.com/questions/2717954/android-simulate-back-button
                }
            };
            root.findViewById(R.id.imagebutton_cardview_dark_background).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_cardview_shadow_start_color).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorAccent).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorPrimaryDark).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_design_default_color_error).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorGreen).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_design_default_color_primary).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorBlack).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_design_default_color_secondary_variant).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorViolet).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorGray).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorYellowLight).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorWhite).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorBrown).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorSwamp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorSkyBlue).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorRedWine).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_colorPurple).setOnClickListener(onClickListener);
        }
        return root;
    }

    @Override
    public void onResume() {
        Log.i("Youssef", "inside ChooseColorFragment : inside onResume");
        super.onResume();
        //activity.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("Youssef", "inside ChooseColorFragment : inside onPause");
        //MUST RETURN SOMETHING USING onActivityResult(...); that is for technical reasons.
        //activity.onActivityResult( activity.CHOOSE_MENUICON_REQUESTCODE, RESULT_CANCELED,null );
        //activity.appearChooseNavigationIcon_MenuItem();
        if( indexOfNavMenuItem != -1 ) {
            activity.updateToolbarTitle(indexOfNavMenuItem); //unfortunately needed.
        }
        activity.showOptionsMenuAndBottomMenu( indexOfNavMenuItem );
        activity.setFirstOptionsMenuIcon();
    }
}
