package com.arvind.quote.utils;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quote.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

public class UpdaterUtils {

    private String TAG = "UpdaterUtils";

    private String currentVersion;
    private String updatedVersion;

    private String interTagUrl;
    private RequestQueue requestQueue;
    private Context context;

    private String tagsUrl = "https://api.github.com/repos/a7r3/GibQuote/git/refs/tags";

    ////////////////////////////////
    // ChangeLog Message Listener //
    ////////////////////////////////

    private String changeLogMessage = "";
    // Interface Object
    private ChangeLog changeLog;
    private boolean isUpdateAvailable = false;
    // Interface Object
    private UpdateAvailable updateAvailable;

    public UpdaterUtils(Context context) {
        // Get Activity context
        this.context = context;
        // Get Current Version of App
        currentVersion = "v" + BuildConfig.VERSION_NAME;
        // Create a new Volley Request Queue
        requestQueue = Volley.newRequestQueue(context);
        // Method to check for updates
        checkForUpdates();
    }

    public String getUpdatedVersion() {
        return updatedVersion;
    }

    //////////////////////
    // Boolean Listener //
    //////////////////////

    public void setChangeLogMessageListener(ChangeLog changeLog) {
        this.changeLog = changeLog;
    }

    public void setUpdateAvailableListener(UpdateAvailable updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    // When update is available, this boolean is set to true
    // onChange() method in Interface object is called
    // with the updated value
    private void setUpdateAvailable() {
        this.isUpdateAvailable = true;
        // Notify the listener with the method, and an updated value passed to that method
        if (updateAvailable != null)
            updateAvailable.onChange(getUpdateAvailable());
    }

    private boolean getUpdateAvailable() {
        return isUpdateAvailable;
    }

    private String getChangeLogMessage() {
        return changeLogMessage;
    }

    // ChangeLog message setter //
    // This also calls the Interface's onChange method
    // Which is expected to be implemented by another class
    private void setChangeLogMessage(String newUpdate) {
        changeLogMessage = newUpdate;
        // Inform the interface that the value has changed
        if (changeLog != null)
            changeLog.onChange(getChangeLogMessage());
    }

    private void checkForUpdates() {

        JsonArrayRequest tagsRequest = new JsonArrayRequest(
                Request.Method.GET,
                tagsUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            updatedVersion = response
                                    .getJSONObject(response.length() -1)
                                    .getString("ref")
                                    .replaceAll(".*/", "");

                            // Get Position of currentVersion Tag in the tags List
                            int currentVersionPosition;

                            for (currentVersionPosition = 0; currentVersionPosition < response.length(); currentVersionPosition++) {
                                String interVersion = response
                                        .getJSONObject(currentVersionPosition)
                                        .getString("ref")
                                        .replaceAll(".*/", "");
                                // If the intermediate version equals current, then break the loop
                                if (interVersion.equals(currentVersion))
                                    break;
                            }
                            // If currentVersion is at the End, we're updated
                            if (currentVersionPosition == response.length() - 1) {
                                Log.d(TAG, "We're updated");
                                Toast.makeText(context,
                                        "NO Updates",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                // Obtain messages of tags which are ahead of current tag
                                // Contains multiple Volley Requests which are to be executed
                                // ... only if this request is successful. This explains the
                                // ... presence of this call in onResponse (Success)
                                obtainTagMessages(currentVersionPosition, response);
                                // Fire up the alert dialog
                                setUpdateAvailable();
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
                    }
                }
        );
        // Add the request to Queue
        requestQueue.add(tagsRequest);
    }

    // Obtain each Tag's SHA, and then Obtain Tag's message
    private void obtainTagMessages(int currentVersionPosition, JSONArray response) {
        for (int j = currentVersionPosition + 1; j < response.length(); j++) {
            try {
                // Get Version tag's URL
                interTagUrl = response.getJSONObject(j)
                        .getJSONObject("object")
                        .getString("url");
                Log.d(TAG, j + ": " + interTagUrl);
            } catch (Exception e) {
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
                                // Umm, since I sign the tags
                                // The Commit message would contain the public GPG key signature too
                                // Split the String by the start of GPG key signature viz mentioned
                                // as an argument to 'split()'
                                String[] responseWithoutKey = response.getString("message").split("-----BEGIN PGP SIGNATURE-----");
                                // Update the changeLog message
                                setChangeLogMessage("\n" + responseWithoutKey[0] + getChangeLogMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            );
            // Add above intermediate tag request
            requestQueue.add(interTagRequest);
        }
    }

    public interface ChangeLog {
        void onChange(String newChangeLog);
    }

    public interface UpdateAvailable {
        void onChange(boolean isUpdateAvailable);
    }
}
