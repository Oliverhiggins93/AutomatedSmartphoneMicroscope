package com.example.android.SmartMicroscope.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.android.SmartMicroscope.R;

import java.util.ArrayList;

public class InferenceImageAdapter extends ArrayAdapter<Bitmap> {
    public InferenceImageAdapter(Context context, ArrayList<Bitmap> bitmapArray) {
        super(context, 0, bitmapArray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Bitmap imageitem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_inferenceitem, parent, false);
        }
        // Lookup view for data population
        ImageView imageDisplay = (ImageView) convertView.findViewById(R.id.inferenceImageView);
        // Populate the data into the template view using the data object
        imageDisplay.setImageBitmap(imageitem);
        // Return the completed view to render on screen
        return convertView;
    }
}