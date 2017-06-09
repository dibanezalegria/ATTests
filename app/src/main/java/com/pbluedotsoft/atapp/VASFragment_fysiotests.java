//package com.pbluedotsoft.atapp;
//
//
//import android.content.ContentValues;
//import android.content.DialogInterface;
//import android.database.Cursor;
//import android.databinding.DataBindingUtil;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
//import com.pbluedotsoft.atapp.data.EXTRAS;
//import com.pbluedotsoft.atapp.data.Test;
//import com.pbluedotsoft.atapp.databinding.FragmentVasBinding;
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class VASFragment_fysiotests extends Fragment implements View.OnClickListener {
//
//    private static final String LOG_TAG = VASFragment_fysiotests.class.getSimpleName();
//
//    private static final int N_SLIDERS = 4;
//
//    // Save state constant
//    private static final String STATE_CONTENT = "state_content";
//
//    private SeekBar[] mSeekBars;
//    private TextView[] mTextViews;
//    private ImageButton[] mPlusBtn, mMinusBtn;
//
//    private Uri mTestUri;
//    private int mTab, mInOut;
//
//    private FragmentVasBinding bind;
//
//    public VASFragment_fysiotests() {
//        mSeekBars = new SeekBar[N_SLIDERS];
//        mTextViews = new TextView[N_SLIDERS];
//        mPlusBtn = new ImageButton[N_SLIDERS];
//        mMinusBtn = new ImageButton[N_SLIDERS];
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Test URI
//        mTestUri = Uri.parse(getArguments().getString(EXTRAS.KEY_URI));
//
//        // Tab (IN or OUT)
//        mTab = getArguments().getInt(EXTRAS.KEY_TAB);
//
//        // IN or OUT selected at TestListActivity
//        mInOut = getArguments().getInt(EXTRAS.KEY_INOUT);
//
//        // Binding instead of findViewById
//        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_vas, container, false);
//
//
////        // Testing slow down in activity layout inflater
////        for (int i = 0; i < 60000000; i++) {
////            // Seek Bars
////            mSeekBars[0] = bind.seekbarKondition;
////            mSeekBars[1] = bind.seekbarSmarta;
////            mSeekBars[2] = bind.seekbarStelhet;
////            mSeekBars[3] = bind.seekbarTrotthet;
////        }
////        // -----------------------------------------------------------------------------------------------------------
//
//        // IN or OUT background color adjustments
//        if (mTab == Test.IN) {
//            bind.scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bgIn));
//            for (SeekBar sb : mSeekBars) {
//                sb.setProgressDrawable(
//                        ContextCompat.getDrawable(getActivity(), R.drawable.seek_bar_progress_in));
//            }
//
//        } else {
//            bind.scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bgOut));
//            for (SeekBar sb : mSeekBars) {
//                sb.setProgressDrawable(
//                        ContextCompat.getDrawable(getActivity(), R.drawable.seek_bar_progress_out));
//            }
//        }
//
//        // Disable touch events in the 'other' tab
//        if ((mTab == Test.IN && mInOut == 1) || (mTab == Test.OUT && mInOut == 0)) {
//            disableTouchOnLayout(bind.scrollView);
//        }
//
////        // Layout background listener closes soft keyboard
////        bind.vasLayoutBackground.setOnTouchListener(new View.OnTouchListener() {
////            @Override
////            public boolean onTouch(View v, MotionEvent event) {
////                // Hide soft keyboard
////                InputMethodManager imm = (InputMethodManager) getActivity()
////                        .getSystemService(Context.INPUT_METHOD_SERVICE);
////                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
////                return false;
////            }
////        });
//
//
//        // Plus and minus buttons
//        mMinusBtn[0] = bind.btnMinus1;
//        mMinusBtn[1] = bind.btnMinus2;
//        mMinusBtn[2] = bind.btnMinus3;
//        mMinusBtn[3] = bind.btnMinus4;
//
//        mPlusBtn[0] = bind.btnPlus1;
//        mPlusBtn[1] = bind.btnPlus2;
//        mPlusBtn[2] = bind.btnPlus3;
//        mPlusBtn[3] = bind.btnPlus4;
//
//        // Listeners
//        for (int i = 0; i < N_SLIDERS; i++) {
//            mMinusBtn[i].setOnClickListener(this);
//            mPlusBtn[i].setOnClickListener(this);
//        }
//
//        mTextViews[0] = bind.tvKonditionValue;
//        mTextViews[1] = bind.tvSmartaValue;
//        mTextViews[2] = bind.tvStelhetValue;
//        mTextViews[3] = bind.tvTrotthetValue;
//
//        /**
//         * Listeners
//         */
//        for (int i = 0; i < mSeekBars.length; i++) {
//            mTextViews[i].setText(String.valueOf(mSeekBars[i].getProgress()));
//            final int index = i;
//            mSeekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    mTextViews[index].setText(String.valueOf(progress));
//                    // Inform parent activity that form is outdated
//                    ((TestActivity) getActivity()).setUserHasSaved(false);
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//                }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//                }
//            });
//        }
//
//        // Done button
//        bind.btnDone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveToDatabase();
//                // Inform parent activity
//                ((TestActivity) getActivity()).setUserHasSaved(true);
//                // Show dialog
//                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
//                dialog.setMessage(getResources().getString(R.string.test_saved_complete));
//                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Do nothing
//                    }
//                });
//                dialog.show();
//            }
//        });
//
//        // Get content from either saved instance OR database
//        String contentStr;
//        if (savedInstanceState != null) {
//            // onRestoreInstanceState
//            contentStr = savedInstanceState.getString(STATE_CONTENT);
//            Log.d(LOG_TAG, "Content from savedInstance: " + contentStr);
//        } else {
//            // Read test content from database
//            Cursor cursor = getActivity().getContentResolver().query(mTestUri, null, null, null, null);
//            // Early exit: should never happen
//            if (cursor == null || cursor.getCount() == 0) {
//                return bind.getRoot();  // layout's view
//            }
//            cursor.moveToFirst();
//            if (mTab == Test.IN) {
//                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
//            } else {
//                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
//            }
//
//            cursor.close();
//            Log.d(LOG_TAG, "Content from database: " + contentStr);
//        }
//
//        // Content can be null. Database 'content_in' and 'content_out' are null when first created
//        if (contentStr != null) {
//            // Update sliders
//            String[] content = contentStr.split("\\|");
//            for (int i = 0; i < N_SLIDERS; i++) {
//                mSeekBars[i].setProgress(Integer.parseInt(content[i]));
//            }
//        }
//
//        // Inform parent activity that form is up to date
//        ((TestActivity) getActivity()).setUserHasSaved(true);
//
//        // Inflate the layout for this fragment
//        return bind.getRoot();  // layout's view
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        // Save state for radio groups and total sum
//        String content = generateContent();
//        outState.putString(STATE_CONTENT, content);
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(outState);
//    }
//
//    /**
//     * Generates a string representing the state of all components in the test
//     *
//     * @return String content representing state of views in layout
//     */
//    private String generateContent() {
//        // Create content
//        StringBuilder builder = new StringBuilder();
//        for (SeekBar slider : mSeekBars) {
//            builder.append(String.valueOf(slider.getProgress()));
//            builder.append("|");
//        }
//
//        return builder.toString();
//    }
//
//    public boolean saveToDatabase() {
//        ContentValues values = new ContentValues();
//        if (mTab == Test.IN) {
//            String content = generateContent();
//            values.put(TestEntry.COLUMN_CONTENT_IN, content);
//            values.put(TestEntry.COLUMN_RESULT_IN, content);
//            values.put(TestEntry.COLUMN_STATUS_IN, Test.COMPLETED);
//        } else {
//            String content = generateContent();
//            values.put(TestEntry.COLUMN_CONTENT_OUT, content);
//            values.put(TestEntry.COLUMN_RESULT_OUT, content);
//            values.put(TestEntry.COLUMN_STATUS_OUT, Test.COMPLETED);
//        }
//
//        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
//        Log.d(LOG_TAG, "rows updated: " + rows);
//
//        return true;
//    }
//
//    /**
//     * Plus and minus buttons
//     */
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            // Minus buttons
//            case R.id.btn_minus_1h: {
//                int progress = mSeekBars[0].getProgress();
//                if (progress > 0)
//                    mSeekBars[0].setProgress(progress - 1);
//                break;
//            }
//            case R.id.btn_minus_2: {
//                int progress = mSeekBars[1].getProgress();
//                if (progress > 0)
//                    mSeekBars[1].setProgress(progress - 1);
//                break;
//            }
//            case R.id.btn_minus_3: {
//                int progress = mSeekBars[2].getProgress();
//                if (progress > 0)
//                    mSeekBars[2].setProgress(progress - 1);
//                break;
//            }
//            case R.id.btn_minus_4: {
//                int progress = mSeekBars[3].getProgress();
//                if (progress > 0)
//                    mSeekBars[3].setProgress(progress - 1);
//                break;
//            }
//
//            // Plus buttons
//            case R.id.btn_plus_1h: {
//                int progress = mSeekBars[0].getProgress();
//                if (progress < 100)
//                    mSeekBars[0].setProgress(progress + 1);
//                break;
//            }
//            case R.id.btn_plus_2: {
//                int progress = mSeekBars[1].getProgress();
//                if (progress < 100)
//                    mSeekBars[1].setProgress(progress + 1);
//                break;
//            }
//            case R.id.btn_plus_3: {
//                int progress = mSeekBars[2].getProgress();
//                if (progress < 100)
//                    mSeekBars[2].setProgress(progress + 1);
//                break;
//            }
//            case R.id.btn_plus_4: {
//                int progress = mSeekBars[3].getProgress();
//                if (progress < 100)
//                    mSeekBars[3].setProgress(progress + 1);
//                break;
//            }
//        }
//    }
//
//    /**
//     * Disable all views in a given layout
//     */
//    private void disableTouchOnLayout(ViewGroup vg) {
//        for (int i = 0; i < vg.getChildCount(); i++) {
//            View child = vg.getChildAt(i);
//            child.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    return true;
//                }
//            });
//
//            if (child instanceof ViewGroup) {
//                disableTouchOnLayout((ViewGroup) child);
//            }
//        }
//    }
//
//
//}
