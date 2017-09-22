package com.example.tomer.minesweeper;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.example.tomer.minesweeper.Logic.Game;

import static com.example.tomer.minesweeper.MainActivity.DEFAULT_RECORD;


public class GameActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, AlertService.AlertListener {
    public static final String BUNDLE_KEY="Bundle key";
    public static final String GAME_STATUS_KEY="GameStatusKey";
    public static final String GAME_TIME_KEY = "GameTime";
    public static final String LOCATION_KEY = "Location";
    public static final String MODE_KEY = "Mode";
    private Game mGame;
    private GridView mGridView;
    private RadioButton mFlagRadio,mNormalRadio;
    private TextView mTimerText;
    private int mTimePassed=0;

    private Thread mTimer;

    private int mLastClickedPosition = Constants.POSITION_DOES_NOT_EXIST;

    private AnimatorSet mEndGameAnimatorSet;
    private AnimatorSet mAlertAnimatorSet;

    // Fields regarding the intent service:
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested = false;
    private String mAddressOutput;
    private GoogleApiClient mGoogleApiClient;

    // Booleans for the end-game process...
    private boolean mAnimationsAreFinished = false;
    private boolean mIntentServiceNoLongerNeeded = false;
    private boolean mShouldUpdateDatabase = false;
    private boolean mEndGameIsHandled = false;

    // Fields for rotation service:
    private ServiceConnection mConnection;
    AlertService.AlertBinder mBinder;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        extractDateFromBundle();
        mTimerText=(TextView)findViewById(R.id.timer_text);
        mFlagRadio=(RadioButton)findViewById(R.id.flag_radio);
        mNormalRadio=(RadioButton)findViewById(R.id.normal_radio);
        mNormalRadio.setChecked(true);
        mGridView = (GridView) findViewById(R.id.grid_view);
        mGridView.setNumColumns(mGame.getmBoard().getmNumberOfCols());
        mGridView.setAdapter(new TileAdapter(getApplicationContext(),mGame.getmBoard()));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mNormalRadio.isChecked())
                    mGame.setmFlagState(false);
                if(mFlagRadio.isChecked())
                    mGame.setmFlagState(true);
                mGame.playTile(position/mGame.getmBoard().getmNumberOfCols(),position%mGame.getmBoard().getmNumberOfCols());
                // refreshing the grid:
                ((TileAdapter) mGridView.getAdapter()).notifyDataSetChanged();

                if(mGame.getmGameState()== Game.GameState.WON || mGame.getmGameState()== Game.GameState.LOST){
                    if(mGame.getmGameState()== Game.GameState.WON) updateRecord();
                    handleGameEnding(position);
                }


            }
        });
        setupTimer();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Connection for the rotation service:
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("Service Connection", "bound to service");
                mBinder = (AlertService.AlertBinder)service;
                mBinder.registerListener(GameActivity.this);
                Log.d("Service Connection", "registered as listener");
                isBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // connect:
        mGoogleApiClient.connect();
        // bind service:
        Intent intent = new Intent(this, AlertService.class);
        Log.d("On start", "binding to service...");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // disconnect:
        mGoogleApiClient.disconnect();
        // unbind service:
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }

    @Override
    public void alertOn() {

        // If the game hasn't ended:
        if(mGame.getmGameState() != Game.GameState.LOST && mGame.getmGameState() != Game.GameState.WON){

            // Punish player:
            mGame.punishPlayer();
            ((TileAdapter)mGridView.getAdapter()).notifyDataSetChanged();

            // Vibrate for 200 milliseconds:
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);

            // Display proper toast for 1 second:
            final Toast toast = Toast.makeText(getApplicationContext(),
                    "Mines are being added!!!", Toast.LENGTH_SHORT);
            toast.show();
            Handler toastHandler = new Handler();
            toastHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);

            // Start the proper animation:
            onStartPunishmentAnimation();

        }

    }

    @Override
    public void alertOff() {
        // If the game hasn't ended:
        if(mGame.getmGameState() != Game.GameState.LOST && mGame.getmGameState() != Game.GameState.WON){
            // Return the board parts to their original colors (just in case).
            View topBrick = findViewById(R.id.top_layout);
            topBrick.setBackgroundResource(R.color.colorDimGray);
            mGridView.setBackgroundResource(R.color.colorDimGray);
        }
    }



    private void onStartPunishmentAnimation(){

        //Setting up animator for background color:
        ObjectAnimator topColorAnimator = ObjectAnimator.ofObject(findViewById(R.id.top_layout),
                "backgroundColor",
                new ArgbEvaluator(),
                ContextCompat.getColor(this, R.color.colorAlert1),
                ContextCompat.getColor(this, R.color.colorAlert2));
        topColorAnimator.setInterpolator(new DecelerateInterpolator());
        topColorAnimator.setDuration(400);
        topColorAnimator.setRepeatCount(1);
        topColorAnimator.setRepeatMode(ValueAnimator.REVERSE);

        //Setting up animator for background color:
        ObjectAnimator gridColorAnimator = ObjectAnimator.ofObject(mGridView,
                "backgroundColor",
                new ArgbEvaluator(),
                ContextCompat.getColor(this, R.color.colorAlert1),
                ContextCompat.getColor(this, R.color.colorAlert2));
        gridColorAnimator.setInterpolator(new DecelerateInterpolator());
        gridColorAnimator.setDuration(400);
        gridColorAnimator.setRepeatCount(1);
        gridColorAnimator.setRepeatMode(ValueAnimator.REVERSE);

        //Setting up animator set and start:
        mAlertAnimatorSet = new AnimatorSet();
        mAlertAnimatorSet.playTogether(topColorAnimator, gridColorAnimator);
        mAlertAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                // If the game has ended:
                if(mGame.getmGameState() == Game.GameState.LOST || mGame.getmGameState() == Game.GameState.WON){
                    // handle the games ending if not handled yet:
                    if(!mEndGameIsHandled){
                        mEndGameIsHandled = true;
                        handleGameEnding(Constants.POSITION_DOES_NOT_EXIST); // (the game has ended
                        // and there is no last clicked tile position).
                    }
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mAlertAnimatorSet.start();
    }


    private void handleGameEnding(int position) {
        Log.d("handleGameEnding", "method is activated.");

        mTimer.interrupt(); //Stop timer.
        mLastClickedPosition = position; //Set last-clicked position.

        // Return the board parts to their original colors (just in case).
        View topBrick = findViewById(R.id.top_layout);
        topBrick.setBackgroundResource(R.color.colorDimGray);
        mGridView.setBackgroundResource(R.color.colorDimGray);

        // If won:
        if(mGame.getmGameState() == Game.GameState.WON){

            // Try to update the time record:
            updateRecord();

            // Create dbHelper:
            DatabaseHelper dbHelper = new DatabaseHelper(GameActivity.this);

            // If worthy to enter the leaderboard:
            if(dbHelper.isARecord(mTimerText.getText().toString(), mGame.getmDifficulty().toString())){

                mShouldUpdateDatabase = true;

                // Only start the service to fetch the address if GoogleApiClient is
                // connected.
                if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                    startIntentService();
                }
                // If GoogleApiClient isn't connected, process the user's request by
                // setting mAddressRequested to true. Later, when GoogleApiClient connects,
                // launch the service to fetch the address.
                mAddressRequested = true;

            }
            else
                mIntentServiceNoLongerNeeded = true;


            // Starting Win animation (and then start the conclusion activity):
            onStartWinAnimation();

        }
        else{ // If lost:

            mIntentServiceNoLongerNeeded = true;

            // Vibrate:
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500); //Vibrate for 500 milliseconds.

            // Starting Lose animation (and then start the conclusion activity):
            onStartLoseAnimation();

        }

    }

    private void onStartLoseAnimation() {

        //For each tile, set random x, y, z rotation directions (positive = true, negative = false).
        //As well as setting the falling rate.
        Random rnd = new Random();
        final boolean[][] rotationDirections3D = new boolean[mGridView.getCount()][3];
        final float[] fallingRate = new float[mGridView.getCount()];
        for(int i = 0; i < mGridView.getCount(); i++) {
            rotationDirections3D[i][0] = rnd.nextBoolean();
            rotationDirections3D[i][1] = rnd.nextBoolean();
            rotationDirections3D[i][2] = rnd.nextBoolean();
            fallingRate[i] = rnd.nextFloat()/5 + 1; //Between 1.0-1.2
        }

        // Set animator for tiles rotation:
        ValueAnimator tileRotationAnimator = ValueAnimator.ofFloat(0, 40);

        //Setting interpolation for tiles rotation animator:
        tileRotationAnimator.setInterpolator(new DecelerateInterpolator());

        // Set update listener for tiles rotation animator:
        tileRotationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                // For each tile in the grid:
                for(int i = 0; i < mGridView.getCount(); i++) {

                    // Get the tile:
                    TileView tile = (TileView) mGridView.getChildAt(i);

                    // If tile exists and its not the exploding tile:
                    if(tile != null && i != mLastClickedPosition){

                        // Rotate around the X axis:
                        if(rotationDirections3D[i][0])
                            tile.setRotationX(value);
                        else
                            tile.setRotationX(-value);

                        // Rotate around the Y axis:
                        if(rotationDirections3D[i][1])
                            tile.setRotationY(value);
                        else
                            tile.setRotationY(-value);

                        // Rotate around the Z axis:
                        if(rotationDirections3D[i][2])
                            tile.setRotation(value);
                        else
                            tile.setRotation(-value);

                    }
                }
            }
        });

        //Get screen height:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final float screenHeight = displaymetrics.heightPixels;

        // Set animator for tiles position:
        ValueAnimator tilePositionAnimator = ValueAnimator.ofFloat(0, screenHeight);

        // Set interpolator for tiles position animator:
        tilePositionAnimator.setInterpolator(new LinearInterpolator());

        // Set update listener for tiles position animator:
        tilePositionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                // For each tile in the grid:
                for(int i = 0; i < mGridView.getCount(); i++) {

                    // Get the tile:
                    TileView tile = (TileView) mGridView.getChildAt(i);

                    //If tile exists and its not the exploding tile:
                    if(tile != null && i != mLastClickedPosition) {
                        tile.setTranslationY(value * fallingRate[i]);
                    }
                }
            }
        });

        //Setting up animator for background color:
        ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(findViewById(R.id.grid_view), "backgroundColor",
                new ArgbEvaluator(),
                ContextCompat.getColor(this, R.color.colorDimGray),
                ContextCompat.getColor(this, R.color.colorGhostWhite));

        //Setting up animator set:
        mEndGameAnimatorSet = new AnimatorSet();
        mEndGameAnimatorSet.playTogether(tileRotationAnimator, tilePositionAnimator, backgroundColorAnimator);
        mEndGameAnimatorSet.setDuration(5000L);

        //Adding listener for animator set (in order to start the Conclusion Activity when animations end)
        mEndGameAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationsAreFinished = true;
                if(mIntentServiceNoLongerNeeded)
                    startFinalActivity();
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        // Start all animations:
        mEndGameAnimatorSet.start();


        // Also start explosion sprite if required:

        if(mLastClickedPosition != Constants.POSITION_DOES_NOT_EXIST) {

            final int FRAME_TIME = 130;
            final int FRAMES = 16;

            final TileView explodingTile = (TileView) mGridView.getChildAt(mLastClickedPosition);
            explodingTile.setElevation(16);
            explodingTile.setScaleX(4);
            explodingTile.setY(explodingTile.getY() - (int)(345*screenHeight*0.0015));

            ValueAnimator explosionAnimator = ValueAnimator.ofFloat(0, FRAME_TIME*FRAMES);
            explosionAnimator.setDuration(FRAME_TIME*FRAMES);
            explosionAnimator.setInterpolator(new LinearInterpolator());
            explosionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    if (value < FRAME_TIME) explodingTile.setBackgroundResource(R.drawable.explosion1);
                    else if (value < FRAME_TIME*2) explodingTile.setBackgroundResource(R.drawable.explosion2);
                    else if (value < FRAME_TIME*3) explodingTile.setBackgroundResource(R.drawable.explosion3);
                    else if (value < FRAME_TIME*4) explodingTile.setBackgroundResource(R.drawable.explosion4);
                    else if (value < FRAME_TIME*5) explodingTile.setBackgroundResource(R.drawable.explosion5);
                    else if (value < FRAME_TIME*6) explodingTile.setBackgroundResource(R.drawable.explosion6);
                    else if (value < FRAME_TIME*7) explodingTile.setBackgroundResource(R.drawable.explosion7);
                    else if (value < FRAME_TIME*8) explodingTile.setBackgroundResource(R.drawable.explosion8);
                    else if (value < FRAME_TIME*9) explodingTile.setBackgroundResource(R.drawable.explosion9);
                    else if (value < FRAME_TIME*10) explodingTile.setBackgroundResource(R.drawable.explosion10);
                    else if (value < FRAME_TIME*11) explodingTile.setBackgroundResource(R.drawable.explosion11);
                    else if (value < FRAME_TIME*12) explodingTile.setBackgroundResource(R.drawable.explosion12);
                    else if (value < FRAME_TIME*13) explodingTile.setBackgroundResource(R.drawable.explosion13);
                    else if (value < FRAME_TIME*14) explodingTile.setBackgroundResource(R.drawable.explosion14);
                    else if (value < FRAME_TIME*15) explodingTile.setBackgroundResource(R.drawable.explosion15);
                    else explodingTile.setBackgroundResource(R.drawable.explosion16);

                }
            });
            explosionAnimator.start();
        }

    }


    private void onStartWinAnimation() {

        final long ANIMATION_DURATION = 5000L;
        final int MIN_ANIMATION_REPEATS = 10;
        final int MAX_ANIMATION_REPEATS = 30;

        Random rnd = new Random();

        // Setting Array list of background color animators for the tiles:
        ArrayList<ObjectAnimator> animators = new ArrayList<ObjectAnimator>();

        // For each tile in the grid:
        for(int i = 0; i < mGridView.getCount(); i++) {

            // Get the tile:
            TileView tile = (TileView) mGridView.getChildAt(i);

            //If tile exists,
            if(tile != null) {

                // Set animator for that tile's color:
                ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile, "backgroundColor",
                        new ArgbEvaluator(),
                        ContextCompat.getColor(this, getRandomColor(rnd)),
                        ContextCompat.getColor(this, getRandomColor(rnd)));
                int animationRepeats = rnd.nextInt(MAX_ANIMATION_REPEATS - MIN_ANIMATION_REPEATS) + MIN_ANIMATION_REPEATS;
                tileColorAnimator.setRepeatCount(animationRepeats - 1);
                tileColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
                tileColorAnimator.setDuration(ANIMATION_DURATION/animationRepeats);
                animators.add(tileColorAnimator);

            }

        }

        // Set animator for everything to fade away:
        ValueAnimator allFadeAnimator = ValueAnimator.ofFloat(1,0);
        allFadeAnimator.setDuration(ANIMATION_DURATION);
        allFadeAnimator.setInterpolator(new AccelerateInterpolator());
        allFadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mGridView.setAlpha(value);
            }
        });

        // Setting up animator set:
        mEndGameAnimatorSet = new AnimatorSet();
        mEndGameAnimatorSet.playTogether((Collection) animators);

        // Adding listener for animator set (in order to start the Conclusion Activity when animations end)
        mEndGameAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                //Start the Conclusion Activity:
                mAnimationsAreFinished = true;
                if(mIntentServiceNoLongerNeeded)
                    startFinalActivity();
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        // Start animations:
        mEndGameAnimatorSet.start();
        allFadeAnimator.start();
    }

    private int getRandomColor(Random rnd) {
        int randomNumber = rnd.nextInt(3) + 1;
        switch(randomNumber){
            case 1:
                return R.color.colorBlue;
            case 2:
                return R.color.colorGreen;
            case 3:
                return R.color.colorRed;
            default:
                return 0;
        }
    }


    private void updateRecord(){

        //Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences("GameRecordData", Context.MODE_PRIVATE);
        String easyRecord = sharedPreferences.getString("easyRecord", DEFAULT_RECORD);
        String mediumRecord = sharedPreferences.getString("mediumRecord", DEFAULT_RECORD);
        String hardRecord = sharedPreferences.getString("hardRecord", DEFAULT_RECORD);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (mGame.getmDifficulty()){
            case EASY:
                //If the time this game lasted was shorter than the record,
                //update the time record for the EASY difficulty (lexicographic comparison will do):
                if(easyRecord.compareTo(String.valueOf(mTimePassed)) > 0){
                    editor.putString("easyRecord", String.valueOf(mTimePassed));
                    editor.commit();
                }
                break;
            case MEDIUM:
                //If the time this game lasted was shorter than the record,
                //update the time record for the MEDIUM difficulty (lexicographic comparison will do):
                if(mediumRecord.compareTo(String.valueOf(mTimePassed)) > 0){
                    editor.putString("mediumRecord", String.valueOf(mTimePassed));
                    editor.commit();
                }
                break;
            case HARD:
                //If the time this game lasted was shorter than the record,
                //update the time record for the HARD difficulty (lexicographic comparison will do):
                if(hardRecord.compareTo(String.valueOf(mTimePassed)) > 0){
                    editor.putString("hardRecord", String.valueOf(mTimePassed));
                    editor.commit();
                }
                break;
        }

    }
    private void startFinalActivity(){

        Intent finalGameIntent = new Intent(GameActivity.this, FinalActivity.class);

        //Creating a bundle for the intent:
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_STATUS_KEY,mGame.getmGameState());
        if(mShouldUpdateDatabase){
            bundle.putCharSequence(GAME_TIME_KEY, mTimerText.getText().toString());
            bundle.putCharSequence(LOCATION_KEY, mAddressOutput);
            bundle.putCharSequence(MODE_KEY, mGame.getmDifficulty().toString());
        }
        finalGameIntent.putExtra(BUNDLE_KEY, bundle);

        startActivity(finalGameIntent);
    }
    private void extractDateFromBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(MainActivity.BUNDLE_KEY);
        mGame= new Game((Game.Difficulty)bundle.getSerializable(MainActivity.DIFFICULTY_KEY));
    }
    private void setupTimer(){
        mTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mGame.getmGameState()!= Game.GameState.LOST && mGame.getmGameState()!= Game.GameState.WON) {
                    if (mGame.getmGameState()==Game.GameState.STARTED) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mTimePassed++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTimerText.setText("Timer: " + mTimePassed);
                            }
                        });
                    }
                }
            }
        });
        mTimer.start();
    }



    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Checking permission
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    Integer.parseInt(android.Manifest.permission.ACCESS_COARSE_LOCATION));
        }


        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Log.e("onConnected","no geocoder available");
                return;
            }

            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a log message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.e("onReceiveResult","address successfully found.");

                //Start the Conclusion Activity if animations were already finished:
                mIntentServiceNoLongerNeeded = true;
                if(mAnimationsAreFinished)
                    startFinalActivity();

            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mEndGameAnimatorSet != null){
            mEndGameAnimatorSet.end();
        }

    }
}
