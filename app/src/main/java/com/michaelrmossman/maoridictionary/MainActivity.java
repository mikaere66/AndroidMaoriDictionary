package com.michaelrmossman.maoridictionary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    // String of dummy searches for AutoComplete textview
    private final String[] AutoCmplItems = {"canoe", "waka"};
    // Database table names
    private final String tableNameM2E = "maori_to_english";
    private final String tableNameE2M = "english_to_maori";
    private ArrayAdapter<String> myAutoCmplAdapter;
    private final String PREF_LANG = "Language";
    private final String PREF_TYPE = "FuzzyLog";
    private SharedPreferences mySharedPrefs;
    private AutoCompleteTextView myAutoCmpl;
    private RadioGroup searchRadio;
    private RadioButton searchMaori;
    private ProgressDialog mDialog;
    private List<String> myList;
    private Spinner searchType;
    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDialog = new ProgressDialog(this);
        searchRadio = (RadioGroup) findViewById(R.id.search_for);
        searchMaori = (RadioButton) findViewById(R.id.maori_to_english);
        searchType = (Spinner) findViewById(R.id.search_type);
        // Set up the AutoComplete word list and associated array adapter
        myList = new ArrayList<>();
        Collections.addAll(myList, AutoCmplItems);
        myAutoCmpl = (AutoCompleteTextView) findViewById(R.id.main_edit_text);
        myAutoCmpl.addTextChangedListener(this);
        myAutoCmplAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, myList);
        myAutoCmpl.setAdapter(myAutoCmplAdapter);
        // Listen for Search icon on soft keyboard
        myAutoCmpl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int itemId, KeyEvent keyEvent) {
                if (itemId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });
        View searchButton = findViewById(R.id.main_search_button);
        // Used for shake effect if no text entered into Search field
        searchButton.setOnClickListener(this);
        getSharedPrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mDialog.isShowing()) mDialog.dismiss();
    }

    private void getSharedPrefs() {
        // Get previously saved search prefs & any autocomplete words
        mySharedPrefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        if (mySharedPrefs != null) {
            // Read saved user prefs (defaults to 0 if not found)
            Integer prefLang = mySharedPrefs.getInt(PREF_LANG, 0);
            Integer prefType = mySharedPrefs.getInt(PREF_TYPE, 0);
            // If value for RadioButton is valid, assign Checked
            if (prefLang > 0) searchRadio.check(prefLang);
            else searchMaori.setChecked(true);
            // If value for Spinner is valid, assign Selected
            if (prefType > 0) searchType.setSelection(prefType);
            else searchType.setSelection(0);
            // Read in AutoComplete array & add to history
            int arraySize = mySharedPrefs.getInt("Status_size", 0);
            for (int i = 0; i < arraySize; i++) {
                String addItem = mySharedPrefs.getString("Status_" + i, null);
                if (!myList.contains(addItem)) myList.add(addItem);
            }
        }
    }

    private void saveSharedPrefs() {
        SharedPreferences.Editor e = mySharedPrefs.edit();
        // Save checked RadioButton to SharedPreferences
        Integer prefLang = searchRadio.getCheckedRadioButtonId();
        if (prefLang > 0) e.putInt(PREF_LANG, prefLang);
        // Save selected Spinner to SharedPreferences
        Integer prefType;
        if (searchType.getSelectedItemPosition() > 0)
            prefType = searchType.getSelectedItemPosition();
        else prefType = 0;
        e.putInt(PREF_TYPE, prefType);
        // Save the updated AutoComplete entries for next time
        e.putInt("Status_size", myList.size());
        for (int i=0; i < myList.size(); i++) {
            e.remove("Status_" + i);
            e.putString("Status_" + i, myList.get(i));
        } e.apply();
    }

    @Override
    public void onBackPressed() {
        saveSharedPrefs();
        super.onBackPressed();
    }

    public void browseAll(View x) {
        mDialog.setMessage("Loading from Database ...");
        mDialog.setCancelable(false);
        mDialog.setProgress(0);
        mDialog.show();
        Intent bIntent = new Intent(this, FlipActivity.class);
        Bundle b = new Bundle();
        if (searchMaori.isChecked()) tableName = tableNameM2E;
        else tableName = tableNameE2M;
        b.putString("tableName", tableName);
        bIntent.putExtras(b);
        startActivity(bIntent);
    }

    public void goRandom(View y) {
        Bundle b = new Bundle();
        Intent rIntent = new Intent(this, FlipActivity.class);
        if (searchMaori.isChecked()) tableName = tableNameM2E;
        else tableName = tableNameE2M;
        b.putString("tableName", tableName);
        b.putInt("randWord", 1);
        rIntent.putExtras(b);
        startActivity(rIntent);
    }

    public void getColours(View view) {
        Bundle b = new Bundle();
        Intent cIntent = new Intent(this, FlipActivity.class);
        if (searchMaori.isChecked()) tableName = tableNameM2E;
        else tableName = tableNameE2M;
        b.putString("tableName", tableName);
        b.putInt("getColours", 1);
        cIntent.putExtras(b);
        startActivity(cIntent);
    }

    public void getNumbers(View view) {
        Bundle b = new Bundle();
        Intent nIntent = new Intent(this, FlipActivity.class);
        if (searchMaori.isChecked()) tableName = tableNameM2E;
        else tableName = tableNameE2M;
        b.putString("tableName", tableName);
        b.putInt("getNumbers", 1);
        nIntent.putExtras(b);
        startActivity(nIntent);
    }

    public void goFaves(View view) {
       Intent fIntent = new Intent(this, FavouritesActivity.class);
        startActivity(fIntent);
    }

    public void onClick(View z) {
        // Needed because doSearch is called from TWO places, button click & keyboard icon
        doSearch();
    }

    private void doSearch() {
        Intent sIntent = new Intent(this, FlipActivity.class);
        // Get the Searchword entered and trim any unnecessary spaces
        String searchFor = myAutoCmpl.getText().toString().trim();
        if (searchFor.length() > 0) {
            String tableName;
            Bundle b = new Bundle();
            if (!myList.contains(searchFor)) {
                myList.add(searchFor);
                // Update the autocomplete word list with new Searchword
                myAutoCmplAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, myList);
                myAutoCmpl.setAdapter(myAutoCmplAdapter);
                myAutoCmplAdapter.notifyDataSetChanged();
            }
            if (searchMaori.isChecked()) tableName = tableNameM2E;
            else tableName = tableNameE2M;
            b.putString("tableName", tableName);
            b.putString("searchStr", searchFor);
            b.putInt("fuzzyLog", searchType.getSelectedItemPosition());
            sIntent.putExtras(b);
            startActivity(sIntent);
        } else if (searchType.getSelectedItemId() < 0) {
            Toast.makeText(this, "Please choose a Search type first!",
                    Toast.LENGTH_SHORT).show();
        } else {
            shakeIt();
            Toast.makeText(this, "Please enter a Search word or phrase!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void shakeIt() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        findViewById(R.id.main_edit_text).startAnimation(shake);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}