package com.arvind.quote.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.adapter.Quote;
import com.arvind.quote.adapter.QuoteAdapter;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.arvind.quote.Auth.APP_KEY_QUOTE;

public class GibQuoteFragment extends Fragment {

    private ArrayList<Quote> quoteArrayList = new ArrayList<>();
    private ArrayList<Quote> favQuoteArrayList = new ArrayList<>();
    private QuoteAdapter quoteAdapter;
    private RecyclerView quoteRecyclerView;
    private RequestQueue requestQueue;
    private String TAG = "GibQuoteFragment";
    private String quoteProvider;
    private String quoteUrl;
    private String quoteTextVarName;
    private String quoteAuthorVarName;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(savedInstanceState != null) {
            Log.d(TAG, "Restoring Instance state");
            quoteArrayList = savedInstanceState.getParcelableArrayList("quoteData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gib_quote_fragment, container, false);

        MainActivity.setActionBarTitle("GibQuote");

        // Get the Animation Drawable from RelativeLayout
        // Currently 'animation_list.xml'
        //
        // final RelativeLayout rootLayout = (RelativeLayout) view.findViewById(R.id.root_layout);
        // AnimationDrawable anim = (AnimationDrawable) rootLayout.getBackground();
        // anim.setEnterFadeDuration(1000);
        // anim.setExitFadeDuration(1000);
        // // Start the animating background
        // anim.start();

        // RequestQueue executes any number of requests, asynchronously
        // More of a Queue Manager for Volley
        requestQueue = Volley.newRequestQueue(getContext());

        // RecyclerView Object
        quoteRecyclerView = view.findViewById(R.id.quote_list_view);

        // RecyclerView's Adapter - Detects change on DataSet
        quoteAdapter = new QuoteAdapter(getContext(), quoteArrayList);

        // Allows Recycler to perform actions on the Layout
        // whenever the particular adapter detects a change
        quoteRecyclerView.setAdapter(quoteAdapter);

        // RecyclerView's Layout Manager
        // Used for viewing RecyclerView's nodes
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Reverses the Layout
        // Newer nodes come at the top
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        // Set Layout Manager for RecyclerView
        // Two Managers available: 1. LinearLayoutManager 2. StaggeredGridLayoutManager
        quoteRecyclerView.setLayoutManager(linearLayoutManager);

        // Add Default ItemDecoration
        // Used to Decorate every RecyclerView Item
        // Any interaction with the Item won't affect the decoration
        quoteRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // FAB to generate quote onClick
        final FloatingActionButton gibQuoteFab = view.findViewById(R.id.gib_quote_fab);

        if (isNetworkAvailable()) {
            Log.v(TAG, "INTERNET CONNECTIVITY : OK");
        } else {
            Log.v(TAG, "DAM SON :(");
            Snackbar.make(
                    view.findViewById(R.id.frame_layout),
                    "No Internet Connection",
                    Snackbar.LENGTH_LONG).show();
            quoteArrayList.add(new Quote("Always pay your Internet Bills on-time",
                    "Yours truly,\nI_Iz_N00b"));
            quoteAdapter.notifyItemInserted(quoteArrayList.size() - 1);
            quoteRecyclerView.smoothScrollToPosition(quoteArrayList.size() - 1);
            gibQuoteFab.setEnabled(false);
        }

        // Adding an Listener which ...
        // Hides the FAB when RecyclerView is scrolled down
        // Gets the FAB back when RecyclerView is scrolled up
        quoteRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                // dy - Instantaneous change in 'y' co-ordinate
                // Origin is at Top-Left corner
                // scrollDown -> Final 'y' co-ordinate is lesser than (or below) Initial 'y' co-ordinate
                //                which results in a positive difference
                // scrollUp -> Final 'y' Co-ordinate is greater than (or above) Initial 'y' co-ordinate
                //             which results in a Negative difference

                // If scrollDown && the FAB is visible
                if (dy > 0 && gibQuoteFab.getVisibility() == View.VISIBLE) {
                    // then hide it
                    gibQuoteFab.hide();
                    // Create a separate thread, which sleeps for 1000ms
                    // And shows the FAB after it wakes up :P
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                ie.printStackTrace();
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gibQuoteFab.show();
                                }
                            });
                        }
                    }).start();
                } // else If scrollUp && the FAB is not Visible
                else if (dy < 0 && gibQuoteFab.getVisibility() != View.VISIBLE) {
                    // then show that FAB
                    gibQuoteFab.show();
                }
            }
        });

        // generateStuffs on receiving a click on FAB
        gibQuoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateStuffs();
            }
        });

        // If no quotes are present
        if (sharedPreferences.getBoolean("IS_USER_INTRODUCED", false))
            Log.i(TAG, "User knows me already!");
        else {
            Log.i(TAG, "Let's Introduce ourselves");
            showIntroTapTargets(view);
        }

        Spinner providerSpinner = view.findViewById(R.id.provider_select);

        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                quoteProvider = adapterView.getSelectedItem().toString();
                changeProvider(quoteProvider);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> providerArrayAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                new String[]{ "Forismatic", "Talaikis", "Storm", "FavQs"}
        );

        providerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        providerSpinner.setAdapter(providerArrayAdapter);

        return view;
    }

    public void addToFavQuoteList(Context context, Quote quoteData) {
        favQuoteArrayList.add(quoteData);
        Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show();
    }

    private void changeProvider(String quoteProvider) {
        Log.d(TAG, "QuoteProvider: " + quoteProvider);

        // Randomness
        switch(quoteProvider) {
            case "FavQs":
                quoteUrl = "https://favqs.com/api/quotes/";
                quoteTextVarName = "body";
                quoteAuthorVarName = "author";
                break;
            case "Forismatic": default:
                quoteUrl = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json&key=";
                quoteTextVarName = "quoteText";
                quoteAuthorVarName = "quoteAuthor";
                break;
            case "Storm":
                quoteUrl = "http://quotes.stormconsultancy.co.uk/random.json";
                quoteTextVarName = "quote";
                quoteAuthorVarName = "author";
                break;
            case "Talaikis":
                quoteUrl = "https://talaikis.com/api/quotes/random/";
                quoteTextVarName = "quote";
                quoteAuthorVarName = "author";
                break;
        }

    }
    private void generateStuffs() {

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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                !quoteProvider.matches("Storm|Talaikis")
                        ? quoteUrl + generateRandomNumber().toString()
                        : quoteUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseQuote(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "404 Quote not found");
                        Log.e(TAG, error.toString());
                    }
                }
        ) {
            // Optional method to pass Request Headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (quoteProvider.equals("FavQs")) {
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", "Token token=\"" + APP_KEY_QUOTE + "\"");
                }
                return params;
            }
        };

        // Add the request to Volley's Request Queue
        requestQueue.add(jsonObjectRequest);

    }

    /* To Check if we're connected to the Internet */
    // TODO: Get isNetworkAvailable explained
    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* Generates a Random number to be sent as the Quote's ID */
    private Integer generateRandomNumber() {
        int MIN = 1;
        // Yeah! This was the Number of Quotes as on 19/09/17
        // But, some quotes are 404, dunno why
        int MAX = 62024;
        return new Random().nextInt((MAX - MIN) + 1) + MIN;
    }

    /* Allows the user to share currently displayed quote */
    public void shareQuote(Context context, Quote quote) {
        Log.d(TAG, "Creating Share Intent");
        // My intention is to send (throw) a piece of Text (ball)
        Intent quoteIntent = new Intent(Intent.ACTION_SEND);
        // Piece of Text (the Ball)
        String quoteMessage = quote.getQuoteText() + "\n\n-- " + quote.getAuthorText();
        // Specify the Text to be thrown
        quoteIntent.putExtra(Intent.EXTRA_TEXT, quoteMessage);
        // Specify the MIME type of the object to be thrown
        quoteIntent.setType("text/plain");
        // Send an Acknowledgement
        Toast.makeText(context, "Select an App to GibQuote", Toast.LENGTH_SHORT).show();
        // Throw the Ball!
        context.startActivity(Intent.createChooser(quoteIntent, "Share this Quote"));
    }

    private void parseQuote(JSONObject response) {
        Log.i(TAG, response.toString());

        try {
            String quoteText = response.getString(quoteTextVarName);
            String authorText = response.getString(quoteAuthorVarName);
            // Set Obtained details
            quoteArrayList.add(new Quote(quoteText, authorText));
            // Notify the Adapter which in turn notifies the RecyclerView
            // that a new item has been inserted
            quoteAdapter.notifyItemInserted(quoteArrayList.size());

            // Scroll the RecyclerView to given position, smooothly
            quoteRecyclerView.smoothScrollToPosition(quoteAdapter.getItemCount() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showIntroTapTargets(View view) {
        Typeface boldTypeFace = Typeface
                .createFromAsset(getActivity().getAssets(),
                        "fonts/comfortaa/comfortaa_bold.ttf");

        final TapTarget providerTapTarget = TapTarget.forView(view.findViewById(R.id.provider_select),
                "Get started",
                "Select Provider from which you'd fetch quotes")
                .outerCircleColor(R.color.colorPrimary)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(android.R.color.white)
                .titleTextSize(32)
                .titleTextColor(android.R.color.white)
                .descriptionTextSize(20)
                .descriptionTextColor(R.color.colorGray)
                .titleTypeface(boldTypeFace)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(true)
                .transparentTarget(true)
                .targetRadius(110);

        TapTarget gibQuoteTapTarget = TapTarget.forView(view.findViewById(R.id.gib_quote_fab),
                "One last step",
                "Press to fetch a quote!")
                .outerCircleColor(R.color.colorPrimary)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(android.R.color.white)
                .titleTextSize(32)
                .titleTextColor(android.R.color.white)
                .descriptionTextSize(20)
                .descriptionTextColor(R.color.colorGray)
                .titleTypeface(boldTypeFace)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(true)
                .transparentTarget(true)
                .targetRadius(60);

        new TapTargetSequence(getActivity())
                .targets(providerTapTarget, gibQuoteTapTarget)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Log.i(TAG, "Introduction complete, start Gibbing Quotes");
                        Toast.makeText(getContext(), "You're good to go!", Toast.LENGTH_LONG).show();
                        sharedPreferences.edit()
                                .putBoolean("IS_USER_INTRODUCED", true)
                                .apply();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if(lastTarget.equals(providerTapTarget) && targetClicked) {

                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Log.d(TAG, "Eh, what ? Kthxbai");
                    }
                }).start();
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving Instance state");
        outState.putParcelableArrayList("quoteData", quoteArrayList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment onDestroyView called");
    }
}
