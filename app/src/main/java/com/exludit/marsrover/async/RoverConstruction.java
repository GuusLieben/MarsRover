package com.exludit.marsrover.async;

import android.util.Log;

import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.Rover;

import org.json.JSONException;

import java.util.Arrays;

public class RoverConstruction {

    private RoverConstruction() {
    }

    static Rover[] fillRovers(Rover[] rovers) {
        // Rovers and cameras defined on https://api.nasa.gov/api.html#MarsPhotos
        if (rovers == null) {
            Rover curiosity = new Rover("curiosity", new String[]{Constants.FHAZ, Constants.RHAZ, Constants.MAST, Constants.CHEMCAM, Constants.MAHLI, Constants.MARDI, Constants.NAVCAM});
            Rover opportunity = new Rover("opportunity", new String[]{Constants.FHAZ, Constants.RHAZ, Constants.NAVCAM, Constants.PANCAM, Constants.MINITES});
            Rover spirit = new Rover("spirit", new String[]{Constants.FHAZ, Constants.RHAZ, Constants.NAVCAM, Constants.PANCAM, Constants.MINITES});


            rovers = new Rover[]{curiosity, opportunity, spirit};
            Log.i(Constants.ROVER_CONST_TAG, "Successfully constructed rovers : " + Arrays.toString(rovers));
        }
        return rovers;
    }

    static Rover[] constructRoverObjects(Rover[] rovers, String manifestUriString) {
        for (Rover roverNoMaxSol : fillRovers(rovers)) {
            Log.i(Constants.ROVER_CONST_TAG, String.format("Collecting manifest for %s", roverNoMaxSol.getName()));
            try {
                int maxSol = JSONUtils.getJSONObjectFromUrl(manifestUriString)
                        .getJSONObject("photo_manifest")
                        .getInt("max_sol");
                Log.i(Constants.ROVER_CONST_TAG, String.format("Setting maximum Sol to %d", maxSol));
                roverNoMaxSol.setMaxSol(maxSol);
            } catch (JSONException e) {
                Log.e(Constants.ROVER_CONST_TAG, e.getMessage());
            }
        }

        return rovers;
    }

}
