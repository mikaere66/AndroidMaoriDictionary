package com.michaelrmossman.maoridictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;
    private Spinner mSpinner;
    private RadioGroup mScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        String mDuration = null;
        if (mSharedPreferences != null) {
            mDuration = mSharedPreferences.getString("flipDuration", "250ms");
        }
        mSpinner.setSelection(getIndex(mSpinner, mDuration));
        mScroll = (RadioGroup) findViewById(R.id.show_or_hide_scroll);
        String mShowHide = mSharedPreferences.getString("showScroll", "Show");
        if (mShowHide.equals("Hide")) {
            RadioButton mHide = (RadioButton) findViewById(R.id.hide_scroll);
            mHide.setChecked(true);
        } else {
            RadioButton mShow = (RadioButton) findViewById(R.id.show_scroll);
            mShow.setChecked(true);
        }
    }

    public void settingsOk(View view) {
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.putString("flipDuration", mSpinner.getSelectedItem().toString());
        int selectedId = mScroll.getCheckedRadioButtonId();
        RadioButton mSelected = (RadioButton) mScroll.findViewById(selectedId);
        CharSequence tmpStr = mSelected.getText();
        e.putString("showScroll", tmpStr.toString());
        e.apply();
        finish();
    }

    public void settingsCancel(View view) {
        finish();
    }

    // This little beauty matches the list item to the selected item when loading Spinner
    private int getIndex(Spinner mySpinner, String myString) {
        int index = 0;
        for (int i=0; i < mySpinner.getCount(); i++) {
            if (mySpinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
