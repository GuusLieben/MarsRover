package com.exludit.marsrover.async;

import android.util.Log;

import com.exludit.marsrover.domain.Rover;

import org.json.JSONException;

public class RoverConstruction {

    static Rover[] fillRovers(Rover[] rovers) {
        // Rovers and cameras defined on https://api.nasa.gov/api.html#MarsPhotos
        if (rovers == null) {
            Rover curiosity = new Rover("curiosity", new String[]{"CHEMCAM", "FHAZ", "MAHLI", "MARDI", "MAST", "NAVCAM", "RHAZ"});
            Rover opportunity = new Rover("opportunity", new String[]{"FHAZ", "MINITES", "NAVCAM", "PANCAM", "RHAZ"});
            Rover spirit = new Rover("spirit", new String[]{"FHAZ", "MINITES", "NAVCAM", "PANCAM", "RHAZ"});

            rovers = new Rover[]{curiosity, opportunity, spirit};
        }
        return rovers;
    }

    static Rover[] constructRoverObjects(Rover[] rovers, String manifestUriString) {
        for (Rover roverNoMaxSol : fillRovers(rovers)) {
            // Get data from it
            try {
                int maxSol = JSONUtils.getJSONObjectFromUrl(manifestUriString)
                        .getJSONObject("photo_manifest")
                        .getInt("max_sol");
                roverNoMaxSol.setMaxSol(maxSol);
            } catch (JSONException e) {
                Log.e("TAG", e.getMessage());
            }
        }

        return rovers;
    }

}
