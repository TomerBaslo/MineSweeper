package com.example.tomer.minesweeper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.tomer.minesweeper.Logic.Game.Difficulty;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardsActivity extends AppCompatActivity
        implements LeaderboardFragment.OnGameSelectedListener, OnMapReadyCallback, SensorEventListener, LocationListener{

    private static final String TAG = "LeaderboardsActivity";

    private Button mChosenLabel;
    private Button mEasyLabel;
    private Button mMediumLabel;
    private Button mHardLabel;

    private GoogleMap mGoogleMap;
    private ArrayList<Marker> mMarkerList;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
    private float[] mDeviationMatrix = new float[16];
    private float mDeclination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        mChosenLabel = (Button) findViewById(R.id.easy_label);

        // Setting up difficulty labels:
        setupEasyLabel();
        setupMediumLabel();
        setupHardLabel();

        // Setting fragments
        setupLeaderboardFragment();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Setting up the deviation vector sensor:
        sutupDeviationVectorSensor();

    }


    private void sutupDeviationVectorSensor() {

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if(sensorList.get(0) != null) {
            Log.i("Sensor Activity","Rotation Vector Sensor Aquired");
            mRotationVectorSensor = sensorList.get(0);
        } else {
            Log.e("Sensor Activity","No Rotation Vector Sensor Available");
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor's listener:
        mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor's listener:
        mSensorManager.unregisterListener(this, mRotationVectorSensor);
    }


    @Override
    public void onLocationChanged(Location location) {

        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );
        mDeclination = field.getDeclination();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mDeviationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mDeviationMatrix, orientation);
            float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
            updateCamera(bearing);
        }
    }

    private void updateCamera(float bearing) {
        if(mGoogleMap != null){
            CameraPosition oldPos = mGoogleMap.getCameraPosition();
            CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 400, null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Create a new list of markers
        mMarkerList = new ArrayList<Marker>();

        // Checking permission
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    Integer.parseInt(android.Manifest.permission.ACCESS_COARSE_LOCATION));
        }

        // Set user's location enabled
        googleMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        //set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Create a LatLng object for the current location

        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        // Show the current location in Google Map
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10));

        mGoogleMap = googleMap;

        setupMap(Difficulty.EASY);

    }


    private void setupMap(Difficulty mode) {

        mGoogleMap.clear();

        if(mMarkerList != null && mMarkerList.size() > 0)
            mMarkerList.removeAll(mMarkerList);

        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        Cursor cursor = mDbHelper.getSortedRecords(mode.toString());

        Geocoder geocoder = new Geocoder(this);

        for(int i = 0; i < cursor.getCount(); i++) {

            cursor.moveToPosition(i);

            String title = cursor.getString(2);
            String snippet = "NO. " + i+1 + " – " + cursor.getString(0) + " (" + cursor.getString(1) + ")";

            List<Address> addressList = null;

            try {
                addressList = geocoder.getFromLocationName(cursor.getString(2), 1);
            } catch (IOException e) {
                Log.e(TAG, "setupMap: ", e);
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            MarkerOptions marker = new MarkerOptions().position(latLng).title(title).snippet(snippet);
            mMarkerList.add(mGoogleMap.addMarker(marker));

        }


        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener () {
            @Override
            public boolean onMarkerClick(Marker marker) {

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                for(int i = 0; i < mMarkerList.size(); i++) {

                    if(marker.equals(mMarkerList.get(i))){

                        Log.d("MarkersList", "marker in position " + i + " has been selected.");

                        // Update selected game in the leaderboardFragment:
                        LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                                getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                        leaderboardFragment.updateSelectedGame(i+1);

                        // Show marker info:
                        marker.showInfoWindow();

                        return true;

                    }

                }

                return false;

            }
        });

    }


    private void setupEasyLabel(){

        mEasyLabel = (Button) findViewById(R.id.easy_label);

        mEasyLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!mEasyLabel.equals(mChosenLabel)){

                    //Update chosen label:
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    mChosenLabel = mEasyLabel;
                    mChosenLabel.setBackgroundColor(Color.BLUE);

                    //Update mode for leaderboardFragment:
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Difficulty.EASY);

                    //Update mode for leaderboardFragment:
                    setupMap(Difficulty.EASY);

                }

            }
        });

    }


    private void setupMediumLabel(){

        mMediumLabel = (Button) findViewById(R.id.medium_label);

        mMediumLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!mMediumLabel.equals(mChosenLabel)){

                    //Update chosen label:
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    mChosenLabel = mMediumLabel;
                    mChosenLabel.setBackgroundColor(Color.BLUE);

                    //Update mode:
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Difficulty.MEDIUM);

                    //Update mode for leaderboardFragment:
                    setupMap(Difficulty.MEDIUM);

                }

            }
        });

    }


    private void setupHardLabel(){

        mHardLabel = (Button) findViewById(R.id.hard_label);

        mHardLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!mHardLabel.equals(mChosenLabel)){

                    //Update chosen label:
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    mChosenLabel = mHardLabel;
                    mChosenLabel.setBackgroundColor(Color.BLUE);

                    //Update mode:
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Difficulty.HARD);

                    //Update mode for leaderboardFragment:
                    setupMap(Difficulty.HARD);

                }

            }
        });

    }


    private void setupLeaderboardFragment() {

        LeaderboardFragment leaderboardFragment = (LeaderboardFragment)getSupportFragmentManager().
                findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);

        // If fragment doesn't exist yet, create one
        if (leaderboardFragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.leaderboard_fragment_container, LeaderboardFragment.newInstance(),
                            LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG)
                    .commit();
        }
        // else re-use the old fragment

    }

    @Override
    public void onGameSelected(int position) {

        // move the camera to the location of the selected game and show marker's info.

        if(position < mMarkerList.size()) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mMarkerList.get(position).getPosition()));
            mMarkerList.get(position).showInfoWindow();
        }
        else{
            for(int i = 0; i < mMarkerList.size(); i++){
                if(mMarkerList.get(i).isInfoWindowShown()) {
                    mMarkerList.get(i).hideInfoWindow();
                    return;
                }
            }
        }

    }


}
