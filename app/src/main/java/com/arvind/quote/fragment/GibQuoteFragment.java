package com.arvind.quote.fragment;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.arvind.quote.utils.UpdaterUtils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class GibQuoteFragment extends Fragment {

    private String TAG = "GibQuoteFragment";

    private ArrayList<Quote> quoteArrayList = new ArrayList<>();
    private QuoteAdapter quoteAdapter;
    private RecyclerView quoteRecyclerView;

    private RequestQueue requestQueue;
    private TextView updateMessage;
    private UpdaterUtils updaterUtils;

    private AlertDialog.Builder updateAlertDialog;

    // QuoteProvider Details
    private String quoteProvider;
    private String quoteUrl;
    private String quoteTextVarName;
    private String quoteAuthorVarName;

    private View updateMessageLayout;

    private SharedPreferences sharedPreferences;

    // Offline Quote Database Helper
    private GibDatabaseHelper gibDatabaseHelper;

    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring Instance state");
            quoteArrayList = savedInstanceState.getParcelableArrayList("quoteData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.gib_quote_fragment, container, false);

        MainActivity.setActionBarTitle("GibQuote");

        // RequestQueue executes any number of requests, asynchronously
        // More of a Queue Manager for Volley
        requestQueue = Volley.newRequestQueue(mContext);

        // RecyclerView Object
        quoteRecyclerView = view.findViewById(R.id.quote_list_view);

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
        final FloatingActionButton gibQuoteFab = view.findViewById(R.id.gib_quote_fab);

        String[] quoteProviders;
        if (isNetworkAvailable()) {
            Log.v(TAG, "Connected to Internet");
            quoteProviders = new String[]{"Offline", "Forismatic", "Talaikis", "Storm"};
        } else {
            Log.v(TAG, "DAM SON :(");
            Snackbar.make(
                    getActivity().findViewById(R.id.frame_layout),
                    "No Internet Connection? We've got you covered",
                    Snackbar.LENGTH_LONG).show();
            quoteProviders = new String[]{"Offline"};
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

        gibDatabaseHelper = GibDatabaseHelper.getInstance(mContext.getApplicationContext());

        // generateStuffs on receiving a click on FAB
        gibQuoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quoteProvider.equals("Offline")) {
                    quoteArrayList.add(gibDatabaseHelper.getRandomQuote(generateRandomNumber()));
                    quoteAdapter.notifyItemInserted(quoteArrayList.size());
                    quoteRecyclerView.smoothScrollToPosition(quoteAdapter.getItemCount() - 1);
                } else {
                    generateStuffs();
                }
            }
        });

        // If no quotes are present
        if (sharedPreferences.getBoolean("IS_USER_INTRODUCED", false))
            Log.i(TAG, "User knows me already!");
        else {
            Log.i(TAG, "Let's Introduce ourselves");
            showIntroTapTargets(view);
        }

        // Provides user to select an entry from a list of dropdown entries
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

        // Adapter which would provide the Dropdown elements
        // And listen for changes in respective elements (select events)
        ArrayAdapter<String> providerArrayAdapter = new ArrayAdapter<>(
                mContext,
                R.layout.support_simple_spinner_dropdown_item,
                quoteProviders
        );

        providerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        providerSpinner.setAdapter(providerArrayAdapter);

        updaterUtils = new UpdaterUtils(getContext());

        updateMessageLayout = getLayoutInflater().inflate(R.layout.update_message_view, container, false);

        updateMessage = updateMessageLayout.findViewById(R.id.update_message);

        updaterUtils.setChangeLogMessageListener(new UpdaterUtils.ChangeLog() {
            @Override
            public void onChange(String newChangeLog) {
                Log.d(TAG, "Update Message Changed");
                updateMessage.setText(newChangeLog);
                Log.d(TAG, "New: " + newChangeLog);
            }
        });

        // Set a listener for the boolean 'isUpdateAvailable'
        // It is set to true when an update is available
        // On changingm the code under onChange is executed
        updaterUtils.setUpdateAvailableListener(new UpdaterUtils.UpdateAvailable() {
            @Override
            public void onChange(boolean isUpdateAvailable) {
                if (isUpdateAvailable) {
                    // Construkt the AlertDialog
                    updateAlertDialog = new AlertDialog.Builder(getContext());
                    updateAlertDialog.setTitle("Update");
                    updateAlertDialog.setIcon(R.drawable.ic_fiber_new_black_24dp);
                    // Set the TextView as AlertDialog's view
                    // this view would contain the update message
                    updateAlertDialog.setView(updateMessageLayout);
                    updateAlertDialog.setCancelable(false);
                    updateAlertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            updateApplication(updaterUtils.getUpdatedVersion());
                        }
                    });
                    updateAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getContext(), "Bugs Bro Bugs", Toast.LENGTH_LONG).show();
                        }
                    });
                    updateAlertDialog.create().show();
                }
            }
        });

        return view;
    }

    public void updateApplication(String updatedVersion) {
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
        // Show a notification that the download is complete, on completion
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // Download the APK only when on Wi-Fi or Mobile Data (without Metered Limits)
        downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // Directory where file would be saved
        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GibQuote.apk");
        // Get DownloadManager here
        final DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        // Give this request to it
        downloadManager.enqueue(downloadRequest);
        // Tell the user that the file is being downloaded
        // Yet to study BroadcastReceivers :P
        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.drawer_layout),
                "Update is being downloaded, tap on the downloaded file to install it",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
        // TODO: BroadcastReceiver && Invoke PackageInstaler after file is downloaded
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
            // Experimental! Do not uncomment xD
            // NotificationUtils.getInstance(getContext()).issueNotification(new Quote(quoteText, authorText));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* To Check if we're connected to the Internet */
    // TODO: Get isNetworkAvailable explained
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
            return false;
        }
    }

    public void showIntroTapTargets(View view) {
        Typeface boldTypeFace = Typeface
                .createFromAsset(getActivity().getAssets(),
                        "fonts/comfortaa/comfortaa_bold.ttf");

        TapTarget providerTapTarget = TapTarget.forView(view.findViewById(R.id.provider_select),
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
                        Toast.makeText(mContext, "You're good to go!", Toast.LENGTH_LONG).show();
                        sharedPreferences.edit()
                                .putBoolean("IS_USER_INTRODUCED", true)
                                .apply();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if (!targetClicked) {
                            // Seems the user knows everything
                            sharedPreferences.edit()
                                    .putBoolean("IS_USER_INTRODUCED", true)
                                    .apply();
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Log.d(TAG, "Intro Sequence Cancelled");
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
