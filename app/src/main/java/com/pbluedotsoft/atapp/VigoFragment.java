package com.pbluedotsoft.atapp;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentVigoBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class VigoFragment extends Fragment {

    private static final String LOG_TAG = VigoFragment.class.getSimpleName();

    private Uri mTestUri;
    private int mTab, mInOut;
    private boolean mHighlightsON;

    private FragmentVigoBinding bind;

    public VigoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binding instead of findViewById
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_vigo, container, false);

        // Test URI
        mTestUri = Uri.parse(getArguments().getString(EXTRAS.KEY_URI));

        // Tab (IN or OUT)
        mTab = getArguments().getInt(EXTRAS.KEY_TAB);

        // IN or OUT selected at TestListActivity
        mInOut = getArguments().getInt(EXTRAS.KEY_INOUT);

        // Disable touch events in the 'other' tab
        if ((mTab == Test.IN && mInOut == 1) || (mTab == Test.OUT && mInOut == 0)) {
            disableTouchOnLayout(bind.scrollview);
        }

        // IN or OUT background color adjustments
        if (mTab == Test.IN) {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_in));

        } else {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_out));
        }

        // Layout background listener closes soft keyboard
        bind.scrollview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.vigo_gender_spinner,
                R.layout.spinner_list_item);

        bind.spinGender.setAdapter(adapter);

        return bind.getRoot();
    }


    /**
     * Disable all views in a given layout
     */
    private void disableTouchOnLayout(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            if (child instanceof ViewGroup) {
                disableTouchOnLayout((ViewGroup) child);
            }
        }
    }
}
