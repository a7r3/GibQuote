package com.arvind.quote.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.arvind.quote.database.GibDatabaseHelper;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class GibQuoteFragment extends Fragment implements View.OnTouchListener {

    private final String TAG = "GibQuoteFragment";
    // Quote Fetching Stuff
    private ArrayList<Quote> quoteArrayList = new ArrayList<>();
    private QuoteAdapter quoteAdapter;
    private RecyclerView quoteRecyclerView;
    private RequestQueue requestQueue;
    // QuoteProvider Details
    // Default - Offline
    private String quoteProvider = "Offline";
    private String quoteUrl;
    private String quoteTextVarName;
    private String quoteAuthorVarName;
    // SharedPreferences ftw
    private SharedPreferences sharedPreferences;
    // Offline Quote Database Helper
    private GibDatabaseHelper gibDatabaseHelper;
    private Context mContext;
    // Boolean to determine whether the FAB menu is shown
    private boolean isFabMenuShown = false;
    // Animator object, for rotating animation of Main FAB, while revealing the FAB menu
    private ObjectAnimator fabAnimator;
    // The layout in which FAB menu would be displayed
    private RelativeLayout fabRootLayout;
    // The Layouts containing the FAB, with its label
    private LinearLayout changeProviderLayout, clearQuoteLayout;
    // The Menu FAB's smaller FABs
    private FloatingActionButton gibQuoteFab, changeProviderFab, clearQuoteFab;
    // The Labels of Main FAB, and Menu FABs
    private TextView gibQuoteInfo, changeProviderInfo, clearQuoteInfo;
    // The Drawable used for creating an alpha transition of fabRootLayout
    private TransitionDrawable td;
    // AlertDialog which would allow user to change quote provider
    private AlertDialog providerDialog;
    // Maintaining the Index of chosen quote provider
    // Lazy to set an Adapter :P
    private int chosenProviderIndex = 0;
    // The Message to be shown to the user if there are no quotes on the Recycler
    private LinearLayout gibFragDefaultLayout;
    // OnScrollListener for RecyclerView, primarily used for hiding and showing FAB when
    // scrolled
    private RecyclerView.OnScrollListener recyclerOnScrollListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.gib_quote_fragment, container, false);

        MainActivity.setActionBarTitle("GibQuote");

        mContext = getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String quoteJson = sharedPreferences.getString("quoteData", null);
        // RecyclerView Object
        quoteRecyclerView = view.findViewById(R.id.quote_list_view);
        quoteRecyclerView.setTag("RECYCLER");
        // When no quotes are there in the RecyclerView, show this message
        gibFragDefaultLayout = view.findViewById(R.id.gib_quote_fragment_default_layout);
        gibFragDefaultLayout.setTag("GIBDEF");
        // The Dimmed layout which appears when FAB is long pressed
        fabRootLayout = view.findViewById(R.id.fab_root_layout);
        fabRootLayout.setTag("FABROOT");
        fabRootLayout.setOnTouchListener(this);
        // The Animation Drawable
        td = (TransitionDrawable) fabRootLayout.getBackground();

        if (quoteJson != null) {
            Log.d(TAG, "Restoring 'em Quotes");
            quoteArrayList = new Gson().fromJson(quoteJson, new TypeToken<ArrayList<Quote>>() {
            }.getType());
            showViewByTag(quoteRecyclerView);
        } else {
            Log.d(TAG, "New Session. Initializing Quote List");
            quoteArrayList = new ArrayList<>();
            showViewByTag(gibFragDefaultLayout);
        }

        // RequestQueue executes any number of requests, asynchronously
        // More of a Queue Manager for Volley
        requestQueue = Volley.newRequestQueue(mContext);

        // RecyclerView's Adapter - Detects change on DataSet
        quoteAdapter = new QuoteAdapter(mContext, quoteArrayList);

        // Allows Recycler to perform actions on the Layout
        // whenever the particular adapter detects a change
        quoteRecyclerView.setAdapter(quoteAdapter);

        // RecyclerView's Layout Manager
        // Used for viewing RecyclerView's nodes
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);

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
        quoteRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        // FAB to generate quote onClick
        gibQuoteFab = view.findViewById(R.id.gib_quote_fab);
        gibQuoteInfo = view.findViewById(R.id.gib_quote_fab_info);
        gibQuoteInfo.setVisibility(View.GONE);

        final String[] quoteProviders;
        if (isNetworkAvailable()) {
            Log.v(TAG, "Connected to Internet");
            quoteProviders = new String[]{"Offline", "Forismatic", "Talaikis", "Storm"};
        } else {
            Log.v(TAG, "DAM SON :(");
            Snackbar.make(getActivity().findViewById(R.id.frame_layout),
                    "No Internet Connection? We've got some offline quotes!",
                    Snackbar.LENGTH_LONG).show();
            quoteProviders = new String[]{"Offline"};
        }

        // Adding an Listener which ...
        // Hides the FAB when RecyclerView is scrolled down
        // Gets the FAB back when RecyclerView is scrolled up
        recyclerOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                // dy - Instantaneous change in 'y' co-ordinate
                // Origin is at Top-Left corner
                // scrollDown -> Final 'y' co-ordinate is lesser than (or below) Initial 'y' co-ordinate
                //               which results in a positive difference
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

                            // Workaround condition
                            // Scroll through RecyclerView, switch to another Fragment immediately
                            // And the app crashes because FragmentActivity is no more...
                            // Tried removing the recyclerView listener (this), but didn't work
                            if(getActivity() != null)
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
        };

        quoteRecyclerView.addOnScrollListener(recyclerOnScrollListener);

        gibDatabaseHelper = GibDatabaseHelper.getInstance(mContext.getApplicationContext());

        // generateStuffs on receiving a click on FAB
        gibQuoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFabMenuShown)
                    hideFabMenu();
                else {
                    showViewByTag(quoteRecyclerView);
                    if (quoteProvider.equals("Offline")) {
                        quoteArrayList.add(gibDatabaseHelper.getRandomQuote(generateRandomNumber()));
                        quoteAdapter.notifyItemInserted(quoteArrayList.size());
                        quoteRecyclerView.smoothScrollToPosition(quoteAdapter.getItemCount() - 1);
                    } else {
                        generateStuffs();
                    }
                }
            }
        });

        gibQuoteFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isFabMenuShown)
                    hideFabMenu();
                else
                    showFabMenu();
                return true;
            }
        });

        //////////////
        // FAB MENU //
        //////////////

        /////////////////////////
        // CHANGE PROVIDER FAB //
        /////////////////////////

        View.OnClickListener changeProviderListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), MainActivity.themeId);
                builder.setSingleChoiceItems(quoteProviders, chosenProviderIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chosenProviderIndex = i;
                        changeProvider(quoteProviders[chosenProviderIndex]);
                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideFabMenu();
                    }
                });
                builder.setTitle("Change Quote Provider");
                providerDialog = builder.create();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(providerDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                providerDialog.getWindow().setAttributes(lp);
                providerDialog.show();
            }
        };

        changeProviderLayout = view.findViewById(R.id.fabtext_change_provider);
        changeProviderFab = view.findViewById(R.id.fab_change_provider);
        changeProviderFab.setOnClickListener(changeProviderListener);

        changeProviderInfo = view.findViewById(R.id.text_change_provider);
        changeProviderInfo.setOnClickListener(changeProviderListener);

        /////////////////////
        // CLEAR QUOTE FAB //
        /////////////////////

        View.OnClickListener clearQuoteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quoteArrayList.clear();
                quoteAdapter.notifyDataSetChanged();
                quoteRecyclerView.removeAllViews();
                hideFabMenu();
            }
        };

        clearQuoteLayout = view.findViewById(R.id.fabtext_remove_quotes);
        clearQuoteFab = view.findViewById(R.id.fab_remove_quotes);
        clearQuoteFab.setOnClickListener(clearQuoteListener);

        clearQuoteInfo = view.findViewById(R.id.text_remove_quotes);
        clearQuoteInfo.setOnClickListener(clearQuoteListener);

        // Hide the Layouts which hold these FABs and Labels, initially
        changeProviderLayout.setVisibility(View.GONE);
        clearQuoteLayout.setVisibility(View.GONE);
        // Set their Alpha (Transparency) to 0 (Fully Transparent), for the convenience of proper animation
        changeProviderInfo.setAlpha(0);
        clearQuoteInfo.setAlpha(0);

        // If no quotes are present
        if (sharedPreferences.getBoolean("IS_USER_INTRODUCED", false))
            Log.i(TAG, "User knows me already!");
        else {
            Log.i(TAG, "Let's Introduce ourselves");
            showIntroTapTargets(view);
            sharedPreferences.edit().putBoolean("IS_USER_INTRODUCED", true).apply();
        }

        // Animator to animate an Object, with the specified animation property
        fabAnimator = ObjectAnimator.ofFloat(
                gibQuoteFab, // Object to animate
                "rotation", // Animation property - I wanna rotate it
                0f, 360f // Animation's initial and final values
        ).setDuration(500); // Duration of animation

        return view;
    }

    // Convenience method
    private void showViewByTag(View viewToShow) {
        quoteRecyclerView.setVisibility(View.GONE);
        fabRootLayout.setVisibility(View.GONE);
        gibFragDefaultLayout.setVisibility(View.GONE);
        viewToShow.setVisibility(View.VISIBLE);
    }

    private void showFabMenu() {
        td.startTransition(200);
        fabAnimator.start(); // Start the animation, it should complete in 500 seconds
        // Create a new thread, which would change the icon while the animation is taking place
        // In this case, I'm changing the icon in midway of animation :D
        // Delay time is the second argument
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gibQuoteFab.setImageResource(R.drawable.ic_close_black_24dp);
            }
        }, 250);
        // Workaround to Disable (Interactions with) RecyclerView
        // RecyclerView's item checks for this boolean
        // if this is set to false, the Listener code wouldn't be executed
        QuoteAdapter.isClickable = false;
        // Statue! -- Make the RecyclerView non-scrollable
        quoteRecyclerView.setLayoutFrozen(true);
        // Get the FAB Menu up
        fabRootLayout.setVisibility(View.VISIBLE);
        // Animate the FAB labels
        changeProviderInfo.animate().alpha(1).setDuration(200);
        clearQuoteInfo.animate().alpha(1).setDuration(200);
        gibQuoteInfo.animate().alpha(1).setDuration(200);
        // Make the layouts holding these labels, visible
        changeProviderLayout.setVisibility(View.VISIBLE);
        clearQuoteLayout.setVisibility(View.VISIBLE);
        gibQuoteInfo.setVisibility(View.VISIBLE);
        // Translate these layouts to specified positions
        changeProviderLayout.animate().translationYBy(-190);
        clearQuoteLayout.animate().translationYBy(-360);
        // Fabs are shown, make the back
        isFabMenuShown = true;
    }

    // Quite opposite of showFabMenu, except for Animating labels
    // and hiding the RootFab's label after this animation is complete
    private void hideFabMenu() {
        td.reverseTransition(200);

        fabAnimator.reverse();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gibQuoteFab.setImageResource(R.drawable.ic_format_quote_black_24dp);
            }
        }, 250);

        QuoteAdapter.isClickable = true;
        quoteRecyclerView.setLayoutFrozen(false);

        // Animation calls are asynchronous. So hide the Small FAB layouts, ONLY after the
        // animation is complete
        changeProviderInfo.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                changeProviderLayout.setVisibility(View.GONE);
            }
        });
        clearQuoteInfo.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                clearQuoteLayout.setVisibility(View.GONE);
            }
        });
        gibQuoteInfo.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                gibQuoteInfo.setVisibility(View.GONE);
                fabRootLayout.setVisibility(View.GONE);
            }
        });

        if(quoteAdapter.getItemCount() == 0)
            gibFragDefaultLayout.setVisibility(View.VISIBLE);

        changeProviderLayout.animate().translationYBy(190);
        clearQuoteLayout.animate().translationYBy(360);

        isFabMenuShown = false;
    }

    // Method to change JSON parameters based on the quoteProvider selected
    // 'Offline' has no relevance here
    private void changeProvider(String quoteProvider) {
        switch (quoteProvider) {
            case "Forismatic":
            default:
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
        Log.d(TAG, "QuoteProvider: Changed to " + quoteProvider);
    }

    private void generateStuffs() {
        // Creating a new GET JSONObject request
        //
        // Params
        // The Request method - GET
        // The Request URL - a random quote's URL
        // A Listener for performing actions on successfully obtaining a JSON Object
        // A Listener for performing actions if an error occurs while requesting
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                quoteProvider.equals("Forismatic")
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
        );
        // Add the request to Volley's Request Queue
        requestQueue.add(jsonObjectRequest);
    }

    // Generates a Random number to be sent as the Quote's ID
    private Integer generateRandomNumber() {
        int MIN = 0;
        // Yeah! This was the Number of Quotes at FavQs as on 19/09/17
        // But, some quotes are 404
        int MAX = 62024;
        if (quoteProvider.equals("Offline")) {
            MAX = ((int) gibDatabaseHelper.getRowCount());
        }
        return new Random().nextInt((MAX - MIN) + 1) + MIN;
    }

    // Method to parse Quote from JSON Result
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
            // Scroll the RecyclerView to given position, smoothly
            quoteRecyclerView.smoothScrollToPosition(quoteAdapter.getItemCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // To Check if we're connected to the Internet
    private boolean isNetworkAvailable() {
        try {
            NetworkInfo activeNetworkInfo = null;
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) mContext
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null)
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showIntroTapTargets(View view) {
        Typeface boldTypeFace = Typeface
                .createFromAsset(getActivity().getAssets(),
                        "fonts/comfortaa/comfortaa_bold.ttf");

        TapTarget gibQuoteTapTarget = TapTarget.forView(view.findViewById(R.id.gib_quote_fab),
                "Get Started",
                "Single tap to fetch a quote\nLong Press to view Options")
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

        TapTargetView.showFor(getActivity(), gibQuoteTapTarget);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment onDestroyView called");
        // GSON can convert De-serialized Data (Java Objects) to Serialized Data (JSON) and vice-versa
        // https://github.com/google/gson
        if(!quoteArrayList.isEmpty()) {
            String quoteJson = new Gson().toJson(quoteArrayList);
            // Save the serialized JSON String as a Preference
            sharedPreferences.edit()
                    .putString("quoteData", quoteJson)
                    .apply();
        } else
            sharedPreferences.edit().putString("quoteData", null).apply();
        // Hide the 'Change Provider' Dialog
        if(providerDialog != null)
            providerDialog.dismiss();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // If the touch event is on fabRootLayout
        if(view.getTag().equals("FABROOT")) {
            // If the fabMenu is opened  and the gesture performed on fabRootLayout is complete
            if (isFabMenuShown && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Acknowledge the event by hiding the fabMenu
                Log.d(TAG, "Gesture performed in FABROOT is complete");
                Log.d(TAG, "Hiding FAB Menu");
                view.performClick();
                hideFabMenu();
                return false;
            }
        }
        return true;
    }
}
