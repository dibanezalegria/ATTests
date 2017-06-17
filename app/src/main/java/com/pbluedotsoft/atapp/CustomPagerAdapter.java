package com.pbluedotsoft.atapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pbluedotsoft.atapp.data.EXTRAS;

/**
 * Created by Daniel Ibanez on 2016-10-04.
 */

public class CustomPagerAdapter extends FragmentPagerAdapter {

    private static final String LOG_TAG = CustomPagerAdapter.class.getSimpleName();

    private String mTestURI, mTestCode;
    private int mInOut;

    public CustomPagerAdapter(Context context, FragmentManager fm, Bundle bundle) {
        super(fm);
        mTestCode = bundle.getString(EXTRAS.KEY_TEST_CODE);
        mTestURI = bundle.getString(EXTRAS.KEY_URI);
        mInOut = bundle.getInt(EXTRAS.KEY_INOUT);
    }

    @Override
    public Fragment getItem(int position) {
        // What test should I create?
        Fragment fragment;
        switch (mTestCode) {
            case "VAS":
                fragment = new VASFragment();
                break;
            case "SOFI":
                fragment = new SofiFragment();
                break;
            case "HAQ":
                fragment = new HAQFragment();
                break;
            case "JAMAR":
                fragment = new JamarFragment();
                break;
            case "VIGO":
                fragment = new VigoFragment();
                break;
            default:
                fragment = new VASFragment();
        }

        // Android recommends to use Bundle to pass parameters to Fragments
        // instead of parameters in the constructor.
        // What tab is it? IN or OUT
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRAS.KEY_TAB, position);
        bundle.putString(EXTRAS.KEY_URI, mTestURI);
        bundle.putInt(EXTRAS.KEY_INOUT, mInOut);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "IN";
            case 1:
                return "UT";
            default:
                return "";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}
