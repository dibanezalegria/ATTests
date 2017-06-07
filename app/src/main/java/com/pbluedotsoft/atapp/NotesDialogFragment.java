package com.pbluedotsoft.atapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.pbluedotsoft.atapp.databinding.FragmentDialogNotesBinding;

/**
 * Created by Daniel Ibanez on 2016-10-26.
 */

public class NotesDialogFragment extends DialogFragment {

    private String mNotesIn, mNotesOut;

    // Return data back to the activity through the implemented listener
    private NotesDialogListener mListener;

    /**
     * Listener interface
     */
    public interface NotesDialogListener {
        void onSaveNotesDialogFragment(String notesIn, String notesOut);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (NotesDialogFragment.NotesDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NotesDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public NotesDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use 'newInstance' instead as shown below
    }

    public static NotesDialogFragment newInstance(String notesIn, String notesOut) {
        NotesDialogFragment fragment = new NotesDialogFragment();
        if (notesIn != null) {
            fragment.setNotesIn(notesIn);
        } else {
            fragment.setNotesIn("");
        }

        if (notesOut != null) {
            fragment.setNotesOut(notesOut);
        } else {
            fragment.setNotesOut("");
        }

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = (getActivity().getLayoutInflater())
                .inflate(R.layout.fragment_dialog_notes, null);
        // Binding instead of findViewById
        final FragmentDialogNotesBinding bind = FragmentDialogNotesBinding.bind(view);

        // Edit text notes in
        bind.etNotesIn.setText(mNotesIn);
        // Place cursor at the end of the text
        bind.etNotesIn.setSelection(bind.etNotesIn.getText().length());

        // Edit text notes out
        bind.etNotesOut.setText(mNotesOut);
        // Place cursor at the end of the text
        bind.etNotesOut.setSelection(bind.etNotesOut.getText().length());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(view)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onSaveNotesDialogFragment(bind.etNotesIn.getText().toString(),
                                bind.etNotesOut.getText().toString());
                        dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        return dialogBuilder.create();
    }

    public void setNotesIn(String notesIn) {
        mNotesIn = notesIn;
    }

    public void setNotesOut(String notesOut) {
        mNotesOut = notesOut;
    }

}
