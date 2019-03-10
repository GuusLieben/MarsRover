package com.exludit.marsrover;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;

import com.exludit.marsrover.adapters.RoverAdapter;
import com.exludit.marsrover.async.ApiCollector;
import com.exludit.marsrover.async.TaskTypes;
import com.exludit.marsrover.objects.Rover;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RoverActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static Rover[] rovers;
    private static final String PREFERENCES = "MarsRoverPreferences";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private final String logClass = this.getClass().getName();

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.main_recycler);
        Log.d(logClass, "Content view and RecyclerView loaded");

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Log.d(logClass, "Successfully obtained navigation Views");

        editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
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
        }
        else {
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
        collectRovers();
        setTitle(String.format("MarsRover - %s", roverName));
        collectPhotos(roverName);
    }

    private void collectRovers() {
        if (rovers == null) {
            ApiCollector api = new ApiCollector(this, null);
            api.execute(TaskTypes.ROVERS);
            try {
                rovers = api.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(logClass, e.getMessage());
            }
        }
    }

    private void collectPhotos(String currentRoverObjectName) {
        Rover rover = Rover.getByName(currentRoverObjectName);
        if (rover != null) {
            ApiCollector api = new ApiCollector(this, rover);

            api.execute(TaskTypes.PHOTOS);
            recyclerView.setAdapter(mAdapter);
            ((RoverAdapter) mAdapter).setRoverName(rover.getName());

            ((RoverAdapter) Objects.requireNonNull(recyclerView.getAdapter())).swapItems(rover.getPhotos());
        } else {
            collectRovers();
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
                switchCollection("Opportunity");
                break;
            case R.id.nav_spirit:
                switchCollection("Spirit");
                break;
            default:
                switchCollection(getString(R.string.rover_curiosity));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchCollection(String roverName) {
        collectPhotos(roverName.toLowerCase());
        setTitle(String.format("MarsRover - %s", roverName));
        editor.putString(getString(R.string.preffered_rover_key), roverName);
        editor.apply();
    }
}
