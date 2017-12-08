package com.arvind.quote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.fragment.GibQuoteFragment;

import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final List<Quote> quoteList;
    private Context context = null;

    public QuoteAdapter(Context context, List<Quote> quoteDetails) {
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

        // get Quote via position of it from quoteList
        Quote quote = quoteList.get(position);

        holder.quoteTextView.setText(quote.getQuoteText());
        holder.authorTextView.setText(quote.getAuthorText());

    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }

    public class QuoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        final TextView quoteTextView;
        final TextView authorTextView;
        final ImageView starQuoteView;

        QuoteViewHolder(View itemView) {
            super(itemView);

            quoteTextView = itemView.findViewById(R.id.quote_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);

            starQuoteView = itemView.findViewById(R.id.star_quote_button);
            starQuoteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new GibQuoteFragment().addToFavQuoteList(context, quoteList.get(getAdapterPosition()));
                    starQuoteView.setImageResource(R.drawable.star_on);
                }
            });

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            new MainActivity().shareQuote(context, quoteList.get(getAdapterPosition()));
            return true;
        }
    }
}