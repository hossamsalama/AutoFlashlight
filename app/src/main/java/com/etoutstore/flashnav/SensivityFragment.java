package com.etoutstore.flashnav;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.SENSOR_SERVICE;
import static com.etoutstore.flashnav.MainActivity.camera;

public class SensivityFragment extends Fragment implements SensorEventListener {
    private static SensorManager mSensorManager;
    private Sensor mLight;
    private TextView textView;
    private ProgressBar bar;
    private Button torch;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Please note the third parameter should be false, otherwise a java.lang.IllegalStateException maybe thrown.
        View rootView = inflater.inflate(R.layout.senstivity_layout, container, false);
        bar = rootView.findViewById(R.id.progressBar);

        textView = rootView.findViewById(R.id.intensity_value);
        Typeface typeface = getResources().getFont(R.font.digitalregular);
        textView.setTypeface(typeface);

        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);

        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        //check if sensor exist on the device before using it
        if (mLight == null){
            /*
             * First check if light sensor exist
             */
            boolean hasSensor = getContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);

            if (!hasSensor) {
                // Sensor not exist
                // Show alert message and continue running the app without exit from this app.
                AlertDialog alert = new AlertDialog.Builder(getContext())
                        .create();
                alert.setTitle("oops!");
                alert.setMessage("Sorry, your device doesn't support sensor light!");
                alert.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.show();
                return;
                //finish();  //uncomment this method if you want this app to exit after showing alert.
            }
        }
        mSensorManager.registerListener(
                this,
                mLight,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseSensor();
    }

    /*
     * Release sensor resources as user decided to exit from AutoFlash activity.
     */
    private void releaseSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            textView.setText("" + event.values[0] + "\n  Lux");
            float lightLevel = event.values[0];
            bar.setProgress((int) event.values[0] * 20);
            if (lightLevel == 0) {
                camera.switchFlashLight(true);
            } else {
                camera.switchFlashLight(false);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
