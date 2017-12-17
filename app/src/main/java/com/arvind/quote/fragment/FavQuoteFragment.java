package com.arvind.quote.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.adapter.FavQuoteAdapter;
import com.arvind.quote.adapter.Quote;
import com.arvind.quote.database.FavDatabaseHelper;

import java.util.ArrayList;

public class FavQuoteFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private String TAG = "FavQuoteFragment";
    private Snackbar snackbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fav_quote_fragment, container, false);

        MainActivity.setActionBarTitle("FavQuotes");

        // RecyclerView Object
        RecyclerView quoteRecyclerView = view.findViewById(R.id.fav_quote_recyclerview);

        // RecyclerView's Adapter - Detects change on DataSet
        FavDatabaseHelper dbHalp = FavDatabaseHelper.getInstance(getActivity().getApplicationContext());

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

    @Override
    public void onDestroyView() {
        if (snackbar != null)
            snackbar.dismiss();
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
    }
}
