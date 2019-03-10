package com.exludit.marsrover;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.RoverPhoto;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;

public class PhotoView extends AppCompatActivity {

    private ImageView photoImageView;
    private TextView photoTextView;
    private ImageButton photoShareButton;
    private Intent intent;

    private final String logClass = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rover_photo_full);

        Log.i(Constants.PHOTOVIEWINTENT_LOG_TAG, "Created Content View, assigning Views to local variables");

        photoImageView = findViewById(R.id.photo_image);
        photoTextView = findViewById(R.id.photo_details);
        photoShareButton = findViewById(R.id.share_button);
        intent = getIntent();

        Log.i(Constants.PHOTOVIEWINTENT_LOG_TAG, "Successfully assigned Views");
        Log.i(Constants.PHOTOVIEWINTENT_LOG_TAG, "Reconstructing RoverPhoto object from Gson StringExtra");

        String photoJson = intent.getStringExtra("photo");
        RoverPhoto photo = new Gson().fromJson(photoJson, RoverPhoto.class);

        String roverName = intent.getStringExtra(Constants.BUNDLE_ROVERNAME);

        photoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        photoTextView.setText(String.format(
                getString(R.string.full_detail_image_album),
                String.valueOf(photo.getId()),
                roverName,
                String.valueOf(photo.getSolDay()),
                photo.getEarthDate(),
                photo.getFullCameraName()));

        Picasso.get().load(photo.getImageUri()).into(photoImageView);

        Log.i(Constants.PHOTOVIEWINTENT_LOG_TAG, "Assigning onClickListeners");

        photoImageView.setOnClickListener(c -> finish());
        photoTextView.setOnClickListener(c -> finish());
        photoShareButton.setOnClickListener(c -> Picasso.get().load(photo.getImageUri().toString()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                String message = String.format(getString(R.string.share_message),
                        String.valueOf(photo.getId()),
                        roverName,
                        photo.getImageUri().toString());

                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, message);
                Log.i(Constants.PHOTOVIEWINTENT_LOG_TAG, "Sharing image");
                startActivity(Intent.createChooser(i, "Share Image"));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e(logClass, Arrays.toString(e.getStackTrace()));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.e(logClass, String.format("Preparing : %s", placeHolderDrawable.toString()));
            }
        }));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
