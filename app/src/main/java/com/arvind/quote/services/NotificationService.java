package com.arvind.quote.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quote.adapter.Quote;
import com.arvind.quote.utils.notification.NotificationUtils;

import org.json.JSONObject;

public class NotificationService extends IntentService {

    private String TAG = "NotificationService";

    private String authorText, quoteText;
    private NotificationUtils notificationUtils;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notificationUtils = NotificationUtils.getInstance(NotificationService.this);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Log.i(TAG, "Showing up Notification");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://quotes.rest/qod",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject quoteObject = response.getJSONObject("contents").getJSONArray("quotes").getJSONObject(0);
                            authorText = quoteObject.getString("author");
                            quoteText = quoteObject.getString("quote");
                            notificationUtils.issueNotification(new Quote(quoteText, authorText));
                            Log.d(TAG, "QoTD || " + authorText + " || " + quoteText);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "404 Quote not found");
                        Log.e(TAG, error.toString());
                    }
                }
        );
        // Add the request to Volley's Request Queue
        requestQueue.add(jsonObjectRequest);

    }

}
