package com.example.tomer.minesweeper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.tomer.minesweeper.Logic.Game.Difficulty;

public class RecordAdapter extends BaseAdapter {

    private Difficulty mDifficulty;
    private int mSelectedGame;
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;

    private static final int MAX_LOCATION_LENGTH = 16;

    public void setSelectedGame(int pickedGame){
        mSelectedGame = pickedGame;
    }
    public void setDifficulty(Difficulty difficulty){
        mDifficulty = difficulty;
    }

    public RecordAdapter(Context context, Difficulty difficulty) {

        mDifficulty = difficulty;
        mContext = context;
        mSelectedGame = 0;
        mDatabaseHelper = new DatabaseHelper(mContext);

    }


    @Override
    public int getCount() {
        return DatabaseHelper.RECORDS_FOR_EACH_MODE;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RecordView record;
        record = (RecordView)convertView;
        if(record == null) {
            record = new RecordView(mContext);
        }

        record.numberText.setText(String.format("%3s", position + 1 + "."));

        //If database has data for this position:
        if(getItem(position) != null){

            record.nameText.setText(getItem(position)[0]);
            record.timeText.setText(getItem(position)[1]);

            if(getItem(position)[2].length() > MAX_LOCATION_LENGTH) {
                record.locationText.setText(getItem(position)[2].substring(0, MAX_LOCATION_LENGTH - 1) + "...");
            }
            else {
                record.locationText.setText(getItem(position)[2]);
            }

        }

        else {
            record.nameText.setText("");
            record.timeText.setText("");
            record.locationText.setText("");
        }

        //If the row is selected, mark it.
        if(mSelectedGame == position + 1) {
            record.setBackgroundColor(Color.BLUE);
        }
        else { //Otherwise, don't.
            record.setBackgroundColor(Color.GRAY);
        }

        return record;

    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public String[] getItem(int position) {

        Cursor cursor = mDatabaseHelper.getSortedRecords(mDifficulty.toString());

        if (cursor.getCount() <= position){
            return null;
        }

        String[] row = new String[cursor.getColumnCount()];
        cursor.moveToPosition(position);

        for(int i = 0; i < row.length; i++){
            row[i] = cursor.getString(i);
        }

        return row;

    }

}
