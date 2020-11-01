package com.example.paperscanner.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImagePreviewFragment extends Fragment {

    private static final String TAG = "ImagePreviewFragment";

    private ImageView imageView;
    private Bitmap previewImage;

    Mat image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview, container, false);

        imageView = view.findViewById(R.id.captured_image_preview);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            byte[] imageBytes = bundle.getByteArray("image");

            image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
            Core.flip(image.t(), image, 1);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

            previewImage = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(image, previewImage);

            imageView.setImageBitmap(previewImage);

        } else {
            Log.e(TAG, "No image to preview!");
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        previewImage.recycle();
        image.release();
    }
}