package com.example.theawesomeguy.group7;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by peps on 6/17/17.
 */

public class AccMtrService  extends IntentService implements SensorEventListener {


    private SensorManager sensorManager;
    private DBHelper dbHelper= null;

    public AccMtrService(){
        super("AccMtrService");
        dbHelper = new DBHelper();

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Log.d(Constants.CUSTOM_LOG_TYPE, "onsensorChanged called");
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long time = sensorEvent.timestamp;

            Log.d(Constants.CUSTOM_LOG_TYPE,"sensor values->" + x + " , " + y + " ," + z + " timestamp->" +time );
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch(InterruptedException ex){
                Log.d(Constants.CUSTOM_LOG_TYPE, "interrupted exception->" +ex.getMessage());
                ex.printStackTrace();
            }
            //save this value in DB
            dbHelper.insert(x,y,z,time);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onHandleIntent(Intent intent) {

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(Constants.CUSTOM_LOG_TYPE, "ON handle intent");
    }
}
