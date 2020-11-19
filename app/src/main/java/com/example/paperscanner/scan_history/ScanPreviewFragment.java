package com.example.paperscanner.scan_history;

import android.content.ContentUris;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;

public class ScanPreviewFragment extends Fragment {

    private static final String TAG = "ScanPreviewFragment";

    private RecyclerView recyclerView;
    private PdfRenderer pdfRenderer;
    private Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_preview, container, false);

        Bundle bundle = this.getArguments();
        String filename = bundle.getString("filename");
        uri = bundle.getParcelable("uri");
        bundle.clear();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView = view.findViewById(R.id.scan_preview_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        ImageButton backButton = view.findViewById(R.id.scan_preview_back_button);
        backButton.setOnClickListener(v -> this.getFragmentManager().popBackStackImmediate());

        TextView titleTextView = view.findViewById(R.id.scan_preview_title_textview);
        titleTextView.setText(filename);

        ImageButton deleteButton = view.findViewById(R.id.scan_preview_delete_button);
        deleteButton.setOnClickListener(v -> {
            Uri contentUri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.MIME_TYPE + "=? AND " + MediaStore.MediaColumns._ID + "=?";
            String[] selectionArgs = new String[]{Environment.DIRECTORY_DOCUMENTS + "/PaperScanner/", "application/pdf", Long.toString(ContentUris.parseId(this.uri))};

            this.getContext().getContentResolver().delete(contentUri, selection, selectionArgs);

            this.getFragmentManager().popBackStackImmediate();
        });

        ImageButton shareButton = view.findViewById(R.id.scan_preview_share_button);
        shareButton.setOnClickListener(v -> {
            if (this.getActivity() != null) {
                ShareCompat.IntentBuilder.from(this.getActivity())
                        .setStream(this.uri)
                        .setText(filename)
                        .setType("application/pdf")
                        .startChooser();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().hide();

        try {
            ParcelFileDescriptor parcelFileDescriptor = this.getContext().getContentResolver().openFileDescriptor(this.uri, "r");
            this.pdfRenderer = new PdfRenderer(parcelFileDescriptor);

            ScanPreviewRecyclerViewAdapter adapter = new ScanPreviewRecyclerViewAdapter(this.pdfRenderer);
            this.recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Exception ", e);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.pdfRenderer != null) {
            this.pdfRenderer.close();
        }
    }
}