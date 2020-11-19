package com.example.paperscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.camera.ScanActivity;
import com.example.paperscanner.scan_history.ScanHistoryFragment;
import com.example.paperscanner.scan_history.ScanPreviewFragment;

public class MainActivity extends AppCompatActivity implements ScanHistoryFragment.ScanHistoryFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ScanHistoryFragment scanHistoryFragment = new ScanHistoryFragment();
        ft.add(R.id.scanhistory_fragment_container, scanHistoryFragment);
        ft.commit();
    }

    @Override
    public void onCameraFabClick() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPdfClick(String filename, Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
        bundle.putParcelable("uri", uri);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ScanPreviewFragment scanPreviewFragment = new ScanPreviewFragment();
        scanPreviewFragment.setArguments(bundle);
        ft.replace(R.id.scanhistory_fragment_container, scanPreviewFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}