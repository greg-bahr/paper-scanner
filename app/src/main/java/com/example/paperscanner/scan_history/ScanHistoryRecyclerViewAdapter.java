package com.example.paperscanner.scan_history;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paperscanner.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScanHistoryRecyclerViewAdapter extends RecyclerView.Adapter<ScanHistoryRecyclerViewAdapter.ViewHolder> {

    private final Cursor pdfs;
    private OnPdfClickListener onPdfClickListener;

    public ScanHistoryRecyclerViewAdapter(Cursor pdfs) {
        this.pdfs = pdfs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_history_item, parent, false);

        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.pdfs.moveToPosition(position);

        String fileName = this.pdfs.getString(this.pdfs.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
        String date = this.pdfs.getString(this.pdfs.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
        long millis = Long.parseLong(date) * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        date = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.US).format(calendar.getTime());

        long id = this.pdfs.getLong(this.pdfs.getColumnIndex(MediaStore.MediaColumns._ID));
        Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);

        holder.itemView.setOnClickListener(v -> {
            if (this.onPdfClickListener != null) {
                this.onPdfClickListener.onPdfClick(fileName, uri);
            }
        });

        holder.title.setText(fileName);
        holder.date.setText(date);

        new Thread(() -> {
            try {
                ParcelFileDescriptor parcelFileDescriptor = holder.context.getContentResolver().openFileDescriptor(uri, "r");
                PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page firstPage = pdfRenderer.openPage(0);

                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, holder.context.getResources().getDisplayMetrics());
                Bitmap thumbnail = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
                firstPage.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                firstPage.close();
                pdfRenderer.close();

                holder.preview.post(() -> holder.preview.setImageBitmap(thumbnail));
            } catch (Exception e) {
                Log.e("ScanHistoryFragment", "Exception", e);
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return this.pdfs.getCount();
    }

    public void setOnPdfClickListener(OnPdfClickListener onPdfClickListener) {
        this.onPdfClickListener = onPdfClickListener;
        this.notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public TextView title, date;
        public ImageView preview;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            this.title = itemView.findViewById(R.id.scan_history_item_title);
            this.date = itemView.findViewById(R.id.scan_history_item_date);
            this.preview = itemView.findViewById(R.id.scan_history_item_preview);
            this.context = context;
        }
    }

    public interface OnPdfClickListener {
        void onPdfClick(String filename, Uri uri);
    }
}
