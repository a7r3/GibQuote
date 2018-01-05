package com.arvind.quote.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.arvind.quote.adapter.Quote;
import com.arvind.quote.database.FavDatabaseHelper;

import static com.arvind.quote.services.CommonUtilService.showToast;

public class CommonUtils {

    private static final String TAG = "CommonUtils";

    /* Allows the user to share currently displayed quote */
    public static void shareQuote(Context context, Quote quote) {
        Log.d(TAG, "shareQuote: Creating Share Intent");
        // My intention is to send (throw) a piece of Text (ball)
        Intent quoteIntent = new Intent(Intent.ACTION_SEND);
        // Piece of Text (the Ball)
        String quoteMessage = quote.getQuoteText() + "\n\n-- " + quote.getAuthorText();
        // Specify the Text to be thrown
        quoteIntent.putExtra(Intent.EXTRA_TEXT, quoteMessage);
        // Specify the MIME type of the object to be thrown
        quoteIntent.setType("text/plain");
        // Send an Acknowledgement
        showToast(context, "Select an App to GibQuote");
        // Throw the Ball!
        context.startActivity(Intent.createChooser(quoteIntent, "Share this Quote"));
    }

    public static int addToFavQuoteList(Context context, Quote quoteData) {
        FavDatabaseHelper favDatabaseHelper = FavDatabaseHelper.getInstance(context);
        int id = (int) favDatabaseHelper.getRowCount();
        Log.d(TAG, "addToFavQuoteList: Inserting FavQuote " + id);
        favDatabaseHelper.addFavQuote(id, quoteData);
        showToast(context, "Added to Favorites");
        return id;
    }

    public static void removeFromFavQuotesList(Context context, int id) {
        FavDatabaseHelper favDatabaseHelper = FavDatabaseHelper.getInstance(context);
        Log.d(TAG, "removeFromFavQuotesList: Removing FavQuote " + id);
        showToast(context, "Removed from Favorites");
        favDatabaseHelper.removeFavQuote(id);
    }

}
