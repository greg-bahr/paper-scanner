package com.example.paperscanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.camera.PaperScanningFragment;
import com.example.paperscanner.scan_history.ScanHistoryFragment;

public class MainActivity extends AppCompatActivity implements ScanHistoryFragment.OnCameraFabClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ScanHistoryFragment scanHistoryFragment = new ScanHistoryFragment();
        ft.add(R.id.fragment_container, scanHistoryFragment);
        ft.commit();

        boolean permissionGranted = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onCameraFabClick(View v) {
        // Replace ScanHistoryFragment with PaperScanningFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new PaperScanningFragment());
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
                finish();
            }
        }
    }
}