package com.example.paperscanner.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ImagePreviewFragment extends Fragment {

    private static final String TAG = "ImagePreviewFragment";

    Mat image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            byte[] imageBytes = bundle.getByteArray("image");

            image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
        } else {
            Log.e(TAG, "No image to preview!");
        }

        return view;
    }
}