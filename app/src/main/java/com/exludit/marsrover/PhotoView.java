package com.exludit.marsrover;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.exludit.marsrover.objects.RoverPhoto;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PhotoView extends AppCompatActivity {

    private ImageView photoImageView;
    private TextView photoTextView;
    private ImageButton photoShareButton;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rover_photo_full);

        photoImageView = findViewById(R.id.photo_image);
        photoTextView = findViewById(R.id.photo_details);
        photoShareButton = findViewById(R.id.share_button);
        intent = getIntent();

        String photoJson = intent.getStringExtra("photo");
        RoverPhoto photo = new Gson().fromJson(photoJson, RoverPhoto.class);

        String roverName = intent.getStringExtra("roverName");

        photoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        photoTextView.setText(String.format(
                "Image ID : %s%nRover : %s%nSol : %s%nDate : %s%nCamera : %s",
                String.valueOf(photo.getId()),
                roverName,
                String.valueOf(photo.getSolDay()),
                photo.getEarthDate(),
                photo.getFullCameraName()));

        Picasso.get().load(photo.getImageUri()).into(photoImageView);

        photoImageView.setOnClickListener(c -> finish());
        photoTextView.setOnClickListener(c -> finish());
        photoShareButton.setOnClickListener(c -> Picasso.get().load(photo.getImageUri().toString()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                String message = String.format("Check out image #%s from the %s rover! %s",
                        photo.getId(),
                        roverName,
                        photo.getImageUri().toString());

                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(i, "Share Image"));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        }));
        ;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
