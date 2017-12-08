package com.arvind.quote.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.database.DatabaseHelper;

import java.util.List;

public class FavQuoteAdapter extends RecyclerView.Adapter<FavQuoteAdapter.QuoteViewHolder> {

    private final List<Quote> favQuoteList;
    private Context context = null;

    public FavQuoteAdapter(Context context, List<Quote> quoteDetails) {
        this.context = context;
        this.favQuoteList = quoteDetails;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final Context context = parent.getContext();

        // Initializing Layout Inflater with the Parent
        // Activity under which it'd reside
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // Inflate the Single Quote Layout
        View quoteView = layoutInflater.inflate(R.layout.fav_quote_view, parent, false);

        return new QuoteViewHolder(quoteView);
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder holder, final int position) {

        // get Quote via position of it from favQuoteList
        Quote quote = favQuoteList.get(position);

        holder.quoteTextView.setText(quote.getQuoteText());
        holder.authorTextView.setText(quote.getAuthorText());
    }

    @Override
    public int getItemCount() {
        return favQuoteList.size();
    }

    public class QuoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        final TextView quoteTextView;
        final TextView authorTextView;

        QuoteViewHolder(View itemView) {
            super(itemView);

            quoteTextView = itemView.findViewById(R.id.quote_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);

            itemView.setOnLongClickListener(this);

            // Implement Simple DoubleTap listener
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    new AlertDialog
                            .Builder(context)
                            .setIcon(android.R.drawable.ic_menu_delete)
                            .setTitle("Confirm Deletion of Quote ?")
                            .setMessage("Doing this will remove this quote from Favorites list")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
                                    databaseHelper.removeFavQuote(favQuoteList.get(getAdapterPosition()).getId());
                                    favQuoteList.remove(favQuoteList.get(getAdapterPosition()));
                                    notifyItemRemoved(getAdapterPosition());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .show();

                    return true;
                }

            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    view.performClick();
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

        @Override
        public boolean onLongClick(View v) {
            new MainActivity().shareQuote(context, favQuoteList.get(getAdapterPosition()));
            return true;
        }
    }
}