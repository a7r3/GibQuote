package com.arvind.quote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quote.R;

import org.json.JSONObject;

public class GibWidget extends AppWidgetProvider {

    private static String ACTION_UPDATE_WIDGET = "update_widget";
    private final String TAG = getClass().getSimpleName();
    private final String EXTRA_WIDGET_ID = "WIDGET_ID";

    public void updateGibWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        // Send a Broadcast to update the widget's content
        Intent broadcastIntent = new Intent(context, GibWidget.class);
        broadcastIntent.putExtra(EXTRA_WIDGET_ID, appWidgetId);
        broadcastIntent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Issue the intent when a click is made on these RemoteViews
        remoteViews.setOnClickPendingIntent(R.id.widget_quote_view, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // For all widgets
        for(int appWidgetId : appWidgetIds) {
            // Update each of them
            updateGibWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        if(!intent.getAction().equals(ACTION_UPDATE_WIDGET))
            return;

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        Log.i(TAG, "Showing QoTD in Widget");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://quotes.rest/qod",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject quoteObject = response.getJSONObject("contents").getJSONArray("quotes").getJSONObject(0);
                            String authorText = quoteObject.getString("author");
                            String quoteText = quoteObject.getString("quote");
                            // Get all the Widget's Instances
                            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                            Log.d(TAG, "GibWidget || " + authorText + " || " + quoteText);
                            // Change the Layout's Text
                            remoteViews.setTextViewText(R.id.quote_text_view, quoteText);
                            remoteViews.setTextViewText(R.id.author_text_view, authorText);
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            // Update this change in the current widget
                            // ... The Widget which was clicked to send a Broadcast
                            appWidgetManager.updateAppWidget(intent.getIntExtra(EXTRA_WIDGET_ID, -1), remoteViews);
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
