package com.mohanty.akshata.trackread.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;

/**
 * Created by Akshata on 21/2/2017.
 */

public class BooksDBHelper extends SQLiteOpenHelper{

    String LOG_TAG = this.getClass().getSimpleName();
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Movies.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private String createTable(String tableName){

        String SQLEntry =  "CREATE TABLE " + tableName + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_NAME_MOVIE_ID + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_TITLE + TEXT_TYPE + " UNIQUE " + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_SYNOPSIS + TEXT_TYPE + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_POSTER + TEXT_TYPE + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_RATING + TEXT_TYPE + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_RELEASE + TEXT_TYPE + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_VIDEOS + TEXT_TYPE + COMMA_SEP +
                MovieContract.MovieEntry.COLUMN_NAME_REVIEWS + TEXT_TYPE + " )";

        return SQLEntry;

    }

    private String dropTable(String tableName){
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + tableName;
        return  SQL_DELETE_ENTRIES;
    }



    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        // create three tables to store three kinds of movies
        db.execSQL( createTable(MovieContract.TABLE_NAME_POPULAR) );
        db.execSQL( createTable(MovieContract.TABLE_NAME_TOP_RATED) );
        db.execSQL( createTable(MovieContract.TABLE_NAME_SAVED) );

        Log.v(LOG_TAG, "Database tables created");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(dropTable(MovieContract.TABLE_NAME_TOP_RATED));
        db.execSQL(dropTable(MovieContract.TABLE_NAME_POPULAR));
        db.execSQL(dropTable(MovieContract.TABLE_NAME_SAVED));

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // required CRUD functions

    // Add a movie
    public long addMovie(String tableName, ContentValues values){

        SQLiteDatabase db = this.getWritableDatabase();

        //Log.v(LOG_TAG, "Movie Added");

        // Inserting Row
        return db.insert(tableName, null, values);
    }

    // Get all movies
    public Cursor getAllMovies(String tableName){

        // Select All Query
        String selectQuery = "SELECT  * FROM " + tableName;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    // Get single movie
    public Cursor getMovieDetails(String tableName, String movieTitle){

        // Select All Query
        String selectQuery = "SELECT  * FROM " + tableName +
                " WHERE " + MovieContract.MovieEntry.COLUMN_NAME_TITLE + " = "
                + "\"" + movieTitle + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Log.v(LOG_TAG, "detail" + cursor.getString(2) );
        return cursor;
    }

    // Deleting a movie based on movie title
    public long deleteMovie(String tableName, String movieTitle){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, MovieContract.MovieEntry.COLUMN_NAME_TITLE + "=\"" + movieTitle + "\"", null);
    }

    public void deleteAllMovies(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ tableName);
        Log.v(LOG_TAG, "All movies deleted from " + tableName);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
}

