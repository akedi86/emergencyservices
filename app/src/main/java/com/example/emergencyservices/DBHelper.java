package com.example.emergencyservices;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "poppy.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE profile (email VARCHAR PRIMARY KEY NOT NULL, firstname VARCHAR NOT NULL, lastname VARCHAR NOT NULL, mobile VARCHAR NOT NULL, bloodtype VARCHAR NOT NULL)");
        db.execSQL("CREATE TABLE allergies (id SERIAL PRIMARY KEY, name VARCHAR NOT NULL)");
        db.execSQL("CREATE TABLE conditions (id SERIAL PRIMARY KEY, name VARCHAR NOT NULL)");
        db.execSQL("CREATE TABLE contacts (id SERIAL PRIMARY KEY, fullname VARCHAR NOT NULL, mobile VARCHAR NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS profile");
        db.execSQL("DROP TABLE IF EXISTS allergies");
        db.execSQL("DROP TABLE IF EXISTS conditions");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }
}