package com.example.android.SmartMicroscope.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.SmartMicroscope.FileBrowseActivityExpandable;
import com.example.android.SmartMicroscope.R;

import java.util.ArrayList;

public class FolderListAdapter extends ArrayAdapter<FolderListItem> {
    private static final String TAG = "FolderListAdapter";
    private Context mcontext;
    int mResource;

    public FolderListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FolderListItem> objects) {
        super(context, resource, objects);
        mcontext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String sampleNumber = getItem(position).getSampleNumber();
        String dateCreated = getItem(position).getDateCreated();
        String locationCreated = getItem(position).getLocationCreated();
        String numberImages = getItem(position).getNumberImages();
        String positivityResult = getItem(position).getPositivityResult();
        String sampleType = getItem(position).getSampleType();


        FolderListItem folderListItem = new FolderListItem(sampleNumber, dateCreated, locationCreated, numberImages, positivityResult, sampleType);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvSampleNumber = (TextView) convertView.findViewById(R.id.folderSamplenumberTextview);
        TextView tvDateCreated = (TextView) convertView.findViewById(R.id.folderDateTextview);
        TextView tvLocationCreated = (TextView) convertView.findViewById(R.id.folderLocationTextview);
        TextView tvNumberImages = (TextView) convertView.findViewById(R.id.folderNumberImages);
        ImageView ivFolderImage = (ImageView) convertView.findViewById(R.id.folderlistimage);
        TextView tvpositivityResult = (TextView) convertView.findViewById(R.id.folderpositivityresultTextview);
        TextView tvsampleType = (TextView) convertView.findViewById(R.id.folderSampleTypeTextView);

        tvSampleNumber.setText(sampleNumber);
        tvDateCreated.setText(dateCreated);
        tvLocationCreated.setText(locationCreated);
        tvNumberImages.setText(numberImages);
        tvpositivityResult.setText(positivityResult);
        tvsampleType.setText(sampleType);
        ivFolderImage.setImageResource(R.mipmap.folder_round);

        if (positivityResult.contains("Positive")){
            tvSampleNumber.setTextColor(Color.rgb(200,0,0));
            tvsampleType.setTextColor(Color.rgb(200,0,0));
            tvpositivityResult.setTextColor(Color.rgb(200,0,0));
            //tvLocationCreated.setTextColor(Color.rgb(200,0,0));
            //tvNumberImages.setTextColor(Color.rgb(200,0,0));
            //tvDateCreated.setTextColor(Color.rgb(200,0,0));

        }
        else if (positivityResult.contains("Negative")){
            tvSampleNumber.setTextColor(Color.rgb(0,0,200));
            tvsampleType.setTextColor(Color.rgb(0,0,200));
            tvpositivityResult.setTextColor(Color.rgb(0,0,200));
            //tvLocationCreated.setTextColor(Color.rgb(0,0,200));
            //tvNumberImages.setTextColor(Color.rgb(0,0,200));
            //tvDateCreated.setTextColor(Color.rgb(0,0,200));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileBrowseOpen = new Intent(getContext(), FileBrowseActivityExpandable.class);
                fileBrowseOpen.putExtra("folderName", Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/" + sampleNumber);
                getContext().startActivity(fileBrowseOpen);
            }
        });

        return convertView;
    }
}
