package com.mohanty.akshata.trackread.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Akshata on 21/2/2017.
 */

public class BooksContentProvider extends ContentProvider {
    String LOG_TAG = this.getClass().getSimpleName();

    private BooksDBHelper mDBHelper;

    static final int ARCHIVED_BOOKS = 101;
    static final int CURRENT_BOOKS = 102;

    static final int ALL_BOOKS = 103;
    static final int ALL_BOOKS_ITEM = 1031;

    private static final UriMatcher uriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);
    static{
        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.TABLE_NAME_BOOKS, ARCHIVED_BOOKS);
        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.TABLE_NAME_BOOKS, CURRENT_BOOKS);
        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.TABLE_NAME_BOOKS, ALL_BOOKS);
        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.TABLE_NAME_BOOKS + "/*", ALL_BOOKS_ITEM);
    }

    public boolean onCreate() {
        Log.v(LOG_TAG, "Content Provider created.");
        mDBHelper = new BooksDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {

        Cursor cursor = null;

        String bookId;

        switch (uriMatcher.match(uri)) {

            case ARCHIVED_BOOKS:
                cursor = mDBHelper.getArchivedBooks();
                break;

            case CURRENT_BOOKS:
                cursor = mDBHelper.getCurrentBooks();
                break;

            case ALL_BOOKS:
                cursor = mDBHelper.getAllBooks();
                break;

            case ALL_BOOKS_ITEM:
                bookId = uri.getLastPathSegment();
                cursor = mDBHelper.getBookDetails(bookId);
                break;

            default:
                Log.v("Invalid-URI", uri.toString());

        }

        try{
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.v(LOG_TAG, "Query URI-" + uri.toString());
        }
        catch (NullPointerException e){
            Log.v(LOG_TAG, "Null Pointer Exception at Query");
        }

        return cursor;
    }

    public String getType(Uri uri){
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        long id = mDBHelper.addBook(contentValues);

        if(id <= 0)
            return null;

        try{
            getContext().getContentResolver().notifyChange(uri, null);
        }
        catch (NullPointerException e){
            Log.v(LOG_TAG, "Null Pointer Exception at Insert");
        }

        return uri;
    }

    public int bulkInsert(Uri uri, ContentValues[] values){

        int numInserted = 0;
        String tableName = uri.getLastPathSegment();


        for (ContentValues cv : values) {
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            if (db.insert(tableName, null, cv) <= -1) {
                Log.v(LOG_TAG, "Failed to insert row into " + uri);
            }
            else
                numInserted++;
        }


        getContext().getContentResolver().notifyChange(uri, null);
        Log.v(LOG_TAG, "Notifying uri (bulkInsert) - " + uri.toString());
        return numInserted;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

        String bookId;

        switch (uriMatcher.match(uri)) {

            case ALL_BOOKS_ITEM:
                bookId = uri.getLastPathSegment();
                mDBHelper.deleteBook(bookId);

                try{
                    Log.v(LOG_TAG, "Notifying uri (delete-savedItem) - " + uri.toString());
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                catch (NullPointerException e){
                    Log.v(LOG_TAG, "Null Pointer Exception at Insert");
                }
                break;

            default:
                Log.v("Invalid-URI", uri.toString());

        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        try{
            getContext().getContentResolver().notifyChange(uri, null);
        }
        catch (NullPointerException e){
            Log.v(LOG_TAG, "Null Pointer Exception at Insert");
        }

        return 0;
    }

}
