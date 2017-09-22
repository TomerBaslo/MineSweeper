package com.example.tomer.minesweeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database files:
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Leaderboard.db";
    public static final String RECORDS_TABLE = "RecordsTable";
    public static final String ID = "id";
    public static final String PLAYER_NAME = "player_name";
    public static final String TIME = "time";
    public static final String LOCATION = "location";
    public static final String DIFFICULTY = "difficulty";

    public static final int RECORDS_FOR_EACH_MODE = 10;
    public static final int MAX_NICKNAME_SIZE = 12;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("create table " + RECORDS_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PLAYER_NAME + " TEXT, " + TIME + " TEXT, " + LOCATION + " TEXT, " + DIFFICULTY + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE);
        onCreate(database);
    }

    public boolean insertGame(String playerName, String gameTime, String location, String gameMode) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLAYER_NAME, playerName);
        contentValues.put(TIME, gameTime);
        contentValues.put(LOCATION, location);
        contentValues.put(DIFFICULTY, gameMode);
        long result = database.insert(RECORDS_TABLE, null, contentValues);

        // Check if insert failed
        if(result == -1) return false;

        Cursor cursor = database.rawQuery("select * from " + RECORDS_TABLE + " where " + DIFFICULTY + " = '" + gameMode + "' order by " + TIME + " ASC", null);
        if(cursor.getCount() == RECORDS_FOR_EACH_MODE + 1) {
            cursor.moveToLast();
            String worstGameID = cursor.getString(0);
            database.delete(RECORDS_TABLE, ID + " = ?", new String[] {worstGameID});
        }

        return true;

    }

    public boolean isARecord(String gameTime, String gameMode){

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select " + TIME + " from " + RECORDS_TABLE + " where " + DIFFICULTY + " = '" + gameMode + "' order by " + TIME + " ASC", null);

        if(cursor.getCount() < RECORDS_FOR_EACH_MODE) return true;

        cursor.moveToLast();
        if(cursor.getString(0).compareTo(gameTime) > 0) return true;

        //return false if the score is not good enough to enter the leaderboard.
        return false;

    }


    public Cursor getSortedRecords(String gameMode) {

        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("select " + PLAYER_NAME + ", " + TIME + ", " + LOCATION + " from " + RECORDS_TABLE +
                " where " + DIFFICULTY + " = '" + gameMode + "' order by " + TIME + " ASC", null);

    }


}
