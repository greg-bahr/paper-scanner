package com.example.paperscanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.camera.ScanActivity;
import com.example.paperscanner.scan_history.ScanHistoryFragment;

public class MainActivity extends AppCompatActivity implements ScanHistoryFragment.OnCameraFabClickListener {

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
}