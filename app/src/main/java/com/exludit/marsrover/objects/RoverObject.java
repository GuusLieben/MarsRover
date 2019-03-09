package com.exludit.marsrover.objects;

import java.util.ArrayList;
import java.util.List;

public class RoverObject {

    private static List<RoverObject> rovers = new ArrayList<>();

    private String name;
    private int maxSol;
    private String[] cameras;
    private List<RoverPhoto> photos;

    public RoverObject(String name, String[] cameras) {
        this.name = name;
        this.cameras = cameras;
        this.photos = new ArrayList<>();
        rovers.add(this);
    }

    public static RoverObject getByName(String roverObjectName) {
        for (int i=0; i<rovers.size(); i++) {
            RoverObject object = rovers.get(i);
            if (object.getName().toLowerCase().equals(roverObjectName.toLowerCase())) return object;
        }
        return rovers.get(0);
//        return rovers.stream().filter(rover -> rover.getName().toLowerCase().equals(roverObjectName.toLowerCase())).findFirst().orElse(rovers.get(0));
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
