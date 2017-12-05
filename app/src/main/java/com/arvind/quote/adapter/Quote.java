package com.arvind.quote.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Quote implements Parcelable {
    // Class which holds the Data required by RecyclerView's ViewHolder
    // Class' object is given to the ViewHolder
    // which inflates the basic layout
    // and make use of these data in the layout
    // wherever needed

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Quote createFromParcel(Parcel in) {
            return new Quote(in);
        }

        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };
    private final String quoteText;

    private final String authorText;

    private int id;

    public Quote(int id, String quoteText, String authorText) {
        this.id = id;
        this.quoteText = quoteText;
        this.authorText = authorText;
    }

    public Quote(String quoteText, String authorText) {
        this.quoteText = quoteText;
        this.authorText = authorText;
    }

    public Quote(Parcel in) {
        this.quoteText = in.readString();
        this.authorText = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.quoteText);
        parcel.writeString(this.authorText);
    }

    public int getId() {
        return id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthorText() {
        return authorText;
    }

}

