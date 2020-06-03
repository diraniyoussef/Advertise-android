package com.youssefdirani.advertise_admin.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.youssefdirani.advertise_admin.MainActivity;
import com.youssefdirani.advertise_admin.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ChooseMenuIconFragment extends Fragment {
    private MainActivity activity;
    private int indexOfNavMenuItem = -1; //for technical reasons, I have to pass in the checked menu item now. That is because after getting out of ChooseNavMenuIconFragment class, we cannot determine the checked menu item (weird but this is what happens)
    private String action;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("ChooseMenuIconFragment", "no arguments");
        View root = inflater.inflate(R.layout.fragment_menuicons, container, false);
        activity = (MainActivity) getActivity();
        activity.hideOptionsMenuAndBottomMenu();

        Bundle args = getArguments();
        if( args == null ) {
            Log.i("Youssef", "inside ChooseNavMenuIconFragment : no arguments");
        } else {
            indexOfNavMenuItem = getArguments().getInt("index_of_navmenuitem");

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton imageButton = (ImageButton) v;
                    //Drawable icon_drawable = imageButton.getDrawable(); //R.drawable.ic_remove_red_eye_black_24dp; //R.drawable.ic_action_home
                    String tag = v.getTag().toString();
                    Log.i("ChooseMenuIconFragment", "inside onClick " + tag );
                    action = getArguments().getString("action");
                    switch( action ) {
                        case "navigation menu icon":
                            activity.setIconOfCheckedMenuItem( tag, indexOfNavMenuItem, "nav menu");
                            break;
                        case "bottom bar menu item":
                            activity.setIconOfCheckedMenuItem( tag, indexOfNavMenuItem, "bottom nav menu");
                            break;
                        default:
                    }

                    activity.onBackPressed(); //better than activity.getSupportFragmentManager().popBackStack(); //https://stackoverflow.com/questions/2717954/android-simulate-back-button
                }
            };
            root.findViewById(R.id.imagebutton_ic_menu_camera).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_action_home).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_home_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_menu_gallery).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_menu_manage).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_menu_send).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_menu_share).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_menu_slideshow).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_perm_camera_mic_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_person_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_person_pin_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_pets_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_refresh_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_remove_red_eye_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_settings_applications_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_settings_ethernet_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_shopping_basket_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_shopping_cart_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_touch_app_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_vignette_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_weekend_black_24dp).setOnClickListener(onClickListener);
            root.findViewById(R.id.imagebutton_ic_no_icon).setOnClickListener(onClickListener);
        }
        return root;
    }

    @Override
    public void onResume() {
        Log.i("ChooseMenuIconFragment", "inside onResume");
        super.onResume();
        //activity.onBackPressed();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("ChooseMenuIconFragment", "inside onPause");
        //MUST RETURN SOMETHING USING onActivityResult(...); that is for technical reasons.
        //activity.onActivityResult( activity.CHOOSE_MENUICON_REQUESTCODE, RESULT_CANCELED,null );
        //activity.appearChooseNavigationIcon_MenuItem();
        if( indexOfNavMenuItem != -1 ) {
            activity.updateToolbarTitle( indexOfNavMenuItem ); //unfortunately needed.
        }
        activity.showOptionsMenuAndBottomMenu( indexOfNavMenuItem );
        activity.setFirstOptionsMenuIcon();
    }

}
