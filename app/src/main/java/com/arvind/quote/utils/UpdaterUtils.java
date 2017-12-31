package com.arvind.quote.utils;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
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

import java.io.File;

public class UpdaterUtils {

    private String TAG = "UpdaterUtils";

    // The number of Tag Requests
    private int tagRequestCount = 0;
    // The number of successful Volley requests
    private int successfulRequestCount = 0;
    // Current version of the application
    private String currentVersion;
    // Updated version of the application
    private String updatedVersion;

    // Interface Instances (not Instantiated)
    private UpdateAvailable updateAvailable;
    private MessageRequest messageRequestListener;

    // Intermediate Version Information
    private int interVersionPosition;
    private String interTagUrl;

    private RequestQueue requestQueue;
    // The Activity in which UpdaterUtils instance may be created
    private Activity activity;
    // Context of the Activity
    private Context context;
    private BroadcastReceiver downloadBroadcastReceiver;

    // API Endpoint to request a list of Git Tags
    private String tagsUrl = "https://api.github.com/repos/a7r3/GibQuote/git/refs/tags";

    // The ChangeLog message
    private String[] changeLogMessage;

    public UpdaterUtils(Context context, Activity activity) {
        // Get Activity context
        this.context = context;
        this.activity = activity;
        // Get Current Version of App
        currentVersion = "v" + BuildConfig.VERSION_NAME;
        // Create a new Volley Request Queue
        requestQueue = Volley.newRequestQueue(context);
        // Method to check for updates
        checkForUpdates();
    }

    public String[] getChangeLogMessage() {
        return changeLogMessage;
    }

    private int getTagRequestCount() {
        return tagRequestCount;
    }

