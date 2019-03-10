package com.exludit.marsrover.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

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

        this.rover = Rover.getByName(bundle.getString("roverName"));
        this.type = bundle.getString("type");

        switch (bundle.getString("roverName").toLowerCase()) {
            case "curiosity":
                manifestUri = Constants.CURIOSITY_MANIFEST_URL;
                photoUri = Constants.CURIOSITY_PHOTO_URL;
                break;
            case "opportunity":
                manifestUri = Constants.OPPORTUNITY_MANIFEST_URL;
                photoUri = Constants.OPPORTUNITY_PHOTO_URL;
                break;
            case "spirit":
                manifestUri = Constants.SPIRIT_MANIFEST_URL;
                photoUri = Constants.SPIRIT_PHOTO_URL;
                break;
        }
    }

    @Override
    protected void onStartLoading() {
        listener.showProgressBar();
        forceLoad();
    }

    @Override
    @Nullable
    public Rover[] loadInBackground() {
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
        this.listener = listener;
    }
}
