package com.michaelrmossman.maoridictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

class DBHelper extends SQLiteAssetHelper {

    private static final String DBName = "database.db";
    private static final int version = '1';

    public DBHelper(Context context) {
        super(context, DBName, null, version);
        this.getReadableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}