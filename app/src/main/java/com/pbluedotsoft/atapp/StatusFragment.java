package com.pbluedotsoft.atapp;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentStatusBinding;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;



/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment implements TextWatcher {

    private static final String LOG_TAG = StatusFragment.class.getSimpleName();
    private static int N_QUESTIONS = 23;

    // Save state constant
    private static final String STATE_CONTENT = "state_content";
    private static final String STATE_HIGH_ON = "state_high_on";

    private Uri mTestUri;
    private int mTab, mInOut;
    private boolean mHighlightsON;

    private TextView[] mTv;
    private EditText[][] etValue;
    private boolean[] mMissingAnswer;

    private FragmentStatusBinding bind;

    public StatusFragment() {
        mTv = new TextView[N_QUESTIONS];
        etValue = new EditText[N_QUESTIONS][2]; // right and left per question
        mMissingAnswer = new boolean[N_QUESTIONS];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Test URI
        mTestUri = Uri.parse(getArguments().getString(EXTRAS.KEY_URI));

        // Tab (IN or OUT)
        mTab = getArguments().getInt(EXTRAS.KEY_TAB);

        // IN or OUT selected at TestListActivity
        mInOut = getArguments().getInt(EXTRAS.KEY_INOUT);

        // Binding instead of findViewById
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);

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

        bindingViews();

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save to database: return false if test incomplete
                if (!saveToDatabase()) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_incomplete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "SHOW ME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mHighlightsON = true;
                            highlightQuestions();
                        }
                    });
                    dialog.show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_complete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            highlightQuestions(); // clear  highlights
                        }
                    });
                    dialog.show();
                }

                // Inform parent activity
                ((TestActivity) getActivity()).setUserHasSaved(true);
            }
        });

        // Get content from either saved instance OR database
        String contentStr;
        if (savedInstanceState != null) {
            // onRestoreInstanceState
            contentStr = savedInstanceState.getString(STATE_CONTENT);
            mHighlightsON = savedInstanceState.getBoolean(STATE_HIGH_ON);
            Log.d(LOG_TAG, "Content from savedInstance: " + contentStr);
        } else {
            // Read test content from database
            Cursor cursor = getActivity().getContentResolver().query(mTestUri, null, null, null, null);
            // Early exit: should never happen
            if (cursor == null || cursor.getCount() == 0) {
                return bind.getRoot();
            }
            cursor.moveToFirst();
            if (mTab == Test.IN) {
                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
            } else {
                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
            }

            cursor.close();
            Log.d(LOG_TAG, "Content from database: " + contentStr);
        }

        // Content can be null. Database 'content_in' and 'content_out' are null when first created
        if (contentStr != null) {
            // Fill edit text views
            String[] content = contentStr.split("\\|");
            Log.d(LOG_TAG, "content length: " + content.length);
            int index = 0;
            for (int i = 0; i < N_QUESTIONS; i++) {
                    etValue[i][0].setText(content[index++]);
                    etValue[i][1].setText(content[index++]);
            }
        }

        if (mHighlightsON) {
            highlightQuestions();
        }

        // Inform parent activity that form is up to date
        ((TestActivity) getActivity()).setUserHasSaved(true);

        return bind.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state for radio groups and total sum
        String content = generateContent();
        outState.putString(STATE_CONTENT, content);
        outState.putBoolean(STATE_HIGH_ON, mHighlightsON);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    /**
     * Checks whether all edit text have been answered and fills the array mMissingAnswer
     *
     * @return false - test is complete
     */
    private boolean checkMissingAnswers() {
        boolean missing = false;
        for (int i = 0; i < N_QUESTIONS; i++) {
            if (etValue[i][0].getText().toString().isEmpty() ||
                    etValue[i][1].getText().toString().isEmpty()) {
                missing = true;
                mMissingAnswer[i] = true;
            }
        }

        return missing;
    }

    /**
     * Save state for all edit texts
     *
     * @return String representing state for radio groups in layout
     */
    private String generateContent() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < N_QUESTIONS; i++) {
            builder.append(etValue[i][0].getText().toString());
            builder.append("|");
            builder.append(etValue[i][1].getText().toString());
            builder.append("|");
        }

        builder.append("|0|");  // fixes bug when edit fields are empty

        Log.d(LOG_TAG, "generated content: " + builder.toString());

        return builder.toString();
    }

    /**
     * Saves content
     *
     * @return true if test is complete, false if there are answers mMissingAnswer
     */
    public boolean saveToDatabase() {
        // Check whether all spinners have been answered
        boolean missingAnswers = checkMissingAnswers();
        int status;
        if (missingAnswers) {
            status = Test.INCOMPLETED;
        } else {
            status = Test.COMPLETED;
        }

        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_STATUS_IN, status);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_STATUS_OUT, status);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

        return !missingAnswers;
    }

    /**
     * Highlights unanswered question
     */
    private void highlightQuestions() {
        for (int i = 0; i < N_QUESTIONS; i++) {
            if (mHighlightsON &&
                    (etValue[i][0].getText().toString().isEmpty() || etValue[i][1].getText().toString().isEmpty())) {
                mTv[i].setTextColor(ContextCompat.getColor(getContext(), R.color.highlight));
            } else {
                mTv[i].setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
            }
        }
    }

    private void bindingViews() {
        // Text views
        mTv[0] = bind.tvQ0;
        mTv[1] = bind.tvQ1;
        mTv[2] = bind.tvQ2;
        mTv[3] = bind.tvQ3;
        mTv[4] = bind.tvQ4;
        mTv[5] = bind.tvQ5;
        mTv[6] = bind.tvQ6;
        mTv[7] = bind.tvQ7;
        mTv[8] = bind.tvQ8;
        mTv[9] = bind.tvQ9;
        mTv[10] = bind.tvQ10;
        mTv[11] = bind.tvQ11;
        mTv[12] = bind.tvQ12;
        mTv[13] = bind.tvQ13;
        mTv[14] = bind.tvQ14;
        mTv[15] = bind.tvQ15;
        mTv[16] = bind.tvQ16;
        mTv[17] = bind.tvQ17;
        mTv[18] = bind.tvQ18;
        mTv[19] = bind.tvQ19;
        mTv[20] = bind.tvQ20;
        mTv[21] = bind.tvQ21;
        mTv[22] = bind.tvQ22;

        // Edit text
        etValue[0][0] = bind.etSupinationH;
        etValue[0][1] = bind.etSupinationV;
        etValue[1][0] = bind.etPronationH;
        etValue[1][1] = bind.etPronationV;
        etValue[2][0] = bind.etDorsal1H;
        etValue[2][1] = bind.etDorsal1V;
        etValue[3][0] = bind.etDorsal2H;
        etValue[3][1] = bind.etDorsal2V;
        etValue[4][0] = bind.etVolarH;
        etValue[4][1] = bind.etVolarV;
        etValue[5][0] = bind.etDigH;
        etValue[5][1] = bind.etDigV;
        etValue[6][0] = bind.etOpen1H;
        etValue[6][1] = bind.etOpen1V;
        etValue[7][0] = bind.etOpen2H;
        etValue[7][1] = bind.etOpen2V;
        etValue[8][0] = bind.etOppositionH;
        etValue[8][1] = bind.etOppositionV;
        etValue[9][0] = bind.etKnytdiastasH;
        etValue[9][1] = bind.etKnytdiastasV;
        etValue[10][0] = bind.etExtensionH;
        etValue[10][1] = bind.etExtensionV;
        etValue[11][0] = bind.etOppDig2H;
        etValue[11][1] = bind.etOppDig2V;
        etValue[12][0] = bind.etOppDig3H;
        etValue[12][1] = bind.etOppDig3V;
        etValue[13][0] = bind.etOppDig4H;
        etValue[13][1] = bind.etOppDig4V;
        etValue[14][0] = bind.etOppDig5H;
        etValue[14][1] = bind.etOppDig5V;
        etValue[15][0] = bind.etKnytDig2H;
        etValue[15][1] = bind.etKnytDig2V;
        etValue[16][0] = bind.etKnytDig3H;
        etValue[16][1] = bind.etKnytDig3V;
        etValue[17][0] = bind.etKnytDig4H;
        etValue[17][1] = bind.etKnytDig4V;
        etValue[18][0] = bind.etKnytDig5H;
        etValue[18][1] = bind.etKnytDig5V;
        etValue[19][0] = bind.etExtDig2H;
        etValue[19][1] = bind.etExtDig2V;
        etValue[20][0] = bind.etExtDig3H;
        etValue[20][1] = bind.etExtDig3V;
        etValue[21][0] = bind.etExtDig4H;
        etValue[21][1] = bind.etExtDig4V;
        etValue[22][0] = bind.etExtDig5H;
        etValue[22][1] = bind.etExtDig5V;

        // listeners
        for (int i = 0; i < N_QUESTIONS; i++) {
            etValue[i][0].addTextChangedListener(this);
            etValue[i][1].addTextChangedListener(this);
        }
    }

    /**
     * Disable all views in a given layout
     */
    private void disableTouchOnLayout(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setFocusable(false);  // needed for EditText
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

    /**
     * Interface method implementation (EditText)
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (((TestActivity) getActivity()).mUserInteracting) {
            // Inform parent activity
            ((TestActivity) getActivity()).setUserHasSaved(false);
            highlightQuestions();
        }
    }
}
