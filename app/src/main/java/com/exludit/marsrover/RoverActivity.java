package com.exludit.marsrover;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.exludit.marsrover.adapters.RoverAdapter;
import com.exludit.marsrover.async.APICycle;
import com.exludit.marsrover.domain.Constants;
import com.exludit.marsrover.domain.Rover;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Locale;
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

    private String currentRover;

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

        Locale current = getResources().getConfiguration().locale;
        if (!current.toLanguageTag().split("-")[0].equals(preferences.getString(Constants.LANG_PREFERENCE, "en"))) {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Detected Locale was not the preferred setting, switching from %s to %s",
                    current.toLanguageTag().split("-")[0],
                    preferences.getString(Constants.LANG_PREFERENCE, "something")));
            switchLocale(preferences.getString(Constants.LANG_PREFERENCE, "en"));
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Inflating toolbar menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app_bar, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(Constants.MAINACTIVITY_LOG_TAG, "onCreate succeeded, performing Resume actions");
        String roverName = preferences.getString(Constants.ROVER_PREFERENCE, getString(R.string.rover_curiosity));
        currentRover = roverName;
        collectRovers(roverName);
        setTitle(String.format("MarsRover - %s", roverName));
        collectPhotos(roverName);
    }

    @SuppressWarnings("deprecation")
    private void collectRovers(String roverName) {
        if (rovers == null) {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Rover doesn't exist yet, requesting API Cycle to collect %s", roverName));
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
            Log.d(Constants.MAINACTIVITY_LOG_TAG, "Rover exists, swapping Adapter Data Set");
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BUNDLE_ROVERNAME, currentRoverObjectName);
            bundle.putString("type", Constants.TYPE_PHOTOS);
            getSupportLoaderManager().initLoader(20, bundle, this);

            recyclerView.setAdapter(mAdapter);
            ((RoverAdapter) mAdapter).setRoverName(rover.getName());

            ((RoverAdapter) Objects.requireNonNull(recyclerView.getAdapter())).swapItems(rover.getPhotos());
        } else {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, "Rover was null, collecting all data");
            collectRovers(currentRoverObjectName);
            collectPhotos(currentRoverObjectName);
        }
    }

    public void showToastResults() {
        if (((RoverAdapter) mAdapter).getDisplayedItems() != 0) {
            Rover rover = Rover.getByName(currentRover);
            String message = String.format(getString(R.string.load_photo_toast),
                    ((RoverAdapter) mAdapter).getDisplayedItems(),
                    ((RoverAdapter) mAdapter).getDataSet().size(),
                    WordUtils.capitalize(((RoverAdapter) mAdapter).getRoverName()),
                    String.valueOf(Objects.requireNonNull(rover).getMaxSol()));
            Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Requested Toast with message : %s", message));
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, "Amount of loaded images is zero, not showing Toast (yet)");
        }
    }

    public RoverAdapter getRecyclerAdapter() {
        return (RoverAdapter) recyclerView.getAdapter();
    }

    @Override
    public void onBackPressed() {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Back button pressed... performing action");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, "Closing navigation drawer");
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Log.d(Constants.MAINACTIVITY_LOG_TAG, "Forwarding button press to Super");
            super.onBackPressed();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save_rover:
                Log.d(Constants.MAINACTIVITY_LOG_TAG, "User selected Favorite Rover menu option, saving favorite rover");
                editor.putString(Constants.ROVER_PREFERENCE, currentRover);
                editor.apply();
                Toast.makeText(this, String.format(getString(R.string.save_toast), currentRover), Toast.LENGTH_SHORT).show();
                break;
            case R.id.switch_lang:
                Log.d(Constants.MAINACTIVITY_LOG_TAG, "User selected Language Switch menu option, displaying dialog");
                final Dialog dialog = new Dialog(this);
                dialog.setTitle(getString(R.string.switch_language));
                dialog.setContentView(R.layout.lang_dialog);

                RadioGroup rg = dialog.findViewById(R.id.radio_group);
                String[] languages = Constants.LANGUAGES;

                for (int i = 0, languagesLength = languages.length; i < languagesLength; i++) {
                    String lang = languages[i];
                    String langCode = Constants.LANGUAGE_CODES[i];
                    Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Constructing option %s [%s] for new Dialog option", lang, langCode));

                    RadioButton button = new RadioButton(this);
                    button.setText(String.format("%s. %s [%s]", i + 1, lang, langCode));
                    button.setHeight(TypedValue.COMPLEX_UNIT_SP * 50);

                    rg.addView(button);
                }

                rg.setOnCheckedChangeListener((group, checkedId) -> {
                    int childCount = group.getChildCount();
                    Log.d(Constants.MAINACTIVITY_LOG_TAG, "User selected a language");
                    for (int x = 0; x < childCount; x++) {
                        RadioButton btn = (RadioButton) group.getChildAt(x);

                        if (btn.getId() == checkedId) {
                            Log.d(Constants.MAINACTIVITY_LOG_TAG, btn.getText().toString());
                            String langCode = Constants.LANGUAGE_CODES[
                                    Integer.parseInt(
                                            btn.getText()
                                                    .toString()
                                                    .split("\\.")[0]
                                    ) - 1]
                                    .split("_")[0];
                            dialog.cancel();
                            switchLocale(langCode);
                        }
                    }
                });

                dialog.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private void switchLocale(String langCode) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Switching to Locale %s", langCode));
        conf.setLocale(new Locale(langCode.toLowerCase()));
        res.updateConfiguration(conf, dm);

        editor.putString(Constants.LANG_PREFERENCE, langCode);
        editor.apply();

        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Successfully switched Locale, recreating Activity");
        Intent roverMain = getIntent();
        finish();
        startActivity(roverMain);
        if (currentRover != null) switchCollection(currentRover);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("User selected the %s Rover from navigation drawer", item.getTitle()));

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
        Log.d(Constants.MAINACTIVITY_LOG_TAG, String.format("Requesting collection switch for Rover : %s", roverName));
        collectPhotos(roverName.toLowerCase());
        currentRover = roverName;
        setTitle(String.format("MarsRover - %s", roverName));
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_ROVERNAME, roverName);
        bundle.putString("type", Constants.TYPE_PHOTOS);
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Collected attributes, restarting cycle");
        getSupportLoaderManager().restartLoader(20, bundle, this);
    }

    @NonNull
    @Override
    public Loader<Rover[]> onCreateLoader(int i, @Nullable Bundle bundle) {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "New Cycle requested, creating...");
        APICycle cycle = new APICycle(this, Objects.requireNonNull(bundle));
        cycle.setListener(this);
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Successfully created Cycle and set Listener, returning...");
        return cycle;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Rover[]> loader, Rover[] obtainedRovers) {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Storing results in activity...");
        rovers = obtainedRovers;
        hideProgressBar();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Rover[]> loader) {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Loader was reset");
    }

    @Override
    public void showProgressBar() {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Showing loading indicator");
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar() {
        Log.d(Constants.MAINACTIVITY_LOG_TAG, "Hiding loading indicator and showing results");
        loadingIndicator.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(
                this::showToastResults
                , 250);
    }
}
