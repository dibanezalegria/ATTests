package com.pbluedotsoft.atapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.databinding.FragmentEditPatientDialogBinding;

public class EditPatientDialogFragment extends DialogFragment {

    private static final String LOG_TAG = EditPatientDialogFragment.class.getSimpleName();

    // Return data back to the activity through the implemented listener
    private EditPatientDialogListener mListener;

    private int mPatientID;

    /**
     * Interface implemented by MainActivity
     */
    public interface EditPatientDialogListener {
        void onUpdateEditPatientDialog(int id, String name, int entry);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (EditPatientDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement EditPatientDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_edit_patient_dialog, null, false);

        final FragmentEditPatientDialogBinding bind = FragmentEditPatientDialogBinding.bind(view);

        // Fill actual name and entry number in the text views
        if (getArguments() != null) {
            bind.etName.setText(getArguments().getString(EXTRAS.KEY_PATIENT_NAME));
            bind.etEntry.setText(getArguments().getString(EXTRAS.KEY_PATIENT_ENTRY));
            mPatientID = getArguments().getInt(EXTRAS.KEY_PATIENT_ID);
        } else {
            Log.d(LOG_TAG, "Error onCreateDialog: missing arguments");
            mPatientID = -1;
        }

        // Inflate and set the layout for the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameStr = bind.etName.getText().toString().trim();
                        String entryStr = bind.etEntry.getText().toString().trim();
                        int entry = 0;
                        if (mPatientID != -1 && !nameStr.isEmpty()) {
                            try {
                                entry = Integer.parseInt(entryStr);
                            } catch (NumberFormatException e) {
                                Log.d(LOG_TAG, "NumberFormatException thrown");
                            }
                            // Call interface method implemented in PatientListActivity
                            mListener.onUpdateEditPatientDialog(mPatientID, nameStr, entry);
                            dismiss();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
