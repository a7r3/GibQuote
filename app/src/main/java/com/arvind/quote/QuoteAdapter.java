//package com.arvind.quote;
//
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.widget.TextView;
//
///**
// * Created by arvind on 9/24/17.
// */
//
//
//  public class QuoteAdapter extends {
//
//    private List<Quote> quoteList;
//
//    public class QuoteViewHolder extends RecyclerView.ViewHolder {
//        public TextView quoteText;
//        public TextView quoteAuthor;
//        public TextView quoteTags;
//        public ImageButton shareButton;
//
//        public QuoteViewHolder(View itemView)
//        {
//            super(itemView);
//            quoteText = (TextView) findViewById(R.id.quote_text_view);
//            quoteAuthor = (TextView) findViewById(R.id.author_text_view);
//            quoteTags = (TextView) findViewById(R.id.tags_text_view);
//            shareButton = (ImageButton) findViewById(R.id.share_image_view);
//        }
//    }
//
//    wewe a = new wewe("Some Quote", "Some Author", "Some Tags");
//
//    List<wewe> quoteList1 = new ArrayList<>();
//    quoteList1
//    public QuoteAdapter(List<Quote> quoteList) {
//        this.quoteList = quoteList;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        Quote q = quoteList.get(position);
//        // Ermm, got stuck
//    }
//
//    @Override
//    public int getItemCount() {
//        return quoteList.size();
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Context context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        View quoteView1 = inflater.inflate(R.layout.quote_layout, parent, false);
//        RecyclerView.ViewHolder QuoteViewHolder = new QuoteViewHolder(quoteView1);
//
//        return QuoteViewHolder;
//    }
//}
