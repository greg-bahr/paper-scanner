package com.example.paperscanner;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.camera.PaperScanningFragment;
import com.example.paperscanner.scan_history.ScanHistoryFragment;

public class MainActivity extends AppCompatActivity implements ScanHistoryFragment.OnCameraFabClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ScanHistoryFragment scanHistoryFragment = new ScanHistoryFragment();
        ft.add(R.id.fragment_container, scanHistoryFragment);
        ft.commit();
    }


    @Override
    public void onCameraFabClick(View v) {
        // Replace ScanHistoryFragment with PaperScanningFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new PaperScanningFragment());
        ft.addToBackStack(null);
        ft.commit();
    }
}