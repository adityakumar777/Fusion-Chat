package com.fusion.fusionchat.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {



    private Context context;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Users.db";
    private static final String TABLE_NAME = "Users"
            ,  COLUMN_UID = "uid"
            ,  COLUMN_NAME = "name"
            ,  COLUMN_PROFILE_PIC_RES_ID = "profilePicResId"
            ,  COLUMN_ABOUT = "about";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_UID + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PROFILE_PIC_RES_ID + " TEXT, " +
                COLUMN_ABOUT + " TEXT);";
        db.execSQL(query);

        addUsers("df","ddf","jkd","kdjf");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addUsers(String uid, String name, String profilePicResId,String about){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_UID, uid);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_PROFILE_PIC_RES_ID, profilePicResId);
        cv.put(COLUMN_ABOUT, about);

        long result = db.insert(TABLE_NAME,null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    void updateData(String uid, String name, String profilePicResId,String about){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_UID, uid);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_PROFILE_PIC_RES_ID, profilePicResId);
        cv.put(COLUMN_ABOUT, about);

        long result = db.update(TABLE_NAME, cv, "uid=?", new String[]{uid});
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }

    }
    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
