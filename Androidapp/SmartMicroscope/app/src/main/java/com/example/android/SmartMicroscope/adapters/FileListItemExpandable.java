package com.example.android.SmartMicroscope.adapters;

import android.graphics.Bitmap;

public class FileListItemExpandable {
    private String imageNumber;
    private String dateCreated;
    private String locationCreated;
    private String objectsDetected;
    private Bitmap imageThumbnail;
    private String fileLocation;
    private String dateAnalysed;
    private String UUID;

    public FileListItemExpandable(String imageNumber, String dateCreated, String locationCreated, String objectsDetected, Bitmap imageThumbnail, String fileLocation, String dateAnalysed, String UUID) {
        this.imageNumber = imageNumber;
        this.dateCreated = dateCreated;
        this.locationCreated = locationCreated;
        this.objectsDetected = objectsDetected;
        this.imageThumbnail = imageThumbnail;
        this.fileLocation = fileLocation;
        this.dateAnalysed = dateAnalysed;
        this.UUID = UUID;
    }
    public String getFilelocation() {
        return fileLocation;
    }

    public void setFilelocation(String filelocation) {
        this.fileLocation = fileLocation;
    }

    public String getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(String imageNumber) {
        this.imageNumber = imageNumber;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLocationCreated() {
        return locationCreated;
    }

    public void setLocationCreated(String locationCreated) {this.locationCreated = locationCreated;    }

    public String getDateAnalysed() {
        return dateAnalysed;
    }
    public void setDateAnalysed(String dateAnalysed) {this.dateAnalysed = dateAnalysed;}
    public String getUUID() {
        return UUID;
    }

    public String getObjectsDetected() {
        return objectsDetected;
    }

    public void setObjectsDetected(String objectsDetected) {
        this.objectsDetected = objectsDetected;
    }

    public Bitmap getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(Bitmap imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }
}
