package com.exludit.marsrover.objects;

import android.net.Uri;
import android.support.annotation.NonNull;

public class RoverPhoto {

    private int id;
    private String cameraName;
    private String fullCameraName;
    private int solDay;
    private String earthDate;
    private String imageUri;

    public RoverPhoto(int id, String cameraName, String fullCameraName, int solDay, String earthDate, String imageUri) {
        this.id = id;
        this.cameraName = cameraName;
        this.fullCameraName = fullCameraName;
        this.solDay = solDay;
        this.earthDate = earthDate;
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoverPhoto{" +
                "id=" + id +
                ", cameraName='" + cameraName + '\'' +
                ", fullCameraName='" + fullCameraName + '\'' +
                ", solDay=" + solDay +
                ", earthDate='" + earthDate + '\'' +
                ", imageUri=" + imageUri +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getCameraName() {
        return cameraName;
    }

    public String getFullCameraName() {
        return fullCameraName;
    }

    public int getSolDay() {
        return solDay;
    }

    public String getEarthDate() {
        return earthDate;
    }

    public Uri getImageUri() {
        return Uri.parse(imageUri);
    }
}
