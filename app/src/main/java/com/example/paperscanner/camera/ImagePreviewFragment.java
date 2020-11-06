package com.example.paperscanner.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.paperscanner.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewFragment extends Fragment {

    private static final String TAG = "ImagePreviewFragment";
    private static final int DOWNSAMPLE_COUNT = 3;
    private static final boolean DEBUG = false;

    private Bitmap previewImage;

    Mat image;

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
            byte[] imageBytes = bundle.getByteArray("image");
            bundle.clear();

            image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
            Core.flip(image.t(), image, 1);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

            MatOfPoint2f paper = detectPaper(image);
            warpImage(image, paper);
            sharpenImage(image);

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

    private MatOfPoint2f detectPaper(Mat image) {
        Mat downsampled = image.clone();
        Imgproc.cvtColor(downsampled, downsampled, Imgproc.COLOR_RGB2GRAY);

        for (int i = 0; i < DOWNSAMPLE_COUNT; i++) {
            Imgproc.pyrDown(downsampled, downsampled);
        }

        Mat canny = new Mat();
        Imgproc.GaussianBlur(downsampled, downsampled, new Size(5, 5), 1);
        Imgproc.adaptiveThreshold(downsampled, downsampled, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Photo.fastNlMeansDenoising(downsampled, downsampled, 7, 21, 10);

        Imgproc.Canny(downsampled, canny, 50, 150, 5);
        downsampled.release();

        Mat hierarchy = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();
        canny.release();

        List<MatOfPoint> hullList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);

            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIndexList = hull.toList();
            for (int i = 0; i < hullContourIndexList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIndexList.get(i)];
            }

            hullList.add(new MatOfPoint(hullPoints));
        }

        double largestContourArea = Imgproc.contourArea(hullList.get(0));
        for (MatOfPoint contour : hullList) {
            double area = Imgproc.contourArea(contour);
            if (area > largestContourArea) {
                largestContourArea = area;
            }
        }

        ArrayList<MatOfPoint2f> filteredContours = new ArrayList<>();
        ArrayList<MatOfPoint> drawableContour = new ArrayList<>();
        for (MatOfPoint contour : hullList) {
            MatOfPoint2f floatContour = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approximatedContour = new MatOfPoint2f();

            double epsilon = 0.03 * Imgproc.arcLength(floatContour, true);
            Imgproc.approxPolyDP(floatContour, approximatedContour, epsilon, true);

            if (approximatedContour.total() == 4 && Imgproc.contourArea(approximatedContour) > largestContourArea * 0.5) {
                Core.multiply(approximatedContour, new Scalar(Math.pow(2, DOWNSAMPLE_COUNT), Math.pow(2, DOWNSAMPLE_COUNT)), approximatedContour);
                filteredContours.add(approximatedContour);

                if (DEBUG) {
                    Core.multiply(contour, new Scalar(Math.pow(2, DOWNSAMPLE_COUNT), Math.pow(2, DOWNSAMPLE_COUNT)), contour);
                    drawableContour.add(contour);
                }
            }
        }

        double bestRatio = Double.MAX_VALUE;
        int bestRectangleIndex = -1;
        MatOfPoint2f bestRectangle = null;
        for (int i = 0; i < filteredContours.size(); i++) {
            Point[] points = sortPoints(filteredContours.get(i).toArray());

            double width = distance(points[0], points[1]);
            double height = distance(points[0], points[3]);

            double ratio = width / height;

            if (Math.abs(ratio - 0.7) < Math.abs(bestRatio - 0.7)) {
                bestRatio = ratio;
                bestRectangle = filteredContours.get(i);
                bestRectangleIndex = i;
            }
        }

        if (DEBUG) {
            Imgproc.drawContours(image, drawableContour, bestRectangleIndex, new Scalar(0, 255, 0), 5);
        }

        if (bestRectangle == null) {
            Log.i(TAG, "No paper detected! Defaulting to null.");
            return null;
        } else {
            return bestRectangle;
        }
    }

    private void warpImage(Mat image, MatOfPoint2f paper) {
        if (paper == null) {
            return;
        }

        Point[] points = sortPoints(paper.toArray());

        double width = Math.max(distance(points[0], points[1]), distance(points[2], points[3]));
        double height = Math.max(distance(points[0], points[3]), distance(points[1], points[2]));

        Point[] destinationPoints = new Point[]{
                new Point(0, 0),
                new Point(width, 0),
                new Point(width, height),
                new Point(0, height)
        };

        MatOfPoint2f corners = new MatOfPoint2f(points);
        MatOfPoint2f destination = new MatOfPoint2f(destinationPoints);

        Mat transform = Imgproc.getPerspectiveTransform(corners, destination);
        Imgproc.warpPerspective(image, image, transform, new Size(width, height));
        transform.release();
    }

    private void sharpenImage(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(image, image, 5);
        Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        Imgproc.erode(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private Point[] sortPoints(Point[] points) {
        List<Double> sums = new ArrayList<>();
        for (Point point : points) {
            sums.add(point.x + point.y);
        }

        double minSum = Double.MAX_VALUE;
        int minSumIndex = -1;
        double maxSum = Double.MIN_VALUE;
        int maxSumIndex = -1;
        for (int i = 0; i < sums.size(); i++) {
            if (sums.get(i) > maxSum) {
                maxSum = sums.get(i);
                maxSumIndex = i;
            }
            if (sums.get(i) < minSum)  {
                minSum = sums.get(i);
                minSumIndex = i;
            }
        }


        List<Double> diffs = new ArrayList<>();
        for (Point point : points) {
            diffs.add(point.x - point.y);
        }

        double minDiff = Double.MAX_VALUE;
        int minDiffIndex = -1;
        double maxDiff = Double.MIN_VALUE;
        int maxDiffIndex = -1;
        for (int i = 0; i < diffs.size(); i++) {
            if (diffs.get(i) > maxDiff) {
                maxDiff = diffs.get(i);
                maxDiffIndex = i;
            }
            if (diffs.get(i) < minDiff)  {
                minDiff = diffs.get(i);
                minDiffIndex = i;
            }
        }

        return new Point[] {
            points[minSumIndex],
            points[maxDiffIndex],
            points[maxSumIndex],
            points[minDiffIndex]
        };
    }
}