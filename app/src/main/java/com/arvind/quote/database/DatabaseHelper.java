package com.arvind.quote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.arvind.quote.adapter.Quote;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Increment this if you've performed any changes
    // to the database (for eg. Changing column info)
    // Incrementing it would drop older version's DB
    // See onUpgrade() for implementation
    private static int DATABASE_VERSION = 2;
    // Give the DB a good name
    private static String DATABASE_NAME = "QuoteDB";
    // Static Instance of Database Helper
    private static DatabaseHelper databaseHelperInstance;
    private String TAG = "DatabaseHelper";
    // Table name
    private String QUOTE_TABLE = "quotedata";
    // Table columns
    // Primary Key (ID) (Row-Identifier)
    private String ID_KEY = "id";
    // Rest of the columns
    private String QUOTE_KEY = "quote";
    private String AUTHOR_KEY = "author";

    private DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Make sure that we get a single DatabaseHelper Instance throughout
    // the Application (Singleton Instance they said)
    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (databaseHelperInstance == null) {
            databaseHelperInstance = new DatabaseHelper(
                    context.getApplicationContext(),
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
        }
        return databaseHelperInstance;
    }

    // Creates a DB only if it does not exist in the Storage
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_DB = "CREATE TABLE " + QUOTE_TABLE +
                "("
                + ID_KEY + " INTEGER PRIMARY KEY, "
                + QUOTE_KEY + " TEXT, "
                + AUTHOR_KEY + " TEXT" +
                ")";

        // Execute above SQL Query
        sqLiteDatabase.execSQL(CREATE_DB);
        Log.i(TAG, "Database " + QUOTE_TABLE + "created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // i -> oldVersion
        // i1 -> newVersion
        if (i != i1) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QUOTE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    // Method to add a quote (row) into the DB
    public void addFavQuote(int id, Quote quote) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues quoteContentValues = new ContentValues();
        // Inserting values into respective fields
        quoteContentValues.put(ID_KEY, id);
        quoteContentValues.put(QUOTE_KEY, quote.getQuoteText());
        quoteContentValues.put(AUTHOR_KEY, quote.getAuthorText());

        sqLiteDatabase.beginTransaction();
        try {
            // Perform Insert Operation with the above Values
            // Throws an exception if it occurs
            sqLiteDatabase.insertOrThrow(QUOTE_TABLE, null, quoteContentValues);
            Log.i(TAG, "Added Quote " + id + " successfully");
            // Set current transaction as successful
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    // Method to remove a quote (row) by its ID
    // Required arg -> ID of the quote
    public void removeFavQuote(int id) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            // Delete a row by its ID (Primary Key)
            // args -> TableName, Clause (condition), Arguments (I've put nothing)
            sqLiteDatabase.delete(QUOTE_TABLE, ID_KEY + "=" + id, null);
            Log.d(TAG, "Removed Row " + id + " successfully");
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    // Method to get all Quotes (rows) from the DB
    public ArrayList<Quote> getFavQuotes() {

        // ArrayList to hold the results
        ArrayList<Quote> favQuoteArrayList = new ArrayList<>();

        String QUERY = "SELECT * FROM " + QUOTE_TABLE;

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor quoteDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        // Returns true if cursor is moved to the first row successfully
        if (quoteDataCursor.moveToFirst()) {
            do {
                // Extract id, quote, and author
                // And add a new Quote Object to ArrayList
                favQuoteArrayList.add(
                        new Quote(
                                quoteDataCursor.getInt(quoteDataCursor.getColumnIndex(ID_KEY)),
                                quoteDataCursor.getString(quoteDataCursor.getColumnIndex(QUOTE_KEY)),
                                quoteDataCursor.getString(quoteDataCursor.getColumnIndex(AUTHOR_KEY))
                        )
                );
            } while (quoteDataCursor.moveToNext());
        }

        // Prevents any other Query to be executed after this
        quoteDataCursor.close();
        sqLiteDatabase.endTransaction();

        return favQuoteArrayList;
    }

    public long getRowCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), QUOTE_TABLE);
    }
}
