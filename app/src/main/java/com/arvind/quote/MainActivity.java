package com.arvind.quote;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import static com.arvind.quote.Auth.APP_KEY_QUOTE;

public class MainActivity extends AppCompatActivity {

    public static boolean isRequested = false;

    public static String TAG = "QuoteApp";
    
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
        generateStuffs(rootLayout);

        AnimationDrawable anim = (AnimationDrawable) rootLayout.getBackground();
        anim.setEnterFadeDuration(1000);
        anim.setExitFadeDuration(1000);
        // Start the animating background
        anim.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

        switch(event.getAction()) {
            case(MotionEvent.ACTION_MOVE):
                if(!isRequested) {
                    generateStuffs(rootLayout);
                    Log.d(TAG, "Received Gesture, Fetching Quote");
                    isRequested = true;
                }
                else {
                    Log.d(TAG, "Request is being processed");
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
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
        new Quote().execute("https://favqs.com/api/quotes/" + generateRandomNumber().toString());
    }

    class Quote extends AsyncTask<String,String,String> {

        TextView quoteTextView = (TextView) findViewById(R.id.quote_text_view);
        TextView authorTextView = (TextView) findViewById(R.id.author_text_view);
        TextView tagsTextView = (TextView) findViewById(R.id.tags_text_view);
        ImageButton shareImage = (ImageButton) findViewById(R.id.share_image_view);
        AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_view);

        // Wallpaper to be applied to RelativeLayout
        // Bad Wallpaper API, commented it for now
        // Bitmap bmp;
        // ImageView bgImage = (ImageView) findViewById(R.id.bg_image);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Hide 'em views
            quoteTextView.setVisibility(View.GONE);
            authorTextView.setVisibility(View.GONE);
            tagsTextView.setVisibility(View.GONE);
            shareImage.setVisibility(View.GONE);

            // Set Loading Indicator
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicator.smoothToShow();
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);

            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                String quoteText = jsonObject.getString("body");
                String authorText = jsonObject.getString("author");
                JSONArray tagText = jsonObject.getJSONArray("tags");

                StringBuilder tags = new StringBuilder("");
                for(int i = 0; i < tagText.length(); i++) {
                    tags.append(tagText.get(i));
                    if(i != tagText.length() - 1) {
                        tags.append(", ");
                    }
                }

                // Set Obtained details
                quoteTextView.setText(quoteText);
                authorTextView.setText(authorText);
                tagsTextView.setText(tags.toString());

            } catch(Exception e) {
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

        @Override
        protected String doInBackground(String... strings) {
            // Object to execute HTTPS methods
            HttpsURLConnection connection = null;
            // Buffer to read the Stream
            BufferedReader reader = null;

            StringBuilder stringBuilder;

            try {
                // Get the URL passed
                URL url = new URL(strings[0]);

                // Create a HTTPS Connector Object
                connection = (HttpsURLConnection) url.openConnection();
                // Connect with these 'Header' parameters
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Token token=\"" + APP_KEY_QUOTE + "\"");

                // Get Input Stream from API
                InputStream stream = connection.getInputStream();

                // Buffer to read the above InputStream
                reader = new BufferedReader(new InputStreamReader(stream));

                // Buffer which would be returned back as a Pretty JSON String
                stringBuilder = new StringBuilder("");

                // Variable to read each line of Stream
                String line;

                // while the stream isn't completely read
                while ((line = reader.readLine()) != null) {
                    // Append every line to Buffer, and add a NewLine
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                    // Log the same
                    Log.d(TAG, "QuoteJSONResponse > " + line);
                }

                return stringBuilder.toString();
            }
            // Possible Unhandled Exception (Runtime)
            catch (MalformedURLException m) {
                m.printStackTrace();
            }
            // Possible Unhandled Exception (Runtime)
            catch (IOException i) {
                i.printStackTrace();
            }
            finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

}