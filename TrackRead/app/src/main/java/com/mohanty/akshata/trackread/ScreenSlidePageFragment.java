package com.mohanty.akshata.trackread;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mohanty.akshata.trackread.data.BooksContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akshata on 24/2/2017.
 */

public class ScreenSlidePageFragment extends Fragment {

        private static String LOG_TAG = ScreenSlidePageFragment.class.getSimpleName();
        private int page;
        private String bookId, title, author, image, date, notes;

        // newInstance constructor for creating fragment with arguments
        public static ScreenSlidePageFragment newInstance(int page, String bookId, String  title, String  author, String image, String date, String notes) {
            ScreenSlidePageFragment fragmentFirst = new ScreenSlidePageFragment();
            Bundle args = new Bundle();
            args.putInt("someInt", page);
            args.putString("bookId", bookId);
            args.putString("someTitle", title);
            args.putString("someAuthor", author);
            args.putString("someImage", image);
            args.putString("someDate", date);
            args.putString("someNotes", notes);
            fragmentFirst.setArguments(args);
            return fragmentFirst;
        }

        // Store instance variables based on arguments passed
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            page = getArguments().getInt("someInt", 0);
            bookId = getArguments().getString("bookId");
            title = getArguments().getString("someTitle");
            author = getArguments().getString("someAuthor");
            image = getArguments().getString("someImage");
            date = getArguments().getString("someDate");
            notes = getArguments().getString("someNotes");

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_page, container, false);

            TextView bookLabel = (TextView) rootView.findViewById(R.id.bookTitle);
            TextView authorLabel = (TextView) rootView.findViewById(R.id.bookAuthor);
            //TextView dateLabel = (TextView) rootView.findViewById(R.id.date);

            ImageView imageLabel = (ImageView) rootView.findViewById(R.id.photo);

            //Log.v(LOG_TAG, author + "-" + title + "-" + image);

            String[] pictures = getResources().getStringArray(R.array.book_backgrounds);
            int picture_num = (page % 5);

            bookLabel.setText(title);
            bookLabel.setContentDescription(title);

            authorLabel.setText(author);
            authorLabel.setText(author);
            //dateLabel.setText(date);

            Picasso.with(container.getContext())
                    .load(pictures[picture_num])
                    .into(imageLabel);

            // notes
            final ListView listview = (ListView) rootView.findViewById(R.id.notes_container);
            listview.setItemsCanFocus(true);

            try {
                JSONArray values = new JSONArray(notes);
                List<String> list = new ArrayList<String>();
                for(int i = 0; i < values.length(); i++){
                    Log.v(LOG_TAG, (String) values.get(i));
                    list.add((String) values.get(i));
                }
                ArrayAdapter notesAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, list);
                listview.setAdapter(notesAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            FloatingActionButton fab1 = (FloatingActionButton) rootView.findViewById(R.id.add_note);

            fab1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent notesIntent = new Intent(getContext(), StatisticsActivity.class);
                    //startActivity(notesIntent);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("New Note");

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String new_note = input.getText().toString();

                            // check if new_note is blank and return if true
                            if(new_note == null && new_note.isEmpty()){
                                Toast.makeText(getContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {

                                //Log.v(LOG_TAG, "initial notes:" + notes);
                                JSONArray notesArr;
                                if(notes.length() > 0)
                                    notesArr = new JSONArray(notes);
                                else
                                    notesArr = new JSONArray();

                                notesArr.put(new_note);

                                //Log.v(LOG_TAG, notesArr.toString());
                                ContentValues args = new ContentValues();
                                args.put(BooksContract.BookEntry.COLUMN_NAME_NOTES, notesArr.toString());
                                if(getContext().getContentResolver().update(
                                        BooksContract.buildUriForBookDetails(bookId),
                                        args,null, null) == 1){
                                    Toast.makeText(getContext(), R.string.note_added, Toast.LENGTH_SHORT).show();

                                }
                                else
                                    Toast.makeText(getContext(), R.string.error_message, Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                Toast.makeText(getContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), R.string.note_cancel_message, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });

            return rootView;
        }

}
