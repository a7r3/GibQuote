package com.arvind.quote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.arvind.quote.adapter.Quote;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

// SQLiteAssetHelper is an extension of SQLiteOpenHelper with
// the advantage of copying a database file (compressed/uncompressed)
// to Application's Databases and making use of the copy
public class GibDatabaseHelper extends SQLiteAssetHelper {

    private static String TAG = "GibDatabaseHelper";

    // Increment this if you've performed any changes
    // to the database (for eg. Changing column info)
    // Incrementing it would drop older version's DB
    // See onUpgrade() for implementation
    private static int DATABASE_VERSION = 1;

    // Give the DB a good name
    private static String DATABASE_NAME = "gib.db";

    // Static Instance of Database Helper
    private static GibDatabaseHelper gibDatabaseHelperInstance;

    // Table name
    private String QUOTE_TABLE = "quotes";

    // Table columns
    private String ID_KEY = "id";
    private String QUOTE_KEY = "quote";
    private String AUTHOR_KEY = "author";

    private GibDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Make sure that we get a single GibDatabaseHelper Instance throughout
    // the Application (Singleton Instance they said)
    public static synchronized GibDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx

        if (gibDatabaseHelperInstance == null) {
            gibDatabaseHelperInstance = new GibDatabaseHelper(
                    context.getApplicationContext(),
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
        }

        return gibDatabaseHelperInstance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Method to get all Quotes (rows) from the DB
    public Quote getRandomQuote(int id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        String QUERY = "SELECT * FROM " + QUOTE_TABLE + " WHERE id=" + id;
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor quoteDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        // Moves to the first row - returns a boolean if it successfully does this
        quoteDataCursor.moveToFirst();
        Quote randomQuote = new Quote(
                quoteDataCursor.getInt(quoteDataCursor.getColumnIndex(ID_KEY)),
                quoteDataCursor.getString(quoteDataCursor.getColumnIndex(QUOTE_KEY)),
                quoteDataCursor.getString(quoteDataCursor.getColumnIndex(AUTHOR_KEY))
        );

        Log.i(TAG, randomQuote.getId() + " :: "
                + randomQuote.getQuoteText() + " :: "
                + randomQuote.getAuthorText());
        // Prevents any other Query to be executed
        quoteDataCursor.close();
        sqLiteDatabase.endTransaction();

        return randomQuote;
    }

    public long getRowCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), QUOTE_TABLE);
    }

}
