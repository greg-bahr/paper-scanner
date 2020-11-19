package com.example.paperscanner.scan_history;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;

public class ScanPreviewRecyclerViewAdapter extends RecyclerView.Adapter<ScanPreviewRecyclerViewAdapter.ViewHolder> {

    private final PdfRenderer pdfRenderer;

    public ScanPreviewRecyclerViewAdapter(PdfRenderer pdfRenderer) {
        this.pdfRenderer = pdfRenderer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PdfRenderer.Page page = this.pdfRenderer.openPage(position);

        Bitmap image = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        holder.imageView.setImageBitmap(image);
    }

    @Override
    public int getItemCount() {
        return this.pdfRenderer.getPageCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageView = itemView.findViewById(R.id.scanned_image);
        }
    }
}
