package com.mohanty.akshata.trackread.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Akshata on 21/2/2017.
 */

public final class BooksContract {

    public static final String CONTENT_AUTHORITY = "com.mohanty.akshata.trackread";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String TABLE_NAME_BOOKS = "books";

    public static final int STATUS_ARCHIVED = 0;
    public static final int STATUS_CURRENT = 1;
    public static final int STATUS_SAVED = 2;

    public BooksContract() {}

    /* Inner classes that defines the table contents */
    public static abstract class BookEntry implements BaseColumns {

        public static final String COLUMN_NAME_BOOK_ID = "movie_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_DATE_ADDED = "date";
        public static final String COLUMN_NAME_TOTAL_PAGES = "total";
        public static final String COLUMN_NAME_CURRENT_PAGE = "current";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_STATUS = "status";

    }

    // Helper functions for Uri building
    public static final Uri buildUriForPopular(){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_BOOKS).build();
    }

    public static final Uri buildUriForBookDetails(String bookId){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_BOOKS).appendPath(bookId).build();
    }

}



