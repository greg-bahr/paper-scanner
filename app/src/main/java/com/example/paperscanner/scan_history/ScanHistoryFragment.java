package com.example.paperscanner.scan_history;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScanHistoryFragment extends Fragment {
    private ScanHistoryFragmentListener scanHistoryFragmentListener;
    private RecyclerView recyclerView;
    private Cursor pdfs;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            scanHistoryFragmentListener = (ScanHistoryFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ScanHistoryFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_history, container, false);

        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(v -> scanHistoryFragmentListener.onCameraFabClick());

        recyclerView = view.findViewById(R.id.scan_history_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        this.recyclerView.setAdapter(null);
        this.pdfs.close();
    }

    @Override
    public void onResume() {
        super.onResume();

        Uri contentUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{Environment.DIRECTORY_DOCUMENTS + "/PaperScanner/", "application/pdf"};
        this.pdfs = this.getContext().getContentResolver().query(contentUri, null, selection, selectionArgs, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

        ScanHistoryRecyclerViewAdapter adapter = new ScanHistoryRecyclerViewAdapter(this.pdfs);
        adapter.setOnPdfClickListener((filename, uri) -> this.scanHistoryFragmentListener.onPdfClick(filename, uri));
        recyclerView.setAdapter(adapter);
    }

    public interface ScanHistoryFragmentListener {
        void onCameraFabClick();

        void onPdfClick(String filename, Uri uri);
    }

}