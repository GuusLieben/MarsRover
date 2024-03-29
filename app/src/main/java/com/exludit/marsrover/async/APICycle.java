package com.exludit.marsrover.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.exludit.marsrover.RoverActivity;
import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.Rover;

import java.util.Objects;

public class APICycle extends AsyncTaskLoader<Rover[]> {

    private static Rover[] rovers = null;

    @SuppressLint("StaticFieldLeak")
    private RoverActivity context;
    private Rover rover;
    private String manifestUri;
    private String photoUri;
    private String type;
    private LoaderListener listener;

    public APICycle(Context context, Bundle bundle) {
        super(context);
        this.context = (RoverActivity) context;

        Log.d(Constants.API_CYCLE_TAG, "Generating new API Cycle");

        this.rover = Rover.getByName(bundle.getString(Constants.BUNDLE_ROVERNAME));
        this.type = bundle.getString("type");

        switch (Objects.requireNonNull(bundle.getString(Constants.BUNDLE_ROVERNAME)).toLowerCase()) {
            case "opportunity":
                manifestUri = Constants.OPPORTUNITY_MANIFEST_URL;
                photoUri = Constants.OPPORTUNITY_PHOTO_URL;
                break;
            case "spirit":
                manifestUri = Constants.SPIRIT_MANIFEST_URL;
                photoUri = Constants.SPIRIT_PHOTO_URL;
                break;
            default: // In case of Curiosity or invalid name
                manifestUri = Constants.CURIOSITY_MANIFEST_URL;
                photoUri = Constants.CURIOSITY_PHOTO_URL;
                break;
        }

        Log.d(Constants.API_CYCLE_TAG, String.format("Generated new resources for %s :%n\tManifest : %s%n\tPhotos : %s",
                bundle.getString(Constants.BUNDLE_ROVERNAME),
                manifestUri,
                photoUri));
    }

    @Override
    protected void onStartLoading() {
        listener.showProgressBar();
        forceLoad();
    }

    @Override
    @Nullable
    public Rover[] loadInBackground() {
        Log.d(Constants.API_CYCLE_TAG, "Starting background task");
        if (rover == null && type.equals(Constants.TYPE_ROVER))
            rovers = RoverConstruction.constructRoverObjects(rovers, manifestUri);
        else if (Objects.requireNonNull(rover).getPhotos().isEmpty() && type.equals(Constants.TYPE_PHOTOS))
            PhotoConstruction.collectPhotosFor(rover, photoUri, context);
        return rovers;
    }

    public interface LoaderListener {
        void showProgressBar();

        void hideProgressBar();
    }

    public void setListener(LoaderListener listener) {
        Log.d(Constants.API_CYCLE_TAG, "Adopted new listener");
        this.listener = listener;
    }
}
