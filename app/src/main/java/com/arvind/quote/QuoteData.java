package com.arvind.quote;

class QuoteData {
    // Class which holds the Data required by RecyclerView's ViewHolder
    // Class' object is given to the ViewHolder
    // which inflates the basic layout
    // and make use of these data in the layout
    // wherever needed

    private final String quoteText;
    private final String authorText;
    private final String tagsText;

    QuoteData(String quoteText, String authorText, String tagsText) {
        this.quoteText = quoteText;
        this.authorText = authorText;
        this.tagsText = tagsText;
    }

    String getQuoteText() {
        return quoteText;
    }

    String getAuthorText() {
        return authorText;
    }

    String getTagsText() {
        return tagsText;
    }
}

