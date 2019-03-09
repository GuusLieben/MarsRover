package com.exludit.marsrover;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.exludit.marsrover.adapters.RoverAdapter;
import com.exludit.marsrover.async.ApiCollector;
import com.exludit.marsrover.async.TaskTypes;
import com.exludit.marsrover.objects.RoverObject;

import java.util.concurrent.ExecutionException;

public class RoverActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static RoverObject[] rovers;
    private static final String PREFERENCES = "MarsRoverPreferences";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.main_recycler);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            layoutManager = new GridLayoutManager(this, 2);
        else
            layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RoverAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        String rover = preferences.getString("rover", "Curiosity");

        ApiCollector api = new ApiCollector(this, null);
        collectRovers();
        api.getRovers();

        setTitle(String.format("MarsRover - %s", rover));

        collectPhotos(rover);
    }

    private void collectRovers() {
        if (rovers == null) {
            ApiCollector api = new ApiCollector(this, null);
            api.execute(TaskTypes.ROVERS);
            try {
                rovers = api.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void collectPhotos(String currentRoverObjectName) {
        RoverObject roverObject = RoverObject.getByName(currentRoverObjectName);
        if (roverObject != null) {
            ApiCollector api = new ApiCollector(this, roverObject);

            api.execute(TaskTypes.PHOTOS);
            recyclerView.setAdapter(mAdapter);
            ((RoverAdapter) mAdapter).setRoverName(roverObject.getName());

            ((RoverAdapter) recyclerView.getAdapter()).swapItems(roverObject.getPhotos());
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case (R.id.nav_curiosity):
                collectPhotos("curiosity");
                setTitle("MarsRover - Curiosity");
                editor.putString("rover", "Curiosity");
                editor.apply();
                break;
            case (R.id.nav_opportunity):
                collectPhotos("opportunity");
                setTitle("MarsRover - Opportunity");
                editor.putString("rover", "Opportunity");
                editor.apply();
                break;
            case (R.id.nav_spirit):
                collectPhotos("spirit");
                setTitle("MarsRover - Spirit");
                editor.putString("rover", "Spirit");
                editor.apply();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
