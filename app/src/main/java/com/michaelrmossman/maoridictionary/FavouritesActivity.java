package com.michaelrmossman.maoridictionary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavouritesActivity extends AppCompatActivity {
    private SQLiteDatabase newDB; // Create instance of database
    // Set up an array for Favourites ListView Adapter
    private final ArrayList<String> mResults = new ArrayList<>();
    private ListView mFavouritesList;
    private boolean gotResult;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        mFavouritesList = (ListView) findViewById(R.id.list_fav);
        gotResult = false;
        openAndQueryDatabase();
        displayResults();
    }

    private void openAndQueryDatabase() {
        SharedPreferences mySharedPrefs = getSharedPreferences("Faves", Context.MODE_PRIVATE);
        int arraySize = 0;
        if (mySharedPrefs != null) arraySize = mySharedPrefs.getInt("Faves_size", 0);
        if (arraySize > 0) {
            gotResult = true;
            TextView textView1 = new TextView(this);
            int sp = (int) (getResources().getDimension(R.dimen.list_item_min_height)/getResources().getDisplayMetrics().density);
            textView1.setMinHeight(sp);
            textView1.setGravity(16); // CENTER_VERTICAL
            textView1.setTextSize(14);
            textView1.setPaddingRelative(15, 0, 15, 0); // Start, top, end, bottom
            textView1.setTextColor(Color.BLACK);
            String tv1Text = getString(R.string.rem);
            textView1.setText(tv1Text);
            textView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do nothing
                }
            });
            mFavouritesList.addHeaderView(textView1);
            try {
                Cursor queryCursor;
                final DBHelper dbHelper = new DBHelper(this.getApplicationContext());
                newDB = dbHelper.getWritableDatabase();
                String selectFrom = "SELECT * FROM ";
                String whereLike = " WHERE search = ";
                String orderBy = " ORDER BY search COLLATE NOCASE";
                final String[] ContextNames = getResources().getStringArray(R.array.contexts);
                for (int i = 0; i < arraySize; i++) {
                    String thisTable = mySharedPrefs.getString("Table_" + i, getString(R.string.m2e_table));
                    String thisFave = mySharedPrefs.getString("Favourite_" + i, "");
                    String thisArray[] = thisTable.split("_");
                    String thisColumn = thisArray[0];
                    queryCursor = newDB.rawQuery(selectFrom + thisTable + " WHERE " + thisColumn + " = " + "\"" + thisFave + "\"" + orderBy, null);
                    if (!queryCursor.moveToFirst()) {
                        queryCursor = newDB.rawQuery(selectFrom + thisTable + whereLike + "\"" + thisFave + "\"" + orderBy, null);
                    }
                    if (queryCursor.moveToFirst()) {
                        do {
                            String resultsStr;
                            Integer columnCount = 1;
                            String myBullet = getString(R.string.bullet_point);
                            // From tableName, work out the To and From languages, replacing special chars if necessary
                            String tmpArray[] = thisTable.split("_");
                            String translateFromHeader = tmpArray[0].substring(0, 1).toUpperCase(Locale.ENGLISH)
                                    + tmpArray[0].substring(1, tmpArray[0].length()).replace("a", "ā");
                            String translateToHeader = tmpArray[2].substring(0, 1).toUpperCase(Locale.ENGLISH)
                                    + tmpArray[2].substring(1, tmpArray[2].length()).replace("a", "ā");
                            String originalSearch = queryCursor.getString(queryCursor.getColumnIndex("search"));
                            // Because we're TRYING to cut down on variables, we need to strip special char "ā" & replace it with an "a" if present
                            String translateFrom = queryCursor.getString(queryCursor.getColumnIndex(translateFromHeader.replaceAll("ā", "a").toLowerCase(Locale.ENGLISH)));
                            if (translateFrom.equals("")) translateFrom = originalSearch;
                            resultsStr = translateFromHeader + " word : " + translateFrom + "\n" + translateToHeader + " translation :\n" + myBullet;
                            for (String myContext : ContextNames) {
                                // Because we're TRYING to cut down on variables, we need to strip special char "ā" & replace it with an "a" if present
                                String thisLanguageColumn = translateToHeader.replaceAll("ā", "a").toLowerCase(Locale.ENGLISH) + columnCount.toString();
                                String translateTo = queryCursor.getString(queryCursor.getColumnIndex(thisLanguageColumn));
                                if (translateTo.length() > 0) {
                                    // If this is NOT the first time 'round
                                    if (!myContext.equals("context1")) resultsStr += "\n" + myBullet;
                                    String translateContext = queryCursor.getString(queryCursor.getColumnIndex(myContext));
                                    resultsStr += Contexts.getContext(translateContext, translateTo);
                                    columnCount ++;
                                } else break;
                            } mResults.add(resultsStr); // Start populating ListView
                        } while (queryCursor.moveToNext());
                    } else mResults.add("Sorry, could NOT find your word(s) in the current database!");
                    queryCursor.close(); // Tidy up to preserve data integrity
                }
            } catch (SQLiteException se) {
                Log.e(getClass().getSimpleName(), "Could not open the database!");
            } finally {
                if (newDB != null) newDB.close();
            }
        } else {
            TextView textView2 = new TextView(this);
            int sp = (int) (getResources().getDimension(R.dimen.list_item_min_height)/getResources().getDisplayMetrics().density);
            textView2.setMinHeight(sp);
            textView2.setGravity(16); // CENTER_VERTICAL
            textView2.setTextSize(14);
            textView2.setPaddingRelative(15, 0, 15, 0); // Start, top, end, bottom
            textView2.setTextColor(Color.RED);
            String tv2Text = getString(R.string.nil);
            textView2.setText(tv2Text);
            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do nothing
                }
            });
            mFavouritesList.addHeaderView(textView2);
        }
    }

    private void displayResults() {
        final ArrayAdapter<String> thisAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mResults);
        mFavouritesList.setAdapter(thisAdapter);
        if (gotResult) {
            mFavouritesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    processIt(id);
                    return false;
                }
            });
        }
    }

    public void processIt(long id){
        final Long whichFave = id;
        final int whereFave = whichFave.intValue() + 1; // Starts counting from 1 for some reason
        final String whichText = mFavouritesList.getItemAtPosition(whereFave).toString();
        String[] thisText = whichText.split(" : ", 2); // Limit array to a size of 2
        final String thisWord = thisText[1].substring(0, thisText[1].indexOf("\n"));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Choose an action for word : " + thisWord);
        alertDialogBuilder.setPositiveButton("Remove from Favourites", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                List<String> myFaves = new ArrayList<>();
                List<String> myTables = new ArrayList<>();
                String faveName = (String) mFavouritesList.getItemAtPosition(whereFave);
                String[] thisText = faveName.split(" : ", 2); // Limit array to a size of 2
                String thisWord = thisText[1].substring(0, thisText[1].indexOf("\n")).trim();
                SharedPreferences mSharedPreferences = getSharedPreferences("Faves", Context.MODE_PRIVATE);
                int arraySize;
                String myMsg;
                if (mSharedPreferences != null) {
                    arraySize = mSharedPreferences.getInt("Faves_size", 0);
                    if (arraySize == 1) {
                        mSharedPreferences.edit().clear().apply();
                    } else {
                        SharedPreferences.Editor e = mSharedPreferences.edit();
                        for (int i = 0; i < arraySize; i++) {
                            String addFave = mSharedPreferences.getString("Favourite_" + i, "");
                            String addTable = mSharedPreferences.getString("Table_" + i, "");
                            if (!addFave.equals(thisWord)) {
                                myFaves.add(addFave);
                                myTables.add(addTable);
                            }
                        }
                        for (int i = 0; i < myFaves.size(); i++) {
                            e.remove("Faves_size");
                            e.putInt("Faves_size", myFaves.size());
                            e.remove("Favourite_" + i);
                            e.putString("Favourite_" + i, myFaves.get(i));
                            e.remove("Table_" + i);
                            e.putString("Table_" + i, myTables.get(i));
                            myMsg = thisWord + " " + getString(R.string.removeFav);
                            Toast.makeText(FavouritesActivity.this, myMsg, Toast.LENGTH_SHORT).show();
                        }
                        e.apply();
                    }
                    Intent nIntent = getIntent();
                    finish();
                    startActivity(nIntent);
                } else {
                    Toast.makeText(FavouritesActivity.this, getString(R.string.favError), Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialogBuilder.setNeutralButton("Share this Word", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shareIt(whichText);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // Show alertDialog after building
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button neutralButton = (Button) alertDialog.findViewById(android.R.id.button3);
        Button negativeButton = (Button) alertDialog.findViewById(android.R.id.button2);
        // then get their parent ViewGroup ids
        ViewGroup buttonPanelContainer = (ViewGroup) neutralButton.getParent();
        int neutralButtonIndex = buttonPanelContainer.indexOfChild(neutralButton);
        int negativeButtonIndex = buttonPanelContainer.indexOfChild(negativeButton);
        if (neutralButtonIndex < negativeButtonIndex) {
            // and exchange the buttons indexes in ViewGroup
            buttonPanelContainer.removeView(neutralButton);
            buttonPanelContainer.removeView(negativeButton);
            buttonPanelContainer.addView(negativeButton, neutralButtonIndex);
            buttonPanelContainer.addView(neutralButton, negativeButtonIndex);
        }
    }

    private void shareIt(String whichText) {
        ShareActionProvider provider = new ShareActionProvider(this);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, whichText);
        provider.setShareIntent(intent);
        startActivity(intent);
    }

}