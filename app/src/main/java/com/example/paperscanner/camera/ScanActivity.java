package com.example.paperscanner.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.MainActivity;
import com.example.paperscanner.R;

import org.opencv.android.OpenCVLoader;

public class ScanActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, PaperScanningFragment.OnImageCaptureListener {
    private final int PERMISSION_REQUEST_CODE = 1;
    private final String TAG = "ScanActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().hide();
        }

        boolean permissionGranted = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        PaperScanningFragment paperScanningFragment = new PaperScanningFragment();
        ft.add(R.id.scan_fragment_container, paperScanningFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Failed to load OpenCV!");
        }
    }


    @Override
    public void OnImageCapture(byte[] imageBytes) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("image", imageBytes);

        ImagePreviewFragment fragment = new ImagePreviewFragment();
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.scan_fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean permissionGranted = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

            if (!permissionGranted) {
                Toast.makeText(this, "You need camera permissions to use the app.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}