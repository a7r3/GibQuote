package com.arvind.quote;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.arvind.quote.Auth.APP_KEY_QUOTE;

public class MainActivity extends AppCompatActivity {

    public static boolean isRequested = false;
    public static String TAG = "GibQuote";
    public RequestQueue requestQueue;
    public JsonObjectRequest jsonObjectRequest;
    private GestureDetector mGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isNetworkAvailable()) {
            Log.v(TAG, "INTERNET CONNECTIVITY : OK");
            Toast.makeText(this, "Connected to Internet", Toast.LENGTH_SHORT).show();
        } else {
            Log.v(TAG, "DAM SON :(");
            Toast.makeText(this, "Internet Connection Required", Toast.LENGTH_LONG).show();
        }

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

        AnimationDrawable anim = (AnimationDrawable) rootLayout.getBackground();
        anim.setEnterFadeDuration(1000);
        anim.setExitFadeDuration(1000);
        // Start the animating background
        anim.start();

        SwipeListener swipeListener = new SwipeListener();
        mGestureDetector = new GestureDetector(this, swipeListener);

        requestQueue = Volley.newRequestQueue(this);

        generateStuffs(rootLayout);
    }

    /* To Check if we're connected to the Internet */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* Generates a Random number to be sent as the Quote's ID */
    public Integer generateRandomNumber() {
        int MIN = 1;
        // Yeah! This was the Number of Quotes as on 19/09/17
        int MAX = 62024;
        return new Random().nextInt((MAX - MIN) + 1) + MIN;
    }

    /* Allows the user to share currently displayed quote */
    public void shareQuote(View view) {

        TextView quoteTextView = (TextView) findViewById(R.id.quote_text_view);
        TextView authorTextView = (TextView) findViewById(R.id.author_text_view);

        // My intention is to send (throw) a piece of Text (ball)
        Intent quoteIntent = new Intent(Intent.ACTION_SEND);
        // Piece of Text (the Ball)
        String quoteMessage = quoteTextView.getText() + "\n-- " + authorTextView.getText();
        // Specify the Text to be thrown
        quoteIntent.putExtra(Intent.EXTRA_TEXT, quoteMessage);
        // Specify the MIME type of the object to be thrown
        quoteIntent.setType("text/plain");
        // Send an Acknowledgement
        Toast.makeText(this, "Select the App to share this Quote", Toast.LENGTH_SHORT).show();
        // Throw the Ball!
        startActivity(Intent.createChooser(quoteIntent, "Share this Quote"));
    }

    private void generateStuffs(View view) {
        TextView quoteTextView = (TextView) findViewById(R.id.quote_text_view);
        TextView authorTextView = (TextView) findViewById(R.id.author_text_view);
        TextView tagsTextView = (TextView) findViewById(R.id.tags_text_view);
        ImageButton shareImage = (ImageButton) findViewById(R.id.share_image_view);
        AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_view);

        // Hide 'em views
        quoteTextView.setVisibility(View.GONE);
        authorTextView.setVisibility(View.GONE);
        tagsTextView.setVisibility(View.GONE);
        shareImage.setVisibility(View.GONE);

        // Set Loading Indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingIndicator.smoothToShow();

        // Creating a new GET JSONObject request
        //
        // Params
        // The Request method - GET
        // The Request URL - a random quote's URL
        // A Listener for performing actions on successfully obtaining a JSON Object
        // A Listener for performing actions if an error occurs while requesting
        //
        // Additionally, a method of JsonObjectRequest - getHeaders() - has been overridden to provide
        // the request headers (Content Type & Authorization token)

        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://favqs.com/api/quotes/" + generateRandomNumber().toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, response.toString());

                        TextView quoteTextView = (TextView) findViewById(R.id.quote_text_view);
                        TextView authorTextView = (TextView) findViewById(R.id.author_text_view);
                        TextView tagsTextView = (TextView) findViewById(R.id.tags_text_view);
                        ImageButton shareImage = (ImageButton) findViewById(R.id.share_image_view);
                        AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_view);

                        try {
                            String quoteText = response.getString("body");
                            String authorText = response.getString("author");
                            JSONArray tagText = response.getJSONArray("tags");

                            StringBuilder tags = new StringBuilder("");
                            for (int i = 0; i < tagText.length(); i++) {
                                tags.append(tagText.get(i));
                                if (i != tagText.length() - 1) {
                                    tags.append(", ");
                                }
                            }

                            // Set Obtained details
                            quoteTextView.setText(quoteText);
                            authorTextView.setText(authorText);
                            tagsTextView.setText(tags.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Hide the loading indicator, it's done
                        loadingIndicator.hide();
                        loadingIndicator.setVisibility(View.GONE);

                        // Bring 'em views back
                        quoteTextView.setVisibility(View.VISIBLE);
                        authorTextView.setVisibility(View.VISIBLE);
                        tagsTextView.setVisibility(View.VISIBLE);
                        shareImage.setVisibility(View.VISIBLE);

                        // We're open for requests :D
                        isRequested = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                }
        ) {
            // Optional method to pass Request Headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Token token=\"" + APP_KEY_QUOTE + "\"");
                return params;
            }
        };

        // Add the request to Volley's Request Queue
        // RequestQueue executes any number of requests, asynchronously
        requestQueue.add(jsonObjectRequest);

    }

    public class SwipeListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

            // TODO: Accept a Request only after receiving a considerable fling length
            if (!isRequested) {
                generateStuffs(rootLayout);
                Log.d(TAG, "Received Gesture, Fetching Quote");
                isRequested = true;
            } else {
                Log.d(TAG, "Request is being processed");
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
