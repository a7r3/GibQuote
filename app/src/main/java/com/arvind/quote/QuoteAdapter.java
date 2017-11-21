package com.arvind.quote;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final List<QuoteData> quoteList;
    private final Context context;

    public class QuoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        final TextView quoteTextView;
        final TextView authorTextView;
        final TextView tagsTextView;

        QuoteViewHolder(View itemView) {
            super(itemView);

            quoteTextView = itemView.findViewById(R.id.quote_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);
            tagsTextView = itemView.findViewById(R.id.tags_text_view);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            new MainActivity().shareQuote(context, quoteList.get(getAdapterPosition()));
            return true;
        }
    }

    QuoteAdapter(Context context, List<QuoteData> quoteDetails) {
        this.context = context;
        this.quoteList = quoteDetails;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        // Initializing Layout Inflater with the Parent
        // Activity under which it'd reside
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // Inflate the Single Quote Layout
        View quoteView = layoutInflater.inflate(R.layout.quote_view, parent, false);

        // Return a new holder instance
        return new QuoteViewHolder(quoteView);
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder holder, int position) {

        // get QuoteData via position of it from quoteList
        QuoteData quote = quoteList.get(position);

        holder.quoteTextView.setText(quote.getQuoteText());
        holder.authorTextView.setText(quote.getAuthorText());
        holder.tagsTextView.setText(quote.getTagsText());

    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }
}