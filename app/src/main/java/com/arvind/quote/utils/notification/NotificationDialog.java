package com.arvind.quote.utils.notification;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.adapter.Quote;

public class NotificationDialog extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get quoteText, and authorText from the Intent
        final String quoteText = getIntent().getStringExtra("quoteText");
        final String authorText = getIntent().getStringExtra("authorText");

        setTheme(R.style.DialogTheme);

        setContentView(R.layout.activity_notification_dialog);

        TextView quoteTextView = findViewById(R.id.quote_text_view);
        quoteTextView.setText(quoteText);

        TextView authorTextView = findViewById(R.id.author_text_view);
        authorTextView.setText(authorText);

        Button shareButton = findViewById(R.id.notif_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.shareQuote(getApplicationContext(), new Quote(quoteText, authorText));
            }
        });

        Button favButton = findViewById(R.id.notif_fav_button);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.addToFavQuoteList(getApplicationContext(), new Quote(quoteText, authorText));
            }
        });

    }

}
