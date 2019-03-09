package com.exludit.marsrover.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.exludit.marsrover.RoverActivity;
import com.exludit.marsrover.objects.RoverObject;
import com.exludit.marsrover.objects.RoverPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ApiCollector extends AsyncTask<TaskTypes, Void, RoverObject[]> {

    private static final String manifestUriString = "https://api.nasa.gov/mars-photos/api/v1/manifests/ROVERNAME?api_key=APIKEY";

    private static final String photoUriString = "https://api.nasa.gov/mars-photos/api/v1/rovers/ROVERNAME/photos?sol=SOLDAY&api_key=APIKEY";
    private static final String key = "vwBFh1zxgp0ymckxjl6axXihk7EVQIFb91ChbZv7";

    private final String logClass = this.getClass().getName();

    private static RoverObject[] rovers = null;

    private RoverActivity context;
    private RoverObject rover;

    public ApiCollector(Context context, RoverObject rover) {
        this.context = (RoverActivity) context;
        this.rover = rover;
    }

    @Override
    @Nullable
    protected RoverObject[] doInBackground(TaskTypes... taskTypes) {
        if (rover == null && taskTypes[0] == TaskTypes.ROVERS) {
            rovers = constructRoverObjects();
        } else if (rover.getPhotos().size() == 0 && taskTypes[0] == TaskTypes.PHOTOS) {
            if (rover.getPhotos().size() == 0) collectPhotosFor(rover);
        }
        return rovers;
    }

    public static RoverObject[] getRovers() {
        // Rovers and cameras defined on https://api.nasa.gov/api.html#MarsPhotos
        RoverObject curiosity = new RoverObject("curiosity", new String[]{"FHAZ", "RHAZ", "MAST", "CHEMCAM", "MAHLI", "MARDI", "NAVCAM"});
        RoverObject opportunity = new RoverObject("opportunity", new String[]{"FHAZ", "RHAZ", "NAVCAM", "PANCAM", "MINITES"});
        RoverObject spirit = new RoverObject("spirit", new String[]{"FHAZ", "RHAZ", "NAVCAM", "PANCAM", "MINITES"});

        rovers = new RoverObject[]{curiosity, opportunity, spirit};

        return rovers;
    }

    private RoverObject[] constructRoverObjects() {
        for (RoverObject rover : getRovers()) {
            // Get data from it
            try {
                int max_sol = getJSONObjectFromUrl(manifestUriString, rover.getName(), 0)
                        .getJSONObject("photo_manifest")
                        .getInt("max_sol");
                rover.setMaxSol(max_sol);
            } catch (JSONException e) {
                Log.e(logClass, e.getMessage());
            }
        }

        return rovers;
    }

    private void collectPhotosFor(RoverObject roverObject) {
        try {
            JSONObject photoList = getJSONObjectFromUrl(
                    photoUriString,
                    roverObject.getName(),
                    1500);

            Log.i(logClass, photoList.toString());

            JSONArray photoArray = photoList.getJSONArray("photos");

            for (int i = 0; i < photoArray.length(); i++) {
                JSONObject photo = photoArray.getJSONObject(i);
                int id = photo.getInt("id");
                String cameraName = photo.getJSONObject("camera").getString("name");
                String fullCameraName = photo.getJSONObject("camera").getString("full_name");
                int sol = 1500;
                String earthDate = photo.getString("earth_date");
                String img_src = photo.getString("img_src");

                RoverPhoto roverPhoto = new RoverPhoto(id, cameraName, fullCameraName, sol, earthDate, img_src);
                Log.i(logClass, String.format("Successfully created new RoverPhoto : %s", roverPhoto.toString()));
                roverObject.addPhoto(roverPhoto);

                context.runOnUiThread(() -> context.getRecyclerAdapter().swapItems(roverObject.getPhotos()));

            }
        } catch (JSONException e) {
            Log.e(logClass, e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) Log.e(logClass, el.toString());
        }
        context.runOnUiThread(() -> context.showToastResults(String.format("Loaded %s photos for %s", roverObject.getPhotos().size(), roverObject.getName())));
    }

    private JSONObject getJSONObjectFromUrl(String uri, String rovername, int sol) {
        URLConnection connection;
        BufferedReader reader = null;
        try {
            String fullUri = uri
                    .replace("ROVERNAME", rovername)
                    .replace("SOLDAY", String.valueOf(sol))
                    .replace("APIKEY", key);

            Log.i(logClass, fullUri);

            URL url = new URL(fullUri);
            connection = url.openConnection();
            Log.d(logClass, "Successfully opened connection to API");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) buffer.append(line);

            JSONObject parent = new JSONObject(buffer.toString());
            return parent;
        } catch (IOException | JSONException e) {
            Log.e(logClass, e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) Log.e(logClass, el.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(logClass, e.getMessage());
                    for (StackTraceElement el : e.getStackTrace()) Log.e(logClass, el.toString());
                }
            }
        }
        return new JSONObject(); // Never return null, please
    }
}
