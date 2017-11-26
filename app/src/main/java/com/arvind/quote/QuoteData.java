package com.arvind.quote;

import android.os.Parcel;
import android.os.Parcelable;

public class QuoteData implements Parcelable {
    // Class which holds the Data required by RecyclerView's ViewHolder
    // Class' object is given to the ViewHolder
    // which inflates the basic layout
    // and make use of these data in the layout
    // wherever needed

    private int id;

    private final String quoteText;

    private final String authorText;

    private final String tagsText;

    public QuoteData(int id, String quoteText, String authorText, String tagsText) {
        this.id = id;
        this.quoteText = quoteText;
        this.authorText = authorText;
        this.tagsText = tagsText;
    }

    public QuoteData(String quoteText, String authorText, String tagsText) {
        this.quoteText = quoteText;
        this.authorText = authorText;
        this.tagsText = tagsText;
    }

    public QuoteData(Parcel inputParcel) {
        quoteText = inputParcel.readString();
        authorText = inputParcel.readString();
        tagsText = inputParcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(quoteText);
        dest.writeString(authorText);
        dest.writeString(tagsText);
    }

    public static final Parcelable.Creator<QuoteData> CREATOR = new Creator<QuoteData>() {
        @Override
        public QuoteData createFromParcel(Parcel source) {
            return new QuoteData(source);
        }

        @Override
        public QuoteData[] newArray(int size) {
            return new QuoteData[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthorText() {
        return authorText;
    }

    public String getTagsText() {
        return tagsText;
    }
}

