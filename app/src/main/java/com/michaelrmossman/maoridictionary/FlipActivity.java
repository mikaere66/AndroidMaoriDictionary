package com.michaelrmossman.maoridictionary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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

public class FlipActivity extends AppCompatActivity implements ListView.OnScrollListener {
    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }
    private final RemoveWindow mRemoveWindow = new RemoveWindow();
    private final Handler mHandler = new Handler();
    private WindowManager mWindowManager;
    private TextView mDialogText;
    private boolean mShowing;
    private boolean mReady;
    private char mPrevLetter = Character.MIN_VALUE;
    private SQLiteDatabase newDB; // Create instance of database
    // Set up first array for Maori ListView Adapter
    private final ArrayList<String> mResults = new ArrayList<>();
    // Set up second array for English ListView Adapter
    private final ArrayList<String> eResults = new ArrayList<>();
    // Set up objects for ListView Flip animation
    private final Interpolator accelerator = new AccelerateInterpolator();
    private final Interpolator decelerator = new DecelerateInterpolator();
    private android.support.v7.app.ActionBar myAB;
    private ListView mEnglishList;
    private ListView mMaoriList;
    private Button flipButton;
    private Button upButton;
    private TextView textView0;
    private Long flipDuration;
    private Display mDisplay;
    private String searchStr;
    private Integer fuzzyLog;
    private Integer randWord;
    private Integer searchType;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip);
        myAB = getSupportActionBar();
        mMaoriList = (ListView) findViewById(R.id.list_ma);
        mMaoriList.setOnScrollListener(this);
        mEnglishList = (ListView) findViewById(R.id.list_en);
        mEnglishList.setOnScrollListener(this);
        textView0 = (TextView) findViewById(R.id.textView0);
        flipButton = (Button) findViewById(R.id.flip_button);
        upButton = (Button) findViewById(R.id.back_to_top);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        if (mDialogText == null) {
            mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        }
        mDialogText.setVisibility(View.INVISIBLE);
        mHandler.post(new Runnable() {
            public void run() { // mReady = true;
                WindowManager windowManager;
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                mDisplay = windowManager.getDefaultDisplay();
                DisplayMetrics metrics = calculateDisplayMetrics();
                int width = metrics.widthPixels / 2;
                Integer initialX = width - 80;
                Integer initialY = 10;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, // initialXY added by MM
                        WindowManager.LayoutParams.WRAP_CONTENT, initialX, initialY,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }
        });
        Bundle b = getIntent().getExtras();
        // Defaults to Maori if NOT passed from MainActivity
        String tableName = b.getString("tableName", getString(R.string.m2e_table));
        searchStr = b.getString("searchStr", ""); // Defaults to empty string
        fuzzyLog = b.getInt("fuzzyLog", 0); // Fuzzy logic defaults to 0 if not passed
        randWord = b.getInt("randWord", 0); // Random word defaults to 0 if not passed
        // Add cumulative value of the variables to easily refer to search type later
        searchType = searchStr.length() + fuzzyLog + randWord;
        // Decide from tableName which list to populate & if to search BOTH languages
        if (tableName.equals(getString(R.string.m2e_table))) {
            searchMaori(); // Default Maori
            if (searchType == 0) {
                searchEnglish(); // Also load English list for flip animation
                switchText("m2e");
            }
        } else { // Otherwise it's English
            mMaoriList.setVisibility(View.GONE);
            mEnglishList.setVisibility(View.VISIBLE);
            searchEnglish(); // Also load Maori list for flip animation
            if (searchType == 0) {
                searchMaori();
                switchText("e2m");
            }
        }
        if (searchType > 0) {
            hideControls();
            if (myAB != null) {
                // From tableName, work out the To and From languages, replacing special chars if necessary
                String tmpArray[] = tableName.split("_");
                String tmpString = tmpArray[0].substring(0,1).toUpperCase(Locale.ENGLISH)
                        + tmpArray[0].substring(1, tmpArray[0].length()).replace("a", "ā")
                        + " to " + tmpArray[2].substring(0,1).toUpperCase(Locale.ENGLISH)
                        + tmpArray[2].substring(1, tmpArray[2].length()).replace("a", "ā") + " Translation";
                myAB.setTitle(tmpString);
            }
        }
    }

    private void hideControls() {
        flipButton.setVisibility(View.GONE);// Hide flip button for normal search & random
        upButton.setVisibility(View.GONE);  // Hide the Back to Top button
        textView0.setVisibility(View.GONE); // Hide blurb about filtering search results
    }

    private void searchMaori() {
        openAndQueryDatabase(getString(R.string.m2e_table), mMaoriList, mResults, searchStr, fuzzyLog, randWord);
        displayResults(mMaoriList, mResults);
    }

    private void searchEnglish() {
        openAndQueryDatabase(getString(R.string.e2m_table), mEnglishList, eResults, searchStr, fuzzyLog, randWord);
        displayResults(mEnglishList, eResults);
    }

    private void openAndQueryDatabase(String whichTable, final ListView whichList, ArrayList<String> whichArray, String searchFor, Integer fuzzyLog, Integer randWord) {
        // Now the real fun starts =)
        try {
            final DBHelper dbHelper = new DBHelper(this.getApplicationContext());
            newDB = dbHelper.getWritableDatabase();
            Integer randCount = 5; // The number of random words to show
            String whereString; // Parts of the query to pass to SQLite
            String selectFrom = "SELECT * FROM ";
            String whereLike = " WHERE search LIKE ";
            String orderBy = " ORDER BY search COLLATE NOCASE";
            whichList.setTextFilterEnabled(false); // Disable text filtering for short lists
            // From tableName, work out the To and From languages, replacing special chars if necessary
            String tmpArray[] = whichTable.split("_");
            String translateFromHeader = tmpArray[0].substring(0, 1).toUpperCase(Locale.ENGLISH)
                    + tmpArray[0].substring(1, tmpArray[0].length()).replace("a", "ā");
            String translateToHeader = tmpArray[2].substring(0, 1).toUpperCase(Locale.ENGLISH)
                    + tmpArray[2].substring(1, tmpArray[2].length()).replace("a", "ā");
            Cursor queryCursor;
            if (randWord == 1) { // Random words
                queryCursor = newDB.rawQuery(selectFrom + whichTable + " ORDER BY RANDOM() LIMIT " + randCount, null);
            } else {    // More specific searches
                if (fuzzyLog == 1) { // Fuzzy Logic
                    whereString = whereLike + "'%" + searchFor + "'";
                    queryCursor = newDB.rawQuery(selectFrom + whichTable + whereString + orderBy, null);
                } else if (fuzzyLog == 2) { // Extra Fuzzy
                    whereString = whereLike + "'%" + searchFor + "%'";
                    queryCursor = newDB.rawQuery(selectFrom + whichTable + whereString + orderBy, null);
                } else if (searchFor.length() == 0) { // Browse All
                    String queryString = "SELECT * FROM " + whichTable + " ORDER BY search COLLATE NOCASE";
                    queryCursor = newDB.rawQuery(queryString, null);
                } else { // Exact Match
                    whereString = whereLike + "'" + searchFor + "'";
                    queryCursor = newDB.rawQuery(selectFrom + whichTable + whereString + orderBy, null);
                }
                if (queryCursor.getCount() > 10) whichList.setTextFilterEnabled(true); // Enable text filtering for real searches
            }
            if (queryCursor.moveToFirst()) {
                if (searchType > 0) { // Dynamic header row with blurb about adding to favourites
                    TextView textView1 = new TextView(this);
                    int sp = (int) (getResources().getDimension(R.dimen.list_item_min_height)/getResources().getDisplayMetrics().density);
                    textView1.setMinHeight(sp);
                    textView1.setGravity(16); // CENTER_VERTICAL
                    textView1.setTextSize(14);
                    textView1.setPaddingRelative(18, 0, 18, 0); // Start, top, end, bottom
                    textView1.setTextColor(Color.BLACK);
                    String tv3Text = getString(R.string.add);
                    textView1.setText(tv3Text);
                    textView1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Do nothing
                        }
                    });
                    whichList.addHeaderView(textView1);
                }
                final String[] ContextNames = getResources().getStringArray(R.array.contexts);
                do {
                    String resultsStr;
                    Integer columnCount = 1;
                    String myBullet = getString(R.string.bullet_point);
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
                    } whichArray.add(resultsStr); // Start populating ListView
                } while (queryCursor.moveToNext());
                if (randWord == 1) { // Dynamic footer row to offer more Random words
                    TextView textView2 = new TextView(this);
                    int sp = (int) (getResources().getDimension(R.dimen.list_item_min_height)/getResources().getDisplayMetrics().density);
                    textView2.setMinHeight(sp);
                    textView2.setGravity(16); // CENTER_VERTICAL
                    textView2.setTextSize(17);
                    textView2.setTextColor(Color.RED);
                    String tv3Text = "\u00A0 Try another " + randCount + " random words";
                    textView2.setText(tv3Text);
                    textView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent rIntent = getIntent();
                            finish();
                            startActivity(rIntent);
                        }
                    });
                    whichList.addFooterView(textView2);
                }
            } else {
                whichArray.add("Sorry, no results!");
            }
            queryCursor.close(); // Tidy up to preserve data integrity
        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not open the database!");
        } finally {
            if (newDB != null) newDB.close();
        }
    }

    private void displayResults(final ListView whichList, ArrayList<String> whichArray) {
        final ArrayAdapter<String> thisAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, whichArray);
        whichList.setAdapter(thisAdapter);
        whichList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String whichText = whichList.getItemAtPosition(position).toString();
                String[] thisText = whichText.split(" : ", 2); // Limit array to a size of 2
                final String thisWord = thisText[1].substring(0, thisText[1].indexOf("\n"));
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FlipActivity.this);
                alertDialogBuilder.setMessage("Choose an action for word : " + thisWord);
                alertDialogBuilder.setPositiveButton("Add to your Favourites", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        addIt(whichText);
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
                        // Do nothing
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
                return false;
            }
        });
    }

    private void addIt(String whichText) {
        List<String> myFaves = new ArrayList<>();
        List<String> myTables = new ArrayList<>();
        SharedPreferences mSharedPreferences = getSharedPreferences("Faves", Context.MODE_PRIVATE);
        int arraySize = 0;
        if (mSharedPreferences != null) {
            arraySize = mSharedPreferences.getInt("Faves_size", 0);
        }
        String[] thisText = whichText.split(" : ", 2); // Limit array to a size of 2
        String thisTable;
        if (thisText[0].substring(0, 5).equals(getString(R.string.maori))) {
            thisTable = getString(R.string.m2e_table);
        } else {
            thisTable = getString(R.string.e2m_table);
        }
        String thisWord = thisText[1].substring(0, thisText[1].indexOf("\n")).trim();
        SharedPreferences.Editor e = null;
        if (mSharedPreferences != null) {
            e = mSharedPreferences.edit();
        }
        for (int i = 0; i < arraySize; i++) {
            String addFave = mSharedPreferences.getString("Favourite_" + i, null);
            myFaves.add(addFave);
            String addTable = mSharedPreferences.getString("Table_" + i, null);
            myTables.add(addTable);
        }
        if (!myFaves.contains(thisWord)) {
            myFaves.add(thisWord);
            myTables.add(thisTable);
            for (int i=0; i < myFaves.size(); i++) {
                if (e != null) {
                    e.remove("Faves_size");
                }
                if (e != null) {
                    e.putInt("Faves_size", myFaves.size());
                }

                if (e != null) {
                    e.remove("Favourite_" + i);
                }
                if (e != null) {
                    e.putString("Favourite_" + i, myFaves.get(i));
                }

                if (e != null) {
                    e.remove("Table_" + i);
                }
                if (e != null) {
                    e.putString("Table_" + i, myTables.get(i));
                }
            }
            if (e != null) {
                e.apply();
            }
        } else {
            Toast.makeText(FlipActivity.this, "This word is ALREADY in your favourites!", Toast.LENGTH_SHORT).show();
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

    private DisplayMetrics calculateDisplayMetrics() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getMetrics(mDisplayMetrics);
        return mDisplayMetrics;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences mSharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        String flipString = null;
        if (mSharedPreferences != null) {
            flipString = mSharedPreferences.getString("flipDuration", "250ms");
        }
        if (flipString != null) {
            flipDuration = Long.parseLong(flipString.replaceAll("ms", ""));
        }
        String mPreference = null;
        if (mSharedPreferences != null) {
            mPreference = mSharedPreferences.getString("showScroll", "Show");
        }
        if (searchType == 0) if (mPreference != null) {
            mReady = !mPreference.equals("Hide");
        }
    }

    public void flipIt(View v) {
        final ListView visibleList;
        final ListView invisibleList;
        if (mEnglishList.getVisibility() == View.GONE) {
            visibleList = mMaoriList;
            invisibleList = mEnglishList;
            switchText("e2m");
        } else {
            invisibleList = mMaoriList;
            visibleList = mEnglishList;
            switchText("m2e");
        }
        ObjectAnimator visibleToInvisible = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f);
        visibleToInvisible.setDuration(flipDuration);
        visibleToInvisible.setInterpolator(accelerator);
        final ObjectAnimator invisibleToVisible = ObjectAnimator.ofFloat(invisibleList, "rotationY", -90f, 0f);
        invisibleToVisible.setDuration(flipDuration);
        invisibleToVisible.setInterpolator(decelerator);
        visibleToInvisible.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                visibleList.setVisibility(View.GONE);
                invisibleToVisible.start();
                invisibleList.setVisibility(View.VISIBLE);
            }
        });
        visibleToInvisible.start();
    }

    private void switchText(String whichWay) {
        String switchTitle;
        String switchText;
        if (whichWay.equals("m2e")) {
            switchTitle = getString(R.string.flip_ttl) + " " + getString(R.string.m2e_name);
            switchText = getString(R.string.flip_to) + " " + getString(R.string.e2m_name) + " " + getString(R.string.flip_tr);
        } else {
            switchTitle = getString(R.string.flip_ttl) + " " + getString(R.string.e2m_name);
            switchText = getString(R.string.flip_to) + " " + getString(R.string.m2e_name) + " " + getString(R.string.flip_tr);
        }
        if (myAB != null) myAB.setTitle(switchTitle);
        flipButton.setText(switchText);
    }

    public void backToTop(View v) {
        if (mEnglishList.getVisibility() == View.GONE) {
            mMaoriList.setSelection(0);
            mMaoriList.setSelectionFromTop(0, 0);
        } else {
            mEnglishList.setSelection(0);
            mEnglishList.setSelectionFromTop(0, 0);
        }
    }

    public void toggleKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInputFromWindow(textView0.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_context : {
                Intent cIntent = new Intent(this, HelpActivity.class);
                Bundle b = new Bundle();
                b.putString("whichList", "context");
                cIntent.putExtras(b);
                startActivity(cIntent);
                return true;
            }
            case R.id.action_help : {
                Intent hIntent = new Intent(this, HelpActivity.class);
                Bundle b = new Bundle();
                b.putString("whichList", "help");
                hIntent.putExtras(b);
                startActivity(hIntent);
                return true;
            }
            case R.id.action_settings : {
                Intent sIntent = new Intent(this, SettingsActivity.class);
                startActivity(sIntent);
                return true;
            }
        } return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mReady) {
            char firstLetter;
            if (mMaoriList.getVisibility() == View.VISIBLE) {
                firstLetter = mResults.get(firstVisibleItem).charAt(13); // "Māori word : "
            } else {
                firstLetter = eResults.get(firstVisibleItem).charAt(15); // "English word : "
            }
            if (!mShowing && firstLetter != mPrevLetter) {
                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
            }
            mDialogText.setText(((Character)firstLetter).toString());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 1500);
            mPrevLetter = firstLetter;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mDialogText);
        mReady = false;
    }

    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }
}