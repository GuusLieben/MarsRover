package com.exludit.marsrover.domain;

import com.exludit.marsrover.PhotoView;
import com.exludit.marsrover.RoverActivity;
import com.exludit.marsrover.adapters.RoverAdapter;
import com.exludit.marsrover.async.APICycle;
import com.exludit.marsrover.async.JSONUtils;
import com.exludit.marsrover.async.PhotoConstruction;
import com.exludit.marsrover.async.RoverConstruction;

public class Constants {

    // Photo API url Strings //
    public static final String CURIOSITY_PHOTO_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";
    public static final String OPPORTUNITY_PHOTO_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/opportunity/photos";
    public static final String SPIRIT_PHOTO_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/spirit/photos";

    // Manifest API url Strings //
    public static final String CURIOSITY_MANIFEST_URL = "https://api.nasa.gov/mars-photos/api/v1/manifests/curiosity";
    public static final String OPPORTUNITY_MANIFEST_URL = "https://api.nasa.gov/mars-photos/api/v1/manifests/opportunity";
    public static final String SPIRIT_MANIFEST_URL = "https://api.nasa.gov/mars-photos/api/v1/manifests/spirit";

    // General API url params with values //
    public static final String PARAM_SOL = "sol";
    public static final int VALUE_SOL = 1500;

    public static final String PARAM_KEY = "api_key";
    public static final String VALUE_KEY = "vwBFh1zxgp0ymckxjl6axXihk7EVQIFb91ChbZv7";

    // Log Tags for Adapter classes //
    public static final String ADAPTER_LOG_TAG = RoverAdapter.class.getCanonicalName();

    // Log Tags for Async classes //
    public static final String JSON_UTILS_TAG = JSONUtils.class.getCanonicalName();
    public static final String ROVER_CONST_TAG = RoverConstruction.class.getCanonicalName();
    public static final String PHOTO_CONST_TAG = PhotoConstruction.class.getCanonicalName();
    public static final String API_COL_TAG = APICycle.class.getCanonicalName();

    // Log Tags for Objects //
    public static final String ROVEROBJECT_LOG_TAG = Rover.class.getCanonicalName();
    public static final String ROVERPHOTOOBJECT_LOG_TAG = RoverPhoto.class.getCanonicalName();

    // Log Tags for Activities //
    public static final String PHOTOVIEWINTENT_LOG_TAG = PhotoView.class.getCanonicalName();
    public static final String MAINACTIVITY_LOG_TAG = RoverActivity.class.getCanonicalName();

    // Preferences //
    public static final String PREFERENCES = "MarsRoverPreferences";
    public static final String TYPE_ROVER = "$ROVER";
    public static final String TYPE_PHOTOS = "$PHOTOS";
}
