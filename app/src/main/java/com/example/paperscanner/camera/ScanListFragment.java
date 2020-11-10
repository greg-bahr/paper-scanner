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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;

public class ScanListFragment extends Fragment implements RenamePdfDialogFragment.RenamePdfDialogListener {

    private static final String TAG = "ScanListFragment";

    TextView fileNameText;
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

        String pdfTitle = getArguments().getString("title");

        ImageButton cancelButton = view.findViewById(R.id.cancel_scan_button);
        cancelButton.setOnClickListener(v -> scanListFragmentListener.onCancelScan());

        ImageButton addPageButton = view.findViewById(R.id.add_page_button);
        addPageButton.setOnClickListener(v -> scanListFragmentListener.onAddPage());

        ImageButton submitPdfButton = view.findViewById(R.id.submit_pdf_button);
        submitPdfButton.setOnClickListener(v -> scanListFragmentListener.onSubmitPdf());

        fileNameText = view.findViewById(R.id.pdf_title_textview);
        fileNameText.setText(pdfTitle);
        fileNameText.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("title", fileNameText.getText().toString());

                RenamePdfDialogFragment fragment = new RenamePdfDialogFragment();
                fragment.setArguments(bundle);
                fragment.setTargetFragment(ScanListFragment.this, 0);
                fragment.show(getFragmentManager(), "rename");
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.scanned_image_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        ScanListRecyclerViewAdapter adapter = new ScanListRecyclerViewAdapter(getArguments().getStringArrayList("images"));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDialogEditName(String name) {
        fileNameText.setText(name);
        scanListFragmentListener.onTitleChange(name);
    }

    public interface ScanListFragmentListener {
        void onCancelScan();

        void onTitleChange(String name);

        void onAddPage();

        void onSubmitPdf();
    }
}