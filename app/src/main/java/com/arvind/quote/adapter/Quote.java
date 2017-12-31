package com.arvind.quote.adapter;

public class Quote {
    // Class which holds the Data required by RecyclerView's ViewHolder
    // Class' object is given to the ViewHolder
    // which inflates the basic layout
    // and make use of these data in the layout
    // wherever needed

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

