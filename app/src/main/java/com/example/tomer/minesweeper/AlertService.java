package com.example.tomer.minesweeper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class AlertService extends Service implements SensorEventListener{

    private final int MAX_ALLOWED_DEVIATION = 30;
    private final IBinder BINDER = new AlertBinder();

    private float[] mAnglesMatrix = new float[16];
    private float[] mFirstOrientation = null;
    private boolean mIsDeviationOK = true;
    private AlertListener mAlertListener;
    private SensorManager mSensorManager;
    private Sensor mAngleVectorSensor;
    private Handler mHandler;
    private int mInterval = 1000; // read sensor each second
    private boolean mIsReadingSensorRequired = false;

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            mIsReadingSensorRequired = true;
            mHandler.postDelayed(this, mInterval);
        }
    };


    public class AlertBinder extends Binder {

        void registerListener(AlertListener listener) {
            Log.d("Binder", "binding...");
            mAlertListener = listener;

        }

    }

    public interface AlertListener {

        void alertOn();

        void alertOff();

    }



    @Override
    public IBinder onBind(Intent intent) {

        mHandler = new Handler();

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if(sensorList.size() > 0) {
            Log.i("Sensor Activity","Rotation Vector Sensor Aquired");
            mAngleVectorSensor = sensorList.get(0);
        } else {
            Log.e("Sensor Activity","No Rotation Vector Sensor Available");
        }

        // Register sensor listener:
        mSensorManager.registerListener(this, mAngleVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mHandler.post(processSensors);

        return BINDER;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        // remove callbacks and unregister sensor listener:
        mHandler.removeCallbacks(processSensors);
        mSensorManager.unregisterListener(this, mAngleVectorSensor);
        return super.onUnbind(intent);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("onSensorChanged", "method activated");
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && mIsReadingSensorRequired) {

            if(mFirstOrientation == null){
                // Set first orientation
                mFirstOrientation = new float[3];
                SensorManager.getRotationMatrixFromVector(mAnglesMatrix, event.values);
                float[] orientation = new float[3];
                SensorManager.getOrientation(mAnglesMatrix, orientation);
                mFirstOrientation[0] = (float)Math.toDegrees(orientation[0]);
                mFirstOrientation[1] = (float)Math.toDegrees(orientation[1]);
                mFirstOrientation[2] = (float)Math.toDegrees(orientation[2]);
            }
            else {
                // Check deviation:
                SensorManager.getRotationMatrixFromVector(mAnglesMatrix, event.values);
                float[] orientation = new float[3];
                SensorManager.getOrientation(mAnglesMatrix, orientation);
                float[] bearing = {(float)Math.toDegrees(orientation[0]) - mFirstOrientation[0],
                        (float)Math.toDegrees(orientation[1]) - mFirstOrientation[1],
                        (float)Math.toDegrees(orientation[2]) - mFirstOrientation[2]};

                // If deviation is too high.
                if(Math.abs(bearing[1]) > MAX_ALLOWED_DEVIATION){
                    mIsDeviationOK = false;
                    mAlertListener.alertOn();
                }
                else if(!mIsDeviationOK){
                    mIsDeviationOK = true;
                    mAlertListener.alertOff();
                }
            }
            mIsReadingSensorRequired = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
