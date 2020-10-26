package com.example.paperscanner.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaperScanningFragment extends Fragment {

    private static final String TAG = "PaperScanningFragment";
    private FloatingActionButton captureImageButton;
    private PreviewView viewfinder;
    private ImageCapture imageCapture;
    private ExecutorService imageCaptureExecutor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paper_scanning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        captureImageButton = view.findViewById(R.id.capture_image_button);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        viewfinder = view.findViewById(R.id.viewfinder);
        viewfinder.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        startCamera();

        imageCaptureExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewfinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), CameraSelector.DEFAULT_BACK_CAMERA, preview);

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private void captureImage() {
        if (imageCapture == null) {
            return;
        }

        imageCapture.takePicture(imageCaptureExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                // TODO: Process image with opencv, then preview it in another fragment to potentially save it
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageCaptureExecutor.shutdown();
    }
}