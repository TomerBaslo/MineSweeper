package com.example.tomer.minesweeper;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordView extends LinearLayout {

    public TextView numberText;
    public TextView nameText;
    public TextView timeText;
    public TextView locationText;

    private final int TEXT_SIZE = 16;


    public RecordView(Context context) {
        super(context);

        setOrientation(HORIZONTAL);

        //Setting up the View for numberText:
        numberText = new TextView(context);
        numberText.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
        numberText.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        numberText.setGravity(Gravity.LEFT);
        numberText.setTextColor(Color.BLACK);
        numberText.setTextSize(TEXT_SIZE);

        //Setting up the View for nameText:
        nameText = new TextView(context);
        nameText.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10));
        nameText.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        nameText.setGravity(Gravity.LEFT);
        nameText.setTextColor(Color.BLACK);
        nameText.setTextSize(TEXT_SIZE);

        //Setting up the View for timeText:
        timeText = new TextView(context);
        timeText.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5));
        timeText.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        timeText.setGravity(Gravity.LEFT);
        timeText.setTextColor(Color.BLACK);
        timeText.setTextSize(TEXT_SIZE);

        //Setting up the View for locationText:
        locationText = new TextView(context);
        locationText.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 12));
        locationText.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        locationText.setGravity(Gravity.LEFT);
        locationText.setTextColor(Color.BLACK);
        locationText.setTextSize(TEXT_SIZE);

        setPadding(14,6,14,6);

        setBackgroundColor(Color.GRAY);

        //Adding the views to RowView:
        addView(numberText);
        addView(nameText);
        addView(timeText);
        addView(locationText);

    }


}

