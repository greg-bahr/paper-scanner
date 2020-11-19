package com.example.paperscanner.scan_history;

import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

        ImageButton shareButton = view.findViewById(R.id.scan_preview_share_button);


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