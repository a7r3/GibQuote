package com.arvind.quote.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.arvind.quote.adapter.Quote;
import com.arvind.quote.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CommonUtilService extends IntentService {

    // Intent Keys
    public static final String INTENT_EXEC_KEY = "toExecute";
    public static final String SHARE_QUOTE = "shareQuote";
    public static final String ADD_FAV_QUOTE = "addFavQuote";
    public static final String REMOVE_FAV_QUOTE = "removeFavQuote";
    public static final String QUOTE_KEY = "quote";
    // IMPORTANT
    // This is an Utility Class
    // Unless when it handles an Intent, then it is a Service
    private static final String TAG = "CommonUtilService";
    public static Handler handler = new Handler(Looper.getMainLooper());
    private static boolean isActivityOpen = true;

    public CommonUtilService() {
        super("CommonUtilService");
    }

    public static void showToast(final Context context, final String toastText) {
        if (!isActivityOpen) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Toast posted in new Handler thread");
                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // GSON Ftw!
        Quote quote = new Gson().fromJson(
                intent.getStringExtra(QUOTE_KEY),
                new TypeToken<Quote>() {
                }.getType()
        );
        Log.d(TAG, "Ohai there!");
        // This service runs in Background, set this to false
        isActivityOpen = false;
        switch (intent.getStringExtra(INTENT_EXEC_KEY)) {
            case SHARE_QUOTE:
                CommonUtils.shareQuote(this, quote);
                break;
            case ADD_FAV_QUOTE:
                CommonUtils.addToFavQuoteList(this, quote);
                break;
            case REMOVE_FAV_QUOTE:
                CommonUtils.removeFromFavQuotesList(this, quote.getId());
                break;
            default:
                Log.d(TAG, "Deb has gone buggy");
        }
    }

}
