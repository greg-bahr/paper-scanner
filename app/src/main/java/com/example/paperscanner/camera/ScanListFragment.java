package com.example.paperscanner.camera;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;

public class ScanListFragment extends Fragment implements RenamePdfDialogFragment.RenamePdfDialogListener {

    private static final String TAG = "ScanListFragment";

    TextView fileNameText;
    private String pdfTitle;
    private ScanListFragmentListener scanListFragmentListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            scanListFragmentListener = (ScanListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ScanListFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_list, container, false);

        pdfTitle = getArguments().getString("title");

        ImageButton cancelButton = view.findViewById(R.id.cancel_scan_button);
        cancelButton.setOnClickListener(v -> scanListFragmentListener.onCancelScan());

        fileNameText = view.findViewById(R.id.pdf_title_textview);
        fileNameText.setText(pdfTitle);
        fileNameText.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("title", pdfTitle);

                RenamePdfDialogFragment fragment = new RenamePdfDialogFragment();
                fragment.setArguments(bundle);
                fragment.setTargetFragment(ScanListFragment.this, 0);
                fragment.show(getFragmentManager(), "rename");
            }
        });

        return view;
    }

    @Override
    public void onDialogEditName(String name) {
        pdfTitle = name;
        fileNameText.setText(pdfTitle);
        scanListFragmentListener.onTitleChange(name);
    }

    public interface ScanListFragmentListener {
        void onCancelScan();

        void onTitleChange(String name);
    }
}