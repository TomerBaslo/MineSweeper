package com.example.tomer.minesweeper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tomer.minesweeper.Logic.Game;


public class FinalActivity extends AppCompatActivity {

    private DatabaseHelper mDbHelper;
    private String mGameTime;
    private String mLocation;
    private String mMode;
    private String mPlayerName;

    //Dialogs:
    private Dialog mWorthyDialog;
    private AlertDialog mVerificationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        extractDataFromBundle();

        setupTryAgainButton();

        mMode = Game.Difficulty.EASY.name();

    }


    private void extractDataFromBundle() {

        //Getting the intent:
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(GameActivity.BUNDLE_KEY);

        //Setting the right text for the conclusion text:
        TextView conclusionText = (TextView) findViewById(R.id.conclusion_text);
        if(bundle.getSerializable(GameActivity.GAME_STATUS_KEY)== Game.GameState.WON){
            conclusionText.setText("Good job! you won!");
        }
        else{
            conclusionText.setText("Tough luck try again!");
        }

        // Check if the bundle has data regarding a game worthy to enter the leaderboard
        if(bundle.size() > 1){
            mDbHelper = new DatabaseHelper(this);
            mGameTime = bundle.getString(GameActivity.GAME_TIME_KEY);
            mLocation = bundle.getString(GameActivity.LOCATION_KEY);
            mMode = bundle.getString(GameActivity.MODE_KEY);
            createWorthyDialog();
        }
    }

    private void createWorthyDialog() {

        mPlayerName = "";

        mWorthyDialog = new Dialog(this);
        mWorthyDialog.setContentView(R.layout.layout_new_record);
        final EditText playerNameEditable = (EditText) mWorthyDialog.findViewById(R.id.player_name_text);
        Button doneButton = (Button) mWorthyDialog.findViewById(R.id.done_button);
        Button cancelButton = (Button) mWorthyDialog.findViewById(R.id.cancel_button);
        mWorthyDialog.show();

        mWorthyDialog.setCancelable(false);

        //Set the maximum length of the text:
        playerNameEditable.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DatabaseHelper.MAX_NICKNAME_SIZE)});

        playerNameEditable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                mPlayerName = editable.toString();
            }

        });

        //Setting up a listener for the Done Button:
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If text is empty, show a suitable toast.
                if (mPlayerName.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter at least one letter.", Toast.LENGTH_SHORT).show();
                } else { //Otherwise, update the leaderboard and dismiss dialog
                    mDbHelper.insertGame(mPlayerName, mGameTime, mLocation, mMode);
                    mWorthyDialog.dismiss();
                }

            }
        });

        //Setting up a listener for the Cancel Button:
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createVerificationDialog(mWorthyDialog);
            }
        });

    }


    private void createVerificationDialog(final Dialog previousDialog) {

        //Instantiate an AlertDialog.Builder with its constructor:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Chain together various setter methods to set the dialog characteristics:
        builder.setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked YES button
                        // the player's accomplishment will not enter the leaderboard.
                        previousDialog.dismiss(); //Dismiss WorthyDialog
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked NO (cancelled the dialog)
                // Return to WorthyDialog:
            }
        });

        mVerificationDialog = builder.create();
        mVerificationDialog.show();

    }

    private void setupTryAgainButton() {

        Button button = (Button) findViewById(R.id.try_again_button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startGameActivity();
                finish();

            }
        });

    }


    private void startGameActivity() {


        Intent gameIntent = new Intent(this, MainActivity.class);


        startActivity(gameIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(mWorthyDialog != null){
            mWorthyDialog.dismiss();
        }
        if(mVerificationDialog != null) {
            mVerificationDialog.dismiss();
        }

    }
}
