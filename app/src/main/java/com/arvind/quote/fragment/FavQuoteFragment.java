package com.arvind.quote.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.adapter.FavQuoteAdapter;
import com.arvind.quote.adapter.Quote;
import com.arvind.quote.database.FavDatabaseHelper;

import java.util.ArrayList;

public class FavQuoteFragment extends Fragment {

    private final String TAG = "FavQuoteFragment";
    private SharedPreferences sharedPreferences;
    private Snackbar snackbar;
    private static LinearLayout favQuoteDefaultLayout;
    private static RecyclerView quoteRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // RecyclerView's Adapter - Detects change on DataSet
        FavDatabaseHelper dbHalp = FavDatabaseHelper.getInstance(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fav_quote_fragment, container, false);

        MainActivity.setActionBarTitle("FavQuotes");

        // RecyclerView Object
        quoteRecyclerView = view.findViewById(R.id.fav_quote_recyclerview);

        favQuoteDefaultLayout = view.findViewById(R.id.fav_quote_fragment_default_layout);

        if(dbHalp.getRowCount() == 0)
            showDefaultFragLayout();
        else
            showFavRecycler();

        // List of Fav Quotes, get it from Database
        ArrayList<Quote> favQuoteArrayList = dbHalp.getFavQuotes();
        FavQuoteAdapter favQuoteAdapter = new FavQuoteAdapter(getContext(), favQuoteArrayList);
        // Allows Recycler to perform actions on the Layout
        // whenever the particular adapter detects a change
        quoteRecyclerView.setAdapter(favQuoteAdapter);
        // RecyclerView's Layout Manager
        // Used for viewing RecyclerView's nodes
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // Set Layout Manager for RecyclerView
        // Two Managers available: 1. LinearLayoutManager 2. StaggeredGridLayoutManager
        quoteRecyclerView.setLayoutManager(linearLayoutManager);
        // Add Default ItemDecoration
        // Used to Decorate every RecyclerView Item
        // Any interaction with the Item won't affect the decoration
        quoteRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!sharedPreferences.getBoolean("IS_DELETE_QUOTE_SHOWN", false)) {
            // Instruct the user
            snackbar = Snackbar.make(
                    getActivity().findViewById(R.id.frame_layout),
                    "Double tap on a quote to delete it",
                    Snackbar.LENGTH_INDEFINITE
            );

            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                    sharedPreferences.edit()
                            .putBoolean("IS_DELETE_QUOTE_SHOWN", true)
                            .apply();
                }
            });
            snackbar.show();
        }

        return view;
    }

    public static void showDefaultFragLayout() {
        favQuoteDefaultLayout.setVisibility(View.VISIBLE);
        quoteRecyclerView.setVisibility(View.GONE);
    }

    public static void showFavRecycler() {
        favQuoteDefaultLayout.setVisibility(View.GONE);
        quoteRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        if (snackbar != null)
            snackbar.dismiss();
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
    }
}
