package com.burtonshead.burningeye.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;

import com.burtonshead.burningeye.R;

import java.util.Vector;

/**
 * Created by burton on 3/7/16.
 */
public class TiltManager implements SensorEventListener
{
    public TiltManager(Activity a)
    {
        mActivity = a;
        try
        {
            mSensorMgr = (SensorManager) mActivity.getSystemService("sensor");
            mAccSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//            mRotSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            mSensorMgr.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorMgr.registerListener(this, mMagSensor, SensorManager.SENSOR_DELAY_GAME);
//            mSensorMgr.registerListener(this, mRotSensor, SensorManager.SENSOR_DELAY_GAME);

            mRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        }
        catch (Exception e)
        {
            Exception x = e;
            Log.e("TiltManager", "No Sensor Present");
        }

    }

    public void addTiltCalibrationListener(TiltCalibrationListener listener)
    {
        mListeners.add(listener);
    }

    public float getOrientX()
    {
        return mOrientX;
    }

    public float getOrientY()
    {
        return mOrientY;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //Log.i("TiltManager", "OnAccuracyChanged = accuracy, sensor = " + sensor.getName());

        if (accuracy ==  SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            //Log.i("TiltManager", "*** SENSOR_STATUS_UNRELIABLE ***");

            forceCalibration();
        }
        else
        {
            switch (accuracy)
            {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    //Log.i("TiltManager", "*** SENSOR STATUS = HIGH");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    //Log.i("TiltManager", "*** SENSOR STATUS = LOW");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    //Log.i("TiltManager", "*** SENSOR STATUS = MEDIUM");
                    break;
            }

            // clear calibration dialog
            if (sensor == mMagSensor && mDialog != null)
            {
                mDialog.dismiss();
            }

            // inform tilt listeners
            reportTiltOK();
        }
    }

    /**
     * Captures Accelerometer and Magnetometer to get values
     * needed to determine device tilt
     * @param event
     */
    public void onSensorChanged(SensorEvent event)
    {
        final String comma = ", ";
        if (event.accuracy != 0)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                System.arraycopy(event.values, 0, mAccValues, 0, event.values.length);
                //Log.i("onSensorChanged.Accelerometer", event.values[0] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                System.arraycopy(event.values, 0, mMagValues, 0, event.values.length);
                //Log.i("onSensorChanged.MagneticField", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

//            if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR)
//            {
//                System.arraycopy(event.values, 0, mRotValues, 0, event.values.length);
//                //Log.i("onSensorChanged.MagneticField", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
//            }

            if (mAccValues == null)
            {
                //Log.i("GameLogic.onSensorChanged", "\n\n\n ACCELEROMETER NULL \n\n\n");
                // This sensor is noisy, skip this iteration and keep trying
            }
            else if (!isMagOK())
            {
                // mag should always have good values
                //Log.i("GameLogic.onSensorChanged", "\n\n\n MAGNEMOMETER NULL \n\n\n");

                mBadMagValues++;

                if (mBadMagValues > MAG_THRESHOLD)
                {
                    forceCalibration();
                }
            }
            else
            {
                // Sensor values are good!  Calculate the Orientation!
                //Log.i("onSensorChanged", "mAccValues = " + mAccValues.toString() + ", mMagValues = " + mMagValues.toString());

                // clear bad mag values
                mBadMagValues = 0;

                calculateOrientation();
            }

            //Log.i("onSensorChanged", "*** mOrientX = " + mOrientX + ", mOrientY = " + mOrientY + "***");
        }
    }

    public interface TiltCalibrationListener
    {
        void onTiltFail();
        void onTiltOK();
    }


    private static final int MAG_THRESHOLD = 20;

    private Activity mActivity = null;
    private SensorManager mSensorMgr = null;
    private float mOrientX = 0f;
    private float mOrientY = 0f;
    private int mRotation = Surface.ROTATION_0;   // tablets and phones have different rotation settings - which reverses X/Y coordinates
    private float[] mAccValues = new float[3];    // Acclerometer values for Orientation calculation
    private float[] mMagValues = new float[3];    // Magnetometer values for current Orientation calculation
//    private float[] mRotValues = new float[4];    // Game Rotation values for current Orientation calculation
    private float[] mOrientationResult = new float[3];    // Intermediate values used to determine Orientation - stored once for memory efficiency
    private float[] mOrientationRotationMatrix = new float[9];    // Intermediate values used to determine Orientation - stored once for memory efficiency
    private float[] mOrientationTransformMatrix = new float[9];   // Intermediate values used to determine Orientation - stored once for memory efficiency

    private AlertDialog mDialog = null;

    private Vector<TiltCalibrationListener> mListeners = new Vector<>();

    private Sensor mAccSensor = null;
    private Sensor mMagSensor = null;
//    private Sensor mRotSensor = null;

    private int mBadMagValues = 0;

    private void calculateOrientation()
    {
        try
        {
            SensorManager.getOrientation(mOrientationRotationMatrix, mOrientationResult);
            SensorManager.getRotationMatrix(mOrientationRotationMatrix, null, mAccValues, mMagValues);
            SensorManager.remapCoordinateSystem(mOrientationRotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Y, mOrientationTransformMatrix);
            mOrientY = (float) Math.toDegrees((double) mOrientationResult[2]);
            mOrientX = (float) -Math.toDegrees((double) mOrientationResult[1]);

            if (mRotation == Surface.ROTATION_0)
            {
                float x = mOrientY;
                mOrientY = -mOrientX;
                mOrientX = x;
            }

        }
        catch (Exception x)
        {
            Log.e("calcOrientation", "\n\n Problem calculating orientation \n\n", x);
        }
        //Log.i("calculateOrientation", "*** mOrientX = " + mOrientX + ", mOrientY = " + mOrientY + "***");
    }

    private boolean isMagOK()
    {
        if (mMagValues == null)
        {
            return false;
        }

        return !(mMagValues[0] == 0 && mMagValues[1] == 0 && mMagValues[2] == 0);
    }

    private void forceCalibration()
    {
        if (mDialog != null)
        {
            // already showing dialog - bail out
            return;
        }

        // inform tilt listeners
        reportTiltFail();

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                // clear out mDialog - game state takes care of itself
                mDialog = null;
            }
        };

        // get user to calibrate device
        mDialog  = new AlertDialog.Builder(mActivity)
                .setTitle("Tilt Calibration Required")
                //.setMessage("The tilt sensor is confused.  Rotate your device on all three axes, and that should fix it.  Keep turning the device until this message disappears")
                .setView(R.layout.calibrate_img)
                //.setPositiveButton("OK", listener)
                .show();
    }

    private void reportTiltFail()
    {
        for (TiltCalibrationListener l : mListeners)
        {
            l.onTiltFail();
        }
    }

    private void reportTiltOK()
    {
        for (TiltCalibrationListener l : mListeners)
        {
            l.onTiltOK();
        }
    }

}
