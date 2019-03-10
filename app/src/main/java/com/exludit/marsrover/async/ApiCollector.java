package com.exludit.marsrover.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.exludit.marsrover.RoverActivity;
import com.exludit.marsrover.objects.Rover;
import com.exludit.marsrover.objects.RoverPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class ApiCollector extends AsyncTask<TaskTypes, Void, Rover[]> {

    private static final String MANIFEST_URI_STRING = "https://api.nasa.gov/mars-photos/api/v1/manifests/ROVERNAME?api_key=APIKEY";

    private static final String PHOTO_URI_STRING = "https://api.nasa.gov/mars-photos/api/v1/rovers/ROVERNAME/photos?sol=SOLDAY&api_key=APIKEY";
    private static final String API_KEY = "vwBFh1zxgp0ymckxjl6axXihk7EVQIFb91ChbZv7";

    private final String logClass = this.getClass().getName();

    private static Rover[] rovers = null;

    @SuppressLint("StaticFieldLeak")
    private RoverActivity context;
    private Rover rover;

    public ApiCollector(Context context, Rover rover) {
        this.context = (RoverActivity) context;
        this.rover = rover;
    }

    @Override
    @Nullable
    protected Rover[] doInBackground(TaskTypes... taskTypes) {
        if (rover == null && taskTypes[0] == TaskTypes.ROVERS) {
            rovers = constructRoverObjects();
        } else if (Objects.requireNonNull(rover).getPhotos().isEmpty() && taskTypes[0] == TaskTypes.PHOTOS) {
            collectPhotosFor(rover);
        }
        return rovers;
    }

    private Rover[] getRovers() {
        // Rovers and cameras defined on https://api.nasa.gov/api.html#MarsPhotos
        if (rovers == null) {
            Rover curiosity = new Rover("curiosity", new String[]{"CHEMCAM", "FHAZ", "MAHLI", "MARDI", "MAST", "NAVCAM", "RHAZ"});
            Rover opportunity = new Rover("opportunity", new String[]{"FHAZ", "MINITES", "NAVCAM", "PANCAM", "RHAZ"});
            Rover spirit = new Rover("spirit", new String[]{"FHAZ", "MINITES", "NAVCAM", "PANCAM", "RHAZ"});

            rovers = new Rover[]{curiosity, opportunity, spirit};
        }
        return rovers;
    }

    private Rover[] constructRoverObjects() {
        for (Rover roverNoMaxSol : getRovers()) {
            // Get data from it
            try {
                int maxSol = getJSONObjectFromUrl(MANIFEST_URI_STRING, roverNoMaxSol.getName(), 0)
                        .getJSONObject("photo_manifest")
                        .getInt("max_sol");
                roverNoMaxSol.setMaxSol(maxSol);
            } catch (JSONException e) {
                Log.e(logClass, e.getMessage());
            }
        }

        return rovers;
    }

    private void collectPhotosFor(Rover rover) {
        try {
            JSONObject photoList = getJSONObjectFromUrl(
                    PHOTO_URI_STRING,
                    rover.getName(),
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
                String imgSrc = photo.getString("img_src");

                RoverPhoto roverPhoto = new RoverPhoto(id, cameraName, fullCameraName, sol, earthDate, imgSrc);
                Log.i(logClass, String.format("Successfully created new RoverPhoto : %s", roverPhoto.toString()));
                rover.addPhoto(roverPhoto);

                context.runOnUiThread(() -> context.getRecyclerAdapter().swapItems(rover.getPhotos()));

            }
        } catch (JSONException e) {
            Log.e(logClass, e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) Log.e(logClass, el.toString());
        }
        context.runOnUiThread(() -> context.showToastResults(String.format("Loaded %s photos for %s", rover.getPhotos().size(), rover.getName())));
    }

    private JSONObject getJSONObjectFromUrl(String uri, String rovername, int sol) {
        URLConnection connection;
        BufferedReader reader = null;
        try {
            String fullUri = uri
                    .replace("ROVERNAME", rovername)
                    .replace("SOLDAY", String.valueOf(sol))
                    .replace("APIKEY", API_KEY);

            Log.i(logClass, fullUri);

            URL url = new URL(fullUri);
            connection = url.openConnection();
            Log.d(logClass, "Successfully opened connection to API");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) stringBuilder.append(line);

            return new JSONObject(stringBuilder.toString());
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
