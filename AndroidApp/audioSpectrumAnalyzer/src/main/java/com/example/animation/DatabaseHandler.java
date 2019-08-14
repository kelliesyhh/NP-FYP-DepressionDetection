package com.example.animation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

public class DatabaseHandler extends SQLiteOpenHelper {
    public DatabaseHandler(Context context) {
        super(context, "Database1", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CreateTable = "CREATE TABLE Table2 (xValue INTEGER, yvalue INTEGER)";
        sqLiteDatabase.execSQL(CreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Table2");
        onCreate(sqLiteDatabase);
    }

    public void insertToData(long ValX, int ValY) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("xValue", ValX);
        contentValues.put("yValue", ValY);

        sqLiteDatabase.insert("Table2", null, contentValues);
    }


}