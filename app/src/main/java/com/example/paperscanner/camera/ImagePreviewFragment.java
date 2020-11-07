package com.example.paperscanner.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;

public class ImagePreviewFragment extends Fragment {

    private static final String TAG = "ImagePreviewFragment";

    private Bitmap previewImage;
    private OnImageSubmitListener onImageSubmitListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            onImageSubmitListener = (OnImageSubmitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnImageSubmitListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview, container, false);

        ImageView imageView = view.findViewById(R.id.captured_image_preview);
        ImageButton submitImageButton = view.findViewById(R.id.submit_image_button);
        ImageButton backButton = view.findViewById(R.id.close_image_button);

        backButton.setOnClickListener(v -> this.getFragmentManager().popBackStackImmediate());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            previewImage = bundle.getParcelable("image");
            bundle.clear();

            imageView.setImageBitmap(previewImage);
            submitImageButton.setOnClickListener(v -> onImageSubmitListener.onImageSubmit(previewImage));
        } else {
            Log.e(TAG, "No image to preview!");
            this.getFragmentManager().popBackStackImmediate();
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        previewImage.recycle();
    }


    public interface OnImageSubmitListener {
        void onImageSubmit(Bitmap image);
    }
}