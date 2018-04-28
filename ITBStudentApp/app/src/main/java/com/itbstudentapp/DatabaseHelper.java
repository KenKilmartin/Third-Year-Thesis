package com.itbstudentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Date;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "timetable";
    private static final String COL1 = "ID";
    private static final String COL2 = "startTime";
    private static final String COL3 = "endTime";
    private static final String COL4 = "class_event";
    private static final String COL5 = "day";
    private static final String COL6 = "room";
    private static final int DB_VERSION=2;


    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " + COL3 + " TEXT, "+ COL4 + " TEXT, "+ COL5 + " TEXT, "+ COL6 + " TEXT )";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d(TAG, "onUpgrade, tablename= " + TABLE_NAME+" version: "+i+","+ i1);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String startTime, String endTime, String class_event, String day, String room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, startTime);
        contentValues.put(COL3, endTime);
        contentValues.put(COL4, class_event);
        contentValues.put(COL5, day);
        contentValues.put(COL6, room);

        Log.d(TAG, "addData: Adding " + class_event + " to " + TABLE_NAME+" Content Values="+contentValues);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if data is inserted incorrectly it will return -1
        if (result == -1) {

            return false;
        } else {

            return true;
        }

    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    /**
     * Returns all entries for a particular day
     * @param day
     * @return
     */
    public Cursor getDataByDay(String day){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL5 + " = '" + day + "'";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    /**
     * Returns only the ID that matches the class_event and day passed in
     * @param class_event
     * @param day
     * @return
     */
    public Cursor getItemID(String class_event, String day){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL4 + " = '" + class_event + "'" +
                " AND " + COL5 + " = '" + day + "'";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    /**
     * Updates the timetable entry
     * @param id
     * @param newStartTime
     * @param newClass_event
     * @param newDay
     * @param newRoom
     */
    public void updateTimetableEntry(int id, String newStartTime,String newEndTime, String newClass_event, String newDay, String newRoom){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " +
                COL2 + " = '" + newStartTime + "'," +
                COL3 + " = '" + newEndTime + "'," +
                COL4 + " = '" + newClass_event + "'," +
                COL5 + " = '" + newDay + "'," +
                COL6 + " = '" + newRoom + "'" +
                " WHERE " + COL1 + " = " + id ;
        Log.d(TAG, "updateTimetableEntry: query: " + query);
        db.execSQL(query);

    }


    /**
     * Delete from database
     * @param id - id of entry to be deleted
     */
    public void deleteTimetableEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'";
        Log.d(TAG, "deleteTimetableEntry: query: " + query);
        Log.d(TAG, "deleteTimetableEntry: Deleting entry id: " + id + " from database.");
        db.execSQL(query);

    }

}
























