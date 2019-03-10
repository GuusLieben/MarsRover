package com.exludit.marsrover.adapters;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.exludit.marsrover.PhotoView;
import com.exludit.marsrover.R;
import com.exludit.marsrover.objects.RoverPhoto;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RoverAdapter extends RecyclerView.Adapter<RoverAdapter.RoverPhotoHolder> {

    private String roverName;
    private boolean rotationIsLandscape;
    private List<RoverPhoto> dataSet = new ArrayList<>();

    public void setRoverName(String roverName) {
        this.roverName = roverName;
    }

    @NonNull
    @Override
    public RoverPhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rover_photo, parent, false);

        rotationIsLandscape = itemLayoutView.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        return new RoverPhotoHolder(itemLayoutView);
    }

    public void swapItems(List<RoverPhoto> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RoverPhotoHolder roverPhotoHolder, int position) {
        if (rotationIsLandscape) {
            roverPhotoHolder.photoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            roverPhotoHolder.photoTextView.setText(String.format(
                    "Image ID : %s%nRover : %s%nSol : %s%nDate : %s%nCamera : %s",
                    String.valueOf(dataSet.get(position).getId()),
                    roverName,
                    String.valueOf(dataSet.get(position).getSolDay()),
                    dataSet.get(position).getEarthDate(),
                    dataSet.get(position).getFullCameraName()));
        } else {
            roverPhotoHolder.photoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            roverPhotoHolder.photoTextView.setText(String.format("Image ID : %s", String.valueOf(dataSet.get(position).getId())));
        }

        roverPhotoHolder.photoImageView.setOnClickListener(c -> {
            Intent imageIntent = new Intent(roverPhotoHolder.itemView.getContext(),
                    PhotoView.class);

            RoverPhoto photo = dataSet.get(position);

            String photoJson = (new Gson().toJson(photo));
            imageIntent.putExtra("photo", photoJson);

            imageIntent.putExtra("roverName", roverName);

            roverPhotoHolder.itemView.getContext().startActivity(imageIntent);
        });

        Picasso.get().load(dataSet.get(position).getImageUri()).into(roverPhotoHolder.photoImageView);
    }

    static class RoverPhotoHolder extends RecyclerView.ViewHolder {

        TextView photoTextView;
        ImageView photoImageView;

        RoverPhotoHolder(View itemLayoutView) {
            super(itemLayoutView);
            photoTextView = itemLayoutView.findViewById(R.id.photo_details);
            photoImageView = itemLayoutView.findViewById(R.id.photo_image);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}