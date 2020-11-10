package com.example.paperscanner.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;

import java.util.List;

public class ScanListRecyclerViewAdapter extends RecyclerView.Adapter<ScanListRecyclerViewAdapter.ViewHolder> {
    private final List<String> imagePaths;
    private final Bitmap lastImage;

    public ScanListRecyclerViewAdapter(List<String> imagePaths, Bitmap lastImage) {
        this.imagePaths = imagePaths;
        this.lastImage = lastImage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bmp = position == this.getItemCount() - 1 && lastImage != null ? lastImage : BitmapFactory.decodeFile(imagePaths.get(position));

        holder.imageView.setImageBitmap(bmp);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageView = itemView.findViewById(R.id.scanned_image);
        }
    }
}
