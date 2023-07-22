package com.example.android.SmartMicroscope.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.SmartMicroscope.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileListAdapterExpandable extends BaseExpandableListAdapter {private List<String> lstGroups;
    private HashMap<String, List<FileListItemExpandable>> lstItemsGroups;
    private Context context;

    public FileListAdapterExpandable(Context context, List<String> groups, HashMap<String, List<FileListItemExpandable>> itemsGroups){
        // initialize class variables
        this.context = context;
        lstGroups = groups;
        lstItemsGroups = itemsGroups;
    }

    @Override
    public int getGroupCount() {
        // returns groups count
        return lstGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // returns items count of a group
        return lstItemsGroups.get(getGroup(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // returns a group
        return lstGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // returns a group item
        return lstItemsGroups.get(getGroup(groupPosition)).get(childPosition);
    }

    public void moveChildBetweenGroup(int initgroupPosition, ArrayList<Integer> childpositions, int finalgroupposition) {
        for (int listIterator = 0; listIterator < childpositions.size(); ++listIterator)
        {
            lstItemsGroups.get(getGroup(finalgroupposition)).add(getFileListChild(initgroupPosition, childpositions.get(listIterator)));
        }

        //for some reason I cannot get these to remove themselves from the list, so  Iam just going to overwrite the list in the activity.
        //for (int listIterator = 0; listIterator < childpositions.size(); ++listIterator)
        //{
        //    lstItemsGroups.get(getGroup(initgroupPosition)).remove(getFileListChild(initgroupPosition, childpositions.get(listIterator)));
        //}

        //lstItemsGroups.get(getGroup(initgroupPosition)).remove(getFileListChild(initgroupPosition, initchildPosition));
        //notifyDataSetChanged();
    }
    public void moveChildBetweenGroupSingle(int initgroupPosition, int initchildPosition, int finalgroupposition) {

        lstItemsGroups.get(getGroup(finalgroupposition)).add(getFileListChild(initgroupPosition, initchildPosition));
        lstItemsGroups.get(getGroup(initgroupPosition)).remove(getFileListChild(initgroupPosition, initchildPosition));
        //notifyDataSetChanged();
    }

    public FileListItemExpandable getFileListChild(int groupPosition, int childPosition)
    {
        FileListItemExpandable fileListItem = lstItemsGroups.get(getGroup(groupPosition)).get(childPosition);
        return fileListItem;
    }

    @Override
    public long getGroupId(int groupPosition) {
        // return the group id
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // returns the item id of group
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // returns if the ids are specific ( unique for each group or item)
        // or relatives
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        // create main items (groups)
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_file_group, null);
        }

        TextView tvGroup = (TextView) convertView.findViewById(R.id.tvGroup);
        TextView tvAmount = (TextView) convertView.findViewById(R.id.tvAmount);

        tvGroup.setText((String) getGroup(groupPosition));
        tvAmount.setText(String.valueOf(getChildrenCount(groupPosition)));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        // create the subitems (items of groups)

        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_file_layout, null);
        }


        TextView imageNumber = (TextView) convertView.findViewById(R.id.fileImageNumberTextview);
        TextView fileLocation = (TextView)convertView.findViewById(R.id.fileLocationTextview);
        TextView tvDateCreated = (TextView) convertView.findViewById(R.id.fileDateTextview);
        TextView tvLocationCreated = (TextView) convertView.findViewById(R.id.fileLocationTextview);
        TextView tvObjectsDetected = (TextView) convertView.findViewById(R.id.fileobjectlist);
        ImageView ivFileListThumbnail = (ImageView) convertView.findViewById(R.id.fileListThumbnail);

        FileListItemExpandable fileItem = (FileListItemExpandable) getChild(groupPosition, childPosition);
        imageNumber.setText(fileItem.getImageNumber());
        fileLocation.setText(fileItem.getFilelocation());
        tvDateCreated.setText(fileItem.getDateCreated());
        tvLocationCreated.setText(fileItem.getLocationCreated());
        tvObjectsDetected.setText(fileItem.getObjectsDetected());
        ivFileListThumbnail.setImageBitmap(fileItem.getImageThumbnail());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // returns if the subitem (item of group) can be selected
        return true;
    }





}
