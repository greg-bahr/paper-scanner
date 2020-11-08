package com.example.paperscanner.camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.paperscanner.R;

public class RenamePdfDialogFragment extends DialogFragment {

    RenamePdfDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (RenamePdfDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RenamePdfDialogListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pdf_rename, null);

        EditText editText = view.findViewById(R.id.pdf_title_edit);
        editText.setText(this.getArguments().getString("title"));

        builder.setView(view)
                .setTitle("Rename PDF")
                .setPositiveButton("Ok", (dialog, which) -> {
                    listener.onDialogEditName(editText.getText().toString());
                });

        return builder.create();
    }

    public interface RenamePdfDialogListener {
        void onDialogEditName(String name);
    }
}