    public String getUpdatedVersion() {
        return updatedVersion;
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
                                    .getJSONObject(response.length() - 1)
                                    .getString("ref")
                                    .replaceAll(".*/", "");

                            // Variable to hold current Tag's position in the list of tags
                            int currentVersionPosition;

                            // Get Position of currentVersion Tag in the tags List
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
                                        "We're updated!",
                                        Toast.LENGTH_LONG).show();
                            } else { // We're not
                                // The total number of Tag Requests
                                tagRequestCount = (response.length() - 1) + currentVersionPosition;
                                // Creating changeLog message array
                                changeLogMessage = new String[getTagRequestCount()];
                                Log.d(TAG, "Number of Requests : " + getTagRequestCount());
                                // Obtain messages of tags which are ahead of current tag
                                // Contains multiple Volley Requests which are to be executed
                                // ... only if this request is successful. This explains the
                                // ... presence of this call in onResponse (Success)
                                obtainTagMessages(currentVersionPosition, response);
                                // Call the interface's method, which has to be implemented
                                // in activity. Way of notifying is up to the user
                                if (updateAvailable != null)
                                    updateAvailable.onUpdateAvailable();
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
        // Add a requestFinished listener
        // Use this to count the number of successful requests
        // If it equals the total requests sent by us, we're done
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<JsonArrayRequest>() {
            @Override
            public void onRequestFinished(Request<JsonArrayRequest> request) {
                if (request.hasHadResponseDelivered()) {
                    successfulRequestCount++;
                    Log.d(TAG, "Successful Requests : " + successfulRequestCount + ", Total TagRequests : " + tagRequestCount);
                    // successfulRequestCount includes
                    // > The Tags request (1)
                    // > Individual Tag Request
                    // That explains the decrement
                    if (successfulRequestCount - 1 == tagRequestCount) {
                        // If Listener instance is created, call the Listener's method
                        if (messageRequestListener != null)
                            messageRequestListener.onMessageFetchComplete();
                    }
                }
            }
        });
    }

    // Obtain each Tag's SHA, and then Obtain Tag's message
    private void obtainTagMessages(int currentVersionPosition, JSONArray response) {
        for (interVersionPosition = currentVersionPosition + 1; interVersionPosition < response.length(); interVersionPosition++) {
            try {
                // Get Version tag's URL
                interTagUrl = response.getJSONObject(interVersionPosition)
                        .getJSONObject("object")
                        .getString("url");
                Log.d(TAG, interVersionPosition + ": " + interTagUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add above intermediate tag request
            requestQueue.add(getInterTagRequest(
                    interTagUrl,
                    interVersionPosition - currentVersionPosition - 1
                    )
            );
        }
    }

    private JsonObjectRequest getInterTagRequest(String interTagUrl, final int interVersionPosition) {
        // Create a tag request with the obtained Tag URL, and obtain Tag Message
        return new JsonObjectRequest(
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
                            // Place version's changelog in respective order of occurrence
                            changeLogMessage[interVersionPosition] = "\n" + responseWithoutKey[0];
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

    }

    // Updates pls!
    public void updateApplication(final SharedPreferences sharedPreferences) {
        // Place the APK in Downloads
        final String apkDestination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/gib.apk";
        // Uri, required by downloadRequest
        final Uri apkUri = Uri.parse("file://" + apkDestination);
        // Download URL of newer APK
        String downloadURL = "https://github.com/a7r3/GibQuote/releases/download/" + updatedVersion + "/app-debug.apk";
        // Create a new DownloadManager request for this APK
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadURL));
        // Title, On the left side of Download Notification
        downloadRequest.setTitle("GibQuote " + updatedVersion);
        // Description, On the right side of Download Notification
        downloadRequest.setDescription("Downloading Latest APK");
        // It's an APK, so specify the MIME type
        downloadRequest.setMimeType("application/vnd.android.package-archive");
        // Show download progress on notification
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        // Download the APK only when on Wi-Fi or Mobile Data (without Metered Limits)
        downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // Directory where file would be saved
        downloadRequest.setDestinationUri(apkUri);
        // Get DownloadManager here
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // Give this request to the Manager
        // And get the downloadId, required by BroadcastReceiver
        // to check if this particular download is complete
        sharedPreferences.edit()
                .putLong("downloadId", downloadManager.enqueue(downloadRequest))
                .apply();
        // Tell the user that the file is being downloaded
        // Context-Registered Broadcast Receiver
        // Receives Broadcasts from System until the App is killed
        downloadBroadcastReceiver = new BroadcastReceiver() {
            long downloadId = sharedPreferences.getLong("downloadId", DownloadManager.ERROR_UNKNOWN);

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && downloadId != DownloadManager.ERROR_UNKNOWN) {
                    // Create a DownloadManager Query
                    DownloadManager.Query query = new DownloadManager.Query();
                    // Use this query, and get our download's information, by its downloadId
                    query.setFilterById(downloadId);
                    // Iterating through the Information by cursor, while supplying the query
                    Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
                    // If the cursor moves to the first column successfully (No null column)
                    if (cursor.moveToFirst()) {
                        // Get the Download Status Column
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        // Get the Status of our download in this column
                        if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            Log.d(TAG, "Download Complete : " + apkDestination);
                            Uri fileUri = FileProvider.getUriForFile(context,
                                    // Mention the FileProvider class which we've created in the Manifest
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    new File(apkDestination)
                            );
                            // Create a Viewer Intent
                            // By mentioning the MIME type as the one of an APK,
                            // the file to be "viewed" would be treated as an APK
                            Intent packageInstallerIntent = new Intent(Intent.ACTION_VIEW);
                            // Provide the APK with its MIME type
                            packageInstallerIntent.setDataAndType(fileUri, downloadManager.getMimeTypeForDownloadedFile(downloadId));
                            // Destroy the Activity, and clear all Tasks before launching this Intent
                            packageInstallerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            // Allow Intent to read (parse) the URI
                            packageInstallerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            // Ship it!
                            context.startActivity(packageInstallerIntent);
                            // Buhbye! we're updating
                            activity.finish();
                        }
                    }
                }
            }
        };
        Log.d(TAG, "Registering Download Broadcast Receiver");
        context.registerReceiver(downloadBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    // Method to un-register download broadcast receiver
    public void unregisterDownloadReceiver() {
        Log.d(TAG, "Un-registering Download Broadcast Receiver");
        if (downloadBroadcastReceiver != null)
            context.unregisterReceiver(downloadBroadcastReceiver);
    }

    public void setUpdateAvailableListener(UpdateAvailable updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public void setMessageRequestListener(MessageRequest messageRequestListener) {
        this.messageRequestListener = messageRequestListener;
    }

    public interface UpdateAvailable {
        void onUpdateAvailable();
    }

    public interface MessageRequest {
        void onMessageFetchComplete();
    }
}
