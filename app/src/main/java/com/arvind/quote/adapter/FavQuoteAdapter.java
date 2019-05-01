package com.arvind.quote.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arvind.quote.R;
import com.arvind.quote.database.FavDatabaseHelper;
import com.arvind.quote.fragment.FavQuoteFragment;
import com.arvind.quote.utils.CommonUtils;

import java.util.List;

public class FavQuoteAdapter extends RecyclerView.Adapter<FavQuoteAdapter.QuoteViewHolder> {

    private final List<Quote> favQuoteList;
    private Context context;

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

    public class QuoteViewHolder extends RecyclerView.ViewHolder {

        final TextView quoteTextView;
        final TextView authorTextView;

        QuoteViewHolder(View itemView) {
            super(itemView);

            quoteTextView = itemView.findViewById(R.id.quote_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);

            itemView.setOnLongClickListener(view -> {
                CommonUtils.shareQuote(context, favQuoteList.get(getAdapterPosition()));
                return true;
            });


            // Implement Simple DoubleTap listener
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    final Quote quote = favQuoteList.get(getAdapterPosition());
                    new AlertDialog
                            .Builder(context)
                            .setIcon(android.R.drawable.ic_menu_delete)
                            .setTitle("Confirm Deletion of Quote")
                            .setMessage("Doing this will remove this quote from Favorites list")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                FavDatabaseHelper favDatabaseHelper = FavDatabaseHelper.getInstance(context);
                                favDatabaseHelper.removeFavQuote(quote.getId());
                                quote.setStarred(false);
                                favQuoteList.remove(quote);
                                notifyItemRemoved(getAdapterPosition());
                                if(getItemCount() == 0)
                                    FavQuoteFragment.showDefaultFragLayout();
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setCancelable(true)
                            .show();

                    return true;
                }

            });

            itemView.setOnTouchListener((view, motionEvent) -> {
                view.performClick();
                return gestureDetector.onTouchEvent(motionEvent);
            });
        }
    }
}