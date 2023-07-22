package com.example.android.SmartMicroscope.adapters;

public class FolderListItem {
    private String sampleNumber;
    private String dateCreated;
    private String locationCreated;
    private String numberImages;
    private String detectedObjects;
    private String positivityResult;
    private String sampleType;

    public FolderListItem(String sampleNumber, String dateCreated, String locationCreated, String numberImages, String positivityResult, String sampleType) {
        this.sampleNumber = sampleNumber;
        this.dateCreated = dateCreated;
        this.locationCreated = locationCreated;
        this.numberImages = numberImages;
        this.positivityResult = positivityResult;
        this.sampleType = sampleType;
    }

    public String getSampleNumber() {
        return sampleNumber;
    }

    public void setSampleNumber(String sampleNumber) {
        this.sampleNumber = sampleNumber;
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

    public void setLocationCreated(String locationCreated) {
        this.locationCreated = locationCreated;
    }

    public String getNumberImages() {
        return numberImages;
    }

    public void setNumberImages(String numberImages) {
        this.numberImages = numberImages;
    }

    public String getDetectedObjects() {
        return detectedObjects;
    }

    public void setDetectedObjects(String detectedObjects) {
        this.detectedObjects = detectedObjects;
    }
    public String getPositivityResult() {
        return positivityResult;
    }
    public String getSampleType() {
        return sampleType;
    }
}
