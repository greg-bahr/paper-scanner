package com.example.paperscanner.scan_history;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScanHistoryFragment extends Fragment {
    OnCameraFabClickListener cameraFabClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            cameraFabClickListener = (OnCameraFabClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCameraFabClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_history, container, false);

        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraFabClickListener.onCameraFabClick(v);
            }
        });

        return view;
    }

    public interface OnCameraFabClickListener {
        void onCameraFabClick(View v);
    }

}