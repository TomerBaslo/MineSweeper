package com.example.tomer.minesweeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.tomer.minesweeper.Logic.Game;

public class MainActivity extends AppCompatActivity {
    public static final String DEFAULT = "N/A";
    public static final String DEFAULT_RECORD = "9999999";
    public static final String DIFFICULTY_KEY="Difficulty key";
    public static final String BUNDLE_KEY="Bundle key";
    private RadioButton mRadioEasy,mRadioMedium,mRadioHard;
    private Button mPlayButton, mLeaderboardsButton;
    private Game.Difficulty mDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRadioEasy=(RadioButton)findViewById(R.id.radio_easy);
        mRadioMedium=(RadioButton)findViewById(R.id.radio_medium);
        mRadioHard=(RadioButton)findViewById(R.id.radio_hard);
        mPlayButton=(Button)findViewById(R.id.play_button);
        mLeaderboardsButton = (Button) findViewById(R.id.leaderboards_button);
        mLeaderboardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLeaderboardsActivity();
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("GameRecordData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(mRadioEasy.isChecked()){
                    mDifficulty=Game.Difficulty.EASY;
                    editor.putString("chosenDifficulty",Game.Difficulty.EASY.name());
                    editor.commit();
                }
                else if(mRadioMedium.isChecked()){
                    mDifficulty=Game.Difficulty.MEDIUM;
                    editor.putString("chosenDifficulty",Game.Difficulty.MEDIUM.name());
                    editor.commit();
                }
                else if(mRadioHard.isChecked()){
                    mDifficulty=Game.Difficulty.HARD;
                    editor.putString("chosenDifficulty",Game.Difficulty.HARD.name());
                    editor.commit();

                }
                startGame();

            }

        });

        SharedPreferences sharedPreferences = getSharedPreferences("GameRecordData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String chosenDifficulty = sharedPreferences.getString("chosenDifficulty", DEFAULT);

        if(chosenDifficulty.equals(DEFAULT)){
            editor.putString("chosenDifficulty", Game.Difficulty.EASY.name());
            editor.commit();
            mRadioEasy.setChecked(true);
        }
        else{
            if(chosenDifficulty.equals(Game.Difficulty.EASY.name())){
                mRadioEasy.setChecked(true);
            }
            else if(chosenDifficulty.equals(Game.Difficulty.MEDIUM.name())){
                mRadioMedium.setChecked(true);
            }
            else if(chosenDifficulty.equals(Game.Difficulty.HARD.name())){
                mRadioHard.setChecked(true);
            }
        }

        setRecordText();

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void setRecordText(){
        SharedPreferences sharedPreferences = getSharedPreferences("GameRecordData", Context.MODE_PRIVATE);
        String easyRecord = sharedPreferences.getString("easyRecord", DEFAULT_RECORD);
        String mediumRecord = sharedPreferences.getString("mediumRecord", DEFAULT_RECORD);
        String hardRecord = sharedPreferences.getString("hardRecord", DEFAULT_RECORD);

        if(!easyRecord.equals(DEFAULT_RECORD)){
            mRadioEasy.setText("EASY " + " { Record: " + easyRecord+ " }");
        }
        if(!mediumRecord.equals(DEFAULT_RECORD)){
            mRadioMedium.setText("MEDIUM " + " { Record: " + mediumRecord+ " }");
        }
        if(!hardRecord.equals(DEFAULT_RECORD)){
            mRadioHard.setText("HARD " + " { Record: " + hardRecord+ " }");
        }
    }
    private void startGame(){
        Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);

        //Creating a bundle for the intent:
        Bundle bundle = new Bundle();
        bundle.putSerializable(DIFFICULTY_KEY,mDifficulty);
        gameIntent.putExtra(BUNDLE_KEY, bundle);

        startActivity(gameIntent);
    }

    private void startLeaderboardsActivity() {

        //Creating an intent:
        Intent leaderboardsIntent = new Intent(this, LeaderboardsActivity.class);

        //Starting the Leaderboards Activity:
        startActivity(leaderboardsIntent);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                } else {
                    // permission denied.
                    //Instantiate an AlertDialog.Builder with its constructor:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    //Chain together various setter methods to set the dialog characteristics:
                    builder.setMessage("Permission for your location must be granted in order to use this app.")
                            .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                            Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                }
                            }).setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    }).setCancelable(false).create().show();
                }
                return;
            }
        }
    }




}

