package com.exludit.marsrover.async;

import android.util.Log;

import com.exludit.marsrover.RoverActivity;
import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.Rover;
import com.exludit.marsrover.domain.RoverPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoConstruction {

    private PhotoConstruction() {
    }

    static void collectPhotosFor(Rover rover, String photoUri, RoverActivity context) {
        try {
            JSONObject photoList = JSONUtils.getJSONObjectFromUrl(photoUri);

            Log.d(Constants.PHOTO_CONST_TAG, photoList.toString());

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
                Log.d(Constants.PHOTO_CONST_TAG, String.format("Successfully created new RoverPhoto : %s", roverPhoto.toString()));
                rover.addPhoto(roverPhoto);

                context.runOnUiThread(() -> context.getRecyclerAdapter().swapItems(rover.getPhotos()));

            }
        } catch (JSONException e) {
            Log.e(Constants.PHOTO_CONST_TAG, e.getMessage());
            for (StackTraceElement el : e.getStackTrace())
                Log.e(Constants.PHOTO_CONST_TAG, el.toString());
        }
    }

}
