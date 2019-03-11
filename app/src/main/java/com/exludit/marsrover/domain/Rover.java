package com.exludit.marsrover.domain;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Rover {

    private static List<Rover> rovers = new ArrayList<>();

    private String name;
    private int maxSol;
    private String[] cameras;
    private List<RoverPhoto> photos;

    public Rover(String name, String[] cameras) {
        this.name = name;
        this.cameras = cameras;
        this.photos = new ArrayList<>();
        rovers.add(this);
    }

    public static Rover getByName(String roverObjectName) {
        Log.d(Constants.ROVEROBJECT_LOG_TAG, String.format("Requesting Rover for name : %s", roverObjectName));
        int i = 0;
        while (i < rovers.size()) {
            Rover object = rovers.get(i);
            if (object.getName().equalsIgnoreCase(roverObjectName)) {
                Log.d(Constants.ROVEROBJECT_LOG_TAG, "Found matching object, returning...");
                return object;
            }
            i++;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getMaxSol() {
        return maxSol;
    }

    public String[] getCameras() {
        return cameras;
    }

    public void setMaxSol(int maxSol) {
        this.maxSol = maxSol;
    }

    public void addPhoto(RoverPhoto photo) {
        this.photos.add(photo);
    }

    public List<RoverPhoto> getPhotos() {
        return photos;
    }
}
