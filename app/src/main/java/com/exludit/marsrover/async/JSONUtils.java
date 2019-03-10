package com.exludit.marsrover.async;

import android.net.Uri;
import android.util.Log;

import com.exludit.marsrover.domain.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class JSONUtils {

    static JSONObject getJSONObjectFromUrl(String uri) {
        URLConnection connection;
        BufferedReader reader = null;
        try {
            Log.d(Constants.JSON_UTILS_TAG, String.format("Attempting to parse %s", uri));
            Uri builtUri = Uri.parse(uri).buildUpon()
                    .appendQueryParameter(Constants.PARAM_SOL, String.valueOf(Constants.VALUE_SOL))
                    .appendQueryParameter(Constants.PARAM_KEY, Constants.VALUE_KEY)
                    .build();

            Log.d(Constants.JSON_UTILS_TAG, String.format("Successfully parsed Uri to : %s", builtUri.toString()));

            URL url = new URL(builtUri.toString());
            connection = url.openConnection();
            Log.d(Constants.JSON_UTILS_TAG, "Successfully opened connection to API");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) stringBuilder.append(line);

            return new JSONObject(stringBuilder.toString());
        } catch (IOException | JSONException e) {
            Log.e(Constants.JSON_UTILS_TAG, e.getMessage());
            for (StackTraceElement el : e.getStackTrace())
                Log.e(Constants.JSON_UTILS_TAG, el.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(Constants.JSON_UTILS_TAG, e.getMessage());
                    for (StackTraceElement el : e.getStackTrace())
                        Log.e(Constants.JSON_UTILS_TAG, el.toString());
                }
            }
        }
        return new JSONObject(); // Never return null, please
    }

}
