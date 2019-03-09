package com.exludit.marsrover.objects;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class RoverPhoto implements Parcelable {

    private int id;
    private String cameraName;
    private String fullCameraName;
    private int solDay;
    private String earthDate;
    private String imageUri;

    private int parcelData;

    public RoverPhoto(int id, String cameraName, String fullCameraName, int solDay, String earthDate, String imageUri) {
        this.id = id;
        this.cameraName = cameraName;
        this.fullCameraName = fullCameraName;
        this.solDay = solDay;
        this.earthDate = earthDate;
        this.imageUri = imageUri;
    }

    private RoverPhoto(Parcel in) {
        parcelData = in.readInt();
    }

    public static final Creator<RoverPhoto> CREATOR = new Creator<RoverPhoto>() {
        @Override
        public RoverPhoto createFromParcel(Parcel in) {
            return new RoverPhoto(in);
        }

        @Override
        public RoverPhoto[] newArray(int size) {
            return new RoverPhoto[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(parcelData);
    }
}
