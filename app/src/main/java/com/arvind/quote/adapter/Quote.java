package com.arvind.quote.adapter;

///////////////////////////////
// RecyclerView's Data Model //
///////////////////////////////

public class Quote {

    private final String quoteText;
    private final String authorText;
    // Position of the Quote in FavQuotes Database
    private int id;
    // Is the Quote starred ?
    // Helps the RecyclerView to glow the stars on starred Quotes
    private boolean isStarred;

    public Quote(int id, String quoteText, String authorText) {
        this.id = id;
        this.quoteText = quoteText;
        this.authorText = authorText;
        this.isStarred = false;
    }

    public Quote(String quoteText, String authorText) {
        this.quoteText = quoteText;
        this.authorText = authorText;
        this.isStarred = false;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthorText() {
        return authorText;
    }

}

