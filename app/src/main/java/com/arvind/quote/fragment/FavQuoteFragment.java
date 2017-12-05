package com.arvind.quote.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;

public class FavQuoteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fav_quote_fragment, container, false);

        MainActivity.setActionBarTitle("FavQuotes");
//        // RecyclerView Object
//        RecyclerView quoteRecyclerView = view.findViewById(R.id.quote_list_view);
//
//        // RecyclerView's Adapter - Detects change on DataSet
//        ArrayList<Quote> quoteArrayList = new ArrayList<>();
//        QuoteAdapter quoteAdapter = new QuoteAdapter(getContext(), quoteArrayList);
//
//        // Allows Recycler to perform actions on the Layout
//        // whenever the particular adapter detects a change
//        quoteRecyclerView.setAdapter(quoteAdapter);
//
//        // RecyclerView's Layout Manager
//        // Used for viewing RecyclerView's nodes
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//
//        // Reverses the Layout
//        // Newer nodes come at the top
//
//        // Set Layout Manager for RecyclerView
//        // Two Managers available: 1. LinearLayoutManager 2. StaggeredGridLayoutManager
//        quoteRecyclerView.setLayoutManager(linearLayoutManager);
//
        return view;
    }
}
