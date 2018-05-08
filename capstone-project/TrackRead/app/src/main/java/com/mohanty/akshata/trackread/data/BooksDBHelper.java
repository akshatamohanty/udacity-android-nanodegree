package com.mohanty.akshata.trackread.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Akshata on 21/2/2017.
 */

public class BooksDBHelper extends SQLiteOpenHelper{

    String LOG_TAG = this.getClass().getSimpleName();
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Books.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private String createTable(String tableName){

        String SQLEntry =  "CREATE TABLE " + tableName + " (" +
                BooksContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                BooksContract.BookEntry.COLUMN_NAME_BOOK_ID + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_TITLE + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_DATE_ADDED + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_TOTAL_PAGES + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_CURRENT_PAGE + TEXT_TYPE + COMMA_SEP +
                BooksContract.BookEntry.COLUMN_NAME_NOTES + TEXT_TYPE + " )";

        return SQLEntry;

    }

    private String dropTable(String tableName){
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + tableName;
        return  SQL_DELETE_ENTRIES;
    }

    public BooksDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        // create table to store books
        db.execSQL( createTable(BooksContract.TABLE_NAME_BOOKS) );
        Log.v(LOG_TAG, "Database table created");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(dropTable(BooksContract.TABLE_NAME_BOOKS));

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // required CRUD functions

    // Add a movie
    public long addBook(ContentValues values){

        SQLiteDatabase db = this.getWritableDatabase();

        // Inserting Row
        return db.insert(BooksContract.TABLE_NAME_BOOKS, null, values);
    }

    // Get all books
    public Cursor getAllBooks(){

        // Select All Query
        String selectQuery = "SELECT  * FROM " + BooksContract.TABLE_NAME_BOOKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    // get all books with status true (current)
    private Cursor getBooksByStatusCode(int status){

        // Select All Query
        String selectQuery = "SELECT  * FROM " + BooksContract.TABLE_NAME_BOOKS +
                " WHERE " + BooksContract.BookEntry.COLUMN_NAME_STATUS + " = "
                + "\"" + String.valueOf(status) + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor getArchivedBooks(){
        return getBooksByStatusCode(BooksContract.STATUS_ARCHIVED);
    }

    public Cursor getCurrentBooks(){
        return getBooksByStatusCode(BooksContract.STATUS_CURRENT);
    }

    // Get single movie
    public Cursor getBookDetails(String bookId){

        // Select All Query
        String selectQuery = "SELECT  * FROM " + BooksContract.TABLE_NAME_BOOKS +
                " WHERE " + BooksContract.BookEntry.COLUMN_NAME_BOOK_ID + " = "
                + "\"" + bookId + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public boolean addNote(String bookId, ContentValues cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(BooksContract.TABLE_NAME_BOOKS, cv, BooksContract.BookEntry.COLUMN_NAME_BOOK_ID + "=" + bookId, null) > 0;
    }

    // Deleting a book based on book id
    public long deleteBook(String bookId){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(BooksContract.TABLE_NAME_BOOKS, BooksContract.BookEntry.COLUMN_NAME_BOOK_ID + "=\"" + bookId + "\"", null);
    }

    // delete all books
    public void deleteAllBooks(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ tableName);
        Log.v(LOG_TAG, "All books deleted from " + tableName);
    }

}

