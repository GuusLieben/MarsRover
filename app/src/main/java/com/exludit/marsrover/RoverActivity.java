package com.exludit.marsrover;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.exludit.marsrover.adapters.RoverAdapter;
import com.exludit.marsrover.async.APICycle;
import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.Rover;

import java.util.Objects;

public class RoverActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Rover[]>,
        APICycle.LoaderListener {

    private ProgressBar loadingIndicator;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static Rover[] rovers;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private final String logClass = this.getClass().getName();

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingIndicator = findViewById(R.id.loading_indicator);

        recyclerView = findViewById(R.id.main_recycler);
        Log.d(logClass, "Content view and RecyclerView loaded");

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Log.d(logClass, "Successfully obtained navigation Views");

        editor = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();
        preferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        Log.d(logClass, "Successfully obtained SharePreferences");

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Log.d(logClass, "Successfully set up navigation states");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(logClass, "Detected landscape orientation, setting GridLayoutManager (spanCount : 2)");
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            Log.d(logClass, "Detected portrait orientation, setting LinearLayoutManager");
            layoutManager = new LinearLayoutManager(this);
        }

        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RoverAdapter();
        recyclerView.setAdapter(mAdapter);
        Log.d(logClass, "Successfully adopted RoverAdapter");
    }

    @Override
    public void onResume() {
        super.onResume();
        String roverName = preferences.getString(getString(R.string.preffered_rover_key), getString(R.string.rover_curiosity));
        collectRovers(roverName);
        setTitle(String.format("MarsRover - %s", roverName));
        collectPhotos(roverName);
    }

    @SuppressWarnings("deprecation")
    private void collectRovers(String roverName) {
        if (rovers == null) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BUNDLE_ROVERNAME, roverName);
            bundle.putString("type", Constants.TYPE_ROVER);

            getSupportLoaderManager().initLoader(10, bundle, this);
        }
    }

    @SuppressWarnings("deprecation")
    private void collectPhotos(String currentRoverObjectName) {
        Rover rover = Rover.getByName(currentRoverObjectName);
        if (rover != null) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BUNDLE_ROVERNAME, currentRoverObjectName);
            bundle.putString("type", Constants.TYPE_PHOTOS);
            getSupportLoaderManager().initLoader(20, bundle, this);

            recyclerView.setAdapter(mAdapter);
            ((RoverAdapter) mAdapter).setRoverName(rover.getName());

            ((RoverAdapter) Objects.requireNonNull(recyclerView.getAdapter())).swapItems(rover.getPhotos());
        } else {
            collectRovers(currentRoverObjectName);
            collectPhotos(currentRoverObjectName);
        }
    }

    public void showToastResults(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public RoverAdapter getRecyclerAdapter() {
        return (RoverAdapter) recyclerView.getAdapter();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_curiosity:
                switchCollection(getString(R.string.rover_curiosity));
                break;
            case R.id.nav_opportunity:
                switchCollection(getString(R.string.rover_opportunity));
                break;
            case R.id.nav_spirit:
                switchCollection(getString(R.string.rover_spirit));
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this,
                        Settings.class);
                startActivity(settingsIntent);
                break;
            default:
                switchCollection(getString(R.string.rover_curiosity));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressWarnings("deprecation")
    private void switchCollection(String roverName) {
        collectPhotos(roverName.toLowerCase());
        setTitle(String.format("MarsRover - %s", roverName));
        editor.putString(getString(R.string.preffered_rover_key), roverName);
        editor.apply();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_ROVERNAME, roverName);
        bundle.putString("type", Constants.TYPE_PHOTOS);
        getSupportLoaderManager().restartLoader(20, bundle, this);
    }

    @NonNull
    @Override
    public Loader<Rover[]> onCreateLoader(int i, @Nullable Bundle bundle) {
        APICycle cycle = new APICycle(this, Objects.requireNonNull(bundle));
        cycle.setListener(this);
        return cycle;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Rover[]> loader, Rover[] obtainedRovers) {
        rovers = obtainedRovers;
        hideProgressBar();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Rover[]> loader) {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Loader was reset");
    }

    @Override
    public void showProgressBar() {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Showing progressbar");
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar() {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Hiding progressbar and showing results");
        loadingIndicator.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        showToastResults(String.format("Loaded %s of %s photos for %s",
                ((RoverAdapter) mAdapter).getDisplayedItems(),
                ((RoverAdapter) mAdapter).getDataSet().size(),
                ((RoverAdapter) mAdapter).getRoverName()));
    }
}
