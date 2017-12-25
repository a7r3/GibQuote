package com.arvind.quote;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class UpdaterUtils {

    private String currentVersion;
    private String TAG = "UpdaterUtils";
    private StringBuilder changeLogMessage;
    private String interTagUrl;
    private RequestQueue requestQueue;

    private boolean updateAvailable = false;

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public UpdaterUtils(Context context) {
        currentVersion = "v" + BuildConfig.VERSION_NAME;
        requestQueue = Volley.newRequestQueue(context);
        changeLogMessage = new StringBuilder();
        checkForUpdates();
    }

    public StringBuilder getChangeLogMessage() {
        return changeLogMessage;
    }

    public void checkForUpdates() {
        JsonArrayRequest latestTagRequest = new JsonArrayRequest(
                Request.Method.GET,
                "https://api.github.com/repos/a7r3/GibQuote/git/refs/tags",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            int currentVersionPosition;
                            for(currentVersionPosition = 0; currentVersionPosition < response.length(); currentVersionPosition++) {
                                String interVersion = response
                                        .getJSONObject(currentVersionPosition)
                                        .getString("ref")
                                        .replaceAll(".*/", "");
                                Log.d(TAG, interVersion);
                                if(interVersion.equals(currentVersion))
                                    break;
                            }
                            // If currentVersion is at the End, we're updated
                            if(currentVersionPosition == response.length() - 1)
                                Log.d(TAG, "We're updated");
                            else {
                                obtainTagMessages(currentVersionPosition, response);
                                updateAvailable = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "Y u gab 404 link");
                    }
                }
        );

        requestQueue.add(latestTagRequest);

    }

    private void obtainTagMessages(int currentVersionPosition, JSONArray response) {
        // Obtain each Tag's SHA, and then Obtain Tag's message
        for(int j = currentVersionPosition; j <= response.length(); j++) {
            try {
                interTagUrl = response
                        .getJSONObject(j)
                        .getJSONObject("object")
                        .getString("url");
            } catch(Exception e) {
                e.printStackTrace();
            }
            // Create a tag request with the obtained Tag URL, and obtain Tag Message
            JsonObjectRequest interTagRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    interTagUrl,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                changeLogMessage.append(response.getString("message"));
                                changeLogMessage.append("\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Umm, rip");
                        }
                    }
            );
            requestQueue.add(interTagRequest);
        }
        Log.d(TAG, "Final Message\n\n" + changeLogMessage);
    }
}
