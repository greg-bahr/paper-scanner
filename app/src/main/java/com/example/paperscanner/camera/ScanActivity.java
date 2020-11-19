package com.example.paperscanner.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.paperscanner.MainActivity;
import com.example.paperscanner.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, ImageCaptureFragment.OnImageCaptureListener, ImagePreviewFragment.OnImageSubmitListener, ScanListFragment.ScanListFragmentListener {
    private final int PERMISSION_REQUEST_CODE = 1;

    private final String TAG = "ScanActivity";

    Bundle images;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        this.deleteTempImages();

        LocalDateTime localDateTime = LocalDateTime.now();

        this.images = new Bundle();
        this.images.putString("title", "PaperScanner_" + localDateTime.toString());
        this.images.putStringArrayList("images", new ArrayList<>());

        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().hide();
        }

        boolean permissionGranted = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ImageCaptureFragment imageCaptureFragment = new ImageCaptureFragment();
        ft.add(R.id.scan_fragment_container, imageCaptureFragment);
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
    protected void onDestroy() {
        super.onDestroy();

        this.images.clear();
        this.deleteTempImages();
    }

    private void deleteTempImages() {
        String[] files = this.fileList();
        for (String path : files) {
            if (path.contains(".png")) {
                try {
                    File file = new File(this.getFilesDir(), path);
                    file.delete();
                } catch (Exception e) {
                    Log.e(TAG, "Exception: ", e);
                }
            }
        }
    }

    @Override
    public void onImageCapture(byte[] imageBytes) {
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
        Core.flip(image.t(), image, 1);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        MatOfPoint2f paper = ImageProcessor.detectPaper(image);
        ImageProcessor.warpImage(image, paper);
        ImageProcessor.sharpenImage(image);

        Bundle bundle = new Bundle();
        bundle.putParcelable("image", ImageProcessor.matToBitmap(image));

        ImagePreviewFragment fragment = new ImagePreviewFragment();
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.scan_fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.scan_fragment_container);
        if (fragment instanceof ScanListFragment) {
            List<String> paths = this.images.getStringArrayList("images");
            String path = paths.get(paths.size() - 1);

            File file = new File(path);
            if (file.delete()) {
                paths.remove(paths.size() - 1);
            }
        }
    }

    @Override
    public void onImageSubmit(Bitmap image) {
        int currentImage = this.images.getStringArrayList("images").size();
        File file = new File(this.getFilesDir(), currentImage + ".png");

        this.images.putParcelable("lastImage", image);
        this.images.getStringArrayList("images").add(file.getAbsolutePath());

        new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                Log.e(TAG, "Exception: ", e);
            }
        }).start();

        ScanListFragment fragment = new ScanListFragment();
        fragment.setArguments(this.images);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.scan_fragment_container, fragment);
        ft.commit();
    }

    @Override
    public void onCancelScan() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTitleChange(String name) {
        this.images.putString("title", name);
    }

    @Override
    public void onAddPage() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ImageCaptureFragment imageCaptureFragment = new ImageCaptureFragment();
        ft.add(R.id.scan_fragment_container, imageCaptureFragment);
        ft.commit();
    }

    @Override
    public void onSubmitPdf() {
        String name = this.images.getString("title");
        if (!name.contains(".pdf")) {
            name += ".pdf";
        }

        List<String> paths = this.images.getStringArrayList("images");
        PdfDocument document = new PdfDocument();

        for (int i = 0; i < paths.size(); i++) {
            Bitmap bmp = BitmapFactory.decodeFile(paths.get(i));
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bmp.getWidth(), bmp.getHeight(), i + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            canvas.drawBitmap(bmp, 0, 0, null);
            document.finishPage(page);
        }

        ContentResolver resolver = this.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "PaperScanner");

        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

        try (OutputStream os = this.getContentResolver().openOutputStream(uri)) {
            document.writeTo(os);
            document.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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