package com.example.theawesomeguy.group7;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
//import com.example.theawesomeguy.group7.GraphView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.example.theawesomeguy.group7.R.id.age;
import static com.example.theawesomeguy.group7.R.id.graph;

public class Monitor extends AppCompatActivity {

    int running_state=0;
    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    private static final Random generator = new Random();
    int lastX = 0;
    Thread produce;
    private GraphView graph;
    File outDir;
    SQLiteDatabase myDB;


    Button runBtn;
    Button stopBtn ;
    RadioButton maleRadioBtn;
    RadioButton femaleRadioBtn ;
    Button uploadBtn;
    Button downloadBtn;
    EditText nameField;
    EditText idField ;
    EditText ageField;
    RadioGroup gender;


    private SensorManager mSensorManager;
    private Sensor mSensor;
    String tableName;

    DBHelper dbHelper;
    UploadsHelper uploadsHelper;
    DownloadsHelper downloadsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        /*Own code*/
        /*UI component classes*/
        Button runBtn = (Button) findViewById(R.id.Run);
        Button stopBtn = (Button) findViewById(R.id.Stop);
        uploadBtn =  (Button) findViewById(R.id.Upload);
        downloadBtn =  (Button) findViewById(R.id.Download);
        RadioButton maleRadioBtn = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton femaleRadioBtn = (RadioButton) findViewById(R.id.radioButton3);
        nameField = (EditText) findViewById(R.id.editText2);
        idField = (EditText) findViewById(R.id.editText4);
        ageField = (EditText) findViewById(R.id.age);
        gender = (RadioGroup) findViewById(R.id.radioGroup);

        /*graph set up code*/
        /*own code*/
        graph = (GraphView) findViewById(R.id.graph);
        Viewport viewPort = graph.getViewport();
        viewPort.setXAxisBoundsManual(true);
        viewPort.setMinX(0);
        viewPort.setMaxX(5);
        viewPort.setScrollable(true);

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time (in seconds)");
        gridLabel.setVerticalAxisTitle("Sensor value (in units)");
        gridLabel.setVerticalAxisTitleTextSize(35.0f);
        gridLabel.setHorizontalAxisTitleTextSize(35.0f);
        gridLabel.setTextSize(25.0f);
        series1 = new LineGraphSeries<DataPoint>();
        series2 = new LineGraphSeries<DataPoint>();
        series3 = new LineGraphSeries<DataPoint>();

        /**
         * TODO
         *
         * TODO Write Logic for creating a DB at a specific location -- still location part pending  -> DONE
         * TODO upload
         * TODO Download
         * Write logic to see if all entries are filled -- done
         * Write logic to create table when above condition satisifies
         * Write logic to stop the listener once the table has been created to avoid bugs
         * Write a service to get Acc data at certain frequencies
         * Write logic to pull last 10 sec data and plot it
         * Update above to read every second  -- Done by original series listener
         */
        dbHelper = DBHelper.getInstance();

        downloadsHelper = DownloadsHelper.getInstance();
        uploadsHelper = UploadsHelper.getInstance();

        uploadsHelper.setUploadServerURI(Constants.UPLOAD_SERVER_URI);
        uploadsHelper.setUploadFile(Environment.getExternalStorageDirectory() +
                File.separator + Constants.DB_DIRECTORY_NAME + File.separator + Constants.DBNAME);
        //Step 2, Listener on gender and check if all entries have been filled

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                if( nameField.getText()!=null && ageField.getText()!= null && idField.getText()!=null){

                    //gender has been selected, do your thing
                    String maleOrFemale = "";
                    if (gender.getCheckedRadioButtonId()==0){
                        maleOrFemale="f";
                    }
                    else {
                        maleOrFemale="m";
                    }

                    tableName = nameField.getText().toString()+"_"+idField.getText().toString()+"_"+ageField.getText().toString()+"_"+maleOrFemale;
                    Log.v("Done", tableName);
                    dbHelper.createTableWhenConditionsMet(tableName);

                    //Step 3, Create Table, Stop Validation Listener

                    gender.setOnCheckedChangeListener(null);

                    //Step 4, Service to store Data at 1GHZ
                    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                    mSensorManager.registerListener(new SensorEventListener() {

                        // Use the accelerometer.
                        @Override
                        public void onSensorChanged(SensorEvent event) {


                            //Create the thread with 1 second pause
                            float x = event.values[0];
                            float y = event.values[1];
                            float z = event.values[2];

                            dbHelper.insertInTable(x,y,z);
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {

                            //Do nothing
                            //Log.d("Sensor changed", name.getText().toString());

                        }
                    }, mSensor, 1000000);

                }

            }
        });


        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Monitor.this,  "Uploading file...", Toast.LENGTH_LONG).show();


                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(Constants.CUSTOM_LOG_TYPE, "Upload started");
                            }
                        });

                        downloadsHelper.download();

                        //need to switch the database


                    }
                }).start();
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Monitor.this,  "Uploading file...", Toast.LENGTH_LONG).show();


                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(Constants.CUSTOM_LOG_TYPE, "Upload started");
                            }
                        });

                        uploadsHelper.uploadFile();

                    }
                }).start();
            }
        });


        //Step 5, Pull Info

        //Step 6, refresh Info

        /*listener for run button*/
        /*own code*/
        runBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/

                /*start new series with each run click. so it can be started from 0*/

                // TODO here create three series elements, each for X,Y, Z to plot on the same graph. Look at logic for same.
                graph.removeSeries(series1);
                graph.removeSeries(series2);
                graph.removeSeries(series3);
                series1 = new LineGraphSeries<DataPoint>();
                series2 = new LineGraphSeries<DataPoint>();
                series3 = new LineGraphSeries<DataPoint>();
                lastX = 0;
                graph.addSeries(series1);
                graph.addSeries(series2);
                graph.addSeries(series3);

                if (running_state ==0){
                    Log.d("THREAD", "run onclick listener called...");
                    running_state = 1;


                }else{
                    running_state = 1;
                    //produce.stop();
                    Log.d("THREAD", "run onclick listener called again called...");
                    produce.interrupt();
                }

                //produce.start();
                /*start thread only if not in RUNNING state already*/
                Log.d("THREAD", "produce thread get state-->"+ produce.getState());
                if (produce.getState() == Thread.State.NEW ){
                    Log.d("THREAD", "Starting new thread");
                    produce.start();
                }
                Snackbar.make(findViewById(android.R.id.content), "Plotting Graph.", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
            }
        });


        /*listener for stop button*/
        /*own code*/
        stopBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/
                if(running_state==1){
                    Log.d("THREAD", "stop onclick listener called...");
                    running_state = 0;
                    Log.d("THREAD", "thread about to be interrupted...");
                    //lastX=0;
                    //series = null;
                    graph.removeSeries(series1);
                    graph.removeSeries(series2);
                    graph.removeSeries(series3);
                    produce.interrupt();
                    Snackbar.make(findViewById(android.R.id.content), "Graph plotting stopped.", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();

                }
            }
        });


        nameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //check if the name is a text and not numeric
                    if(!Misc.isFieldValid(nameField, Constants.NAME_TYPE)){
                        Toast.makeText(Monitor.this, Constants.INVALID_NAME__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if(areFieldsNotEmpty() && areFieldsValid()) {
                        //createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        //startAccService();
                    }else{
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }
                }
            }
        });

        ageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //check for texts in id, name and sex fields
                    if(!Misc.isFieldValid(ageField, Constants.AGE_TYPE)){
                        Toast.makeText(Monitor.this, Constants.INVALID_AGE__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if(areFieldsNotEmpty() && areFieldsValid()) {
                        //createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        //startAccService();
                    }else{
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }

                }
            }
        });

        idField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //check for texts in id, name and sex fields
                    if(!Misc.isFieldValid(idField, Constants.ID_TYPE)){
                        Toast.makeText(Monitor.this, Constants.INVALID_ID__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if(areFieldsNotEmpty() && areFieldsValid()) {
                        //createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        //startAccService();
                    }else{
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }

                }
            }
        });
    }


    private boolean areFieldsValid(){
        return( Misc.isFieldValid(nameField, Constants.NAME_TYPE) &&
                Misc.isFieldValid(ageField, Constants.AGE_TYPE) &&
                Misc.isFieldValid(idField, Constants.ID_TYPE));
    }


    /*code snippet to check if the age, name, sex and id fields are empty or not*/
    private boolean areFieldsNotEmpty(){
        return (ageField!=null && !ageField.getText().toString().isEmpty() &&
                idField!=null && !idField.getText().toString().isEmpty() &&
                nameField!=null && !nameField.getText().toString().isEmpty() &&
                gender.getCheckedRadioButtonId()!=-1);
    }

    /*private void createTable(){

        String age = ageField.getText().toString();
        String id = idField.getText().toString();
        String name = nameField.getText().toString();

        String table_name = name + Constants.DELIMITER + id + Constants.DELIMITER  +age;

        dbHelper.createTable(table_name);

    }*/

    /*Code snippet from a tutorial for plotting graph*/
    private void addEntry(){


        // get time right now

        String datetimeTimeStamp;
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        datetimeTimeStamp = dateFormat.format(date);

        Date d1 = null;
        Date d2 = null;

        //Cursor c = myDB.rawQuery("SELECT * FROM "+ tableName , null);
        Cursor c = dbHelper.fetchData();
        if(c.moveToFirst()){
            do{
                String datetime = c.getString(0);

                try{
                    d1= dateFormat.parse(datetime); //from DB
                    d2= dateFormat.parse(datetimeTimeStamp); //current date
                }
                catch (Exception e){
                    Log.e("Date Time formatting", datetime);
                }
                if((d2.getTime() - d1.getTime())/1000 % 60 <=10)
                    break;

            }while (c.moveToNext());
            Log.d("DB Entry", c.getString(0) + c.getString(1)+c.getString(2)+c.getString(3));
        }

        series1.appendData(new DataPoint(lastX++, Double.parseDouble(c.getString(1)) ), true, 10);
        series2.appendData(new DataPoint(lastX++, Double.parseDouble(c.getString(2))), true, 10);
        series3.appendData(new DataPoint(lastX++, Double.parseDouble(c.getString(3))), true, 10);
    }

    @Override
    protected void onResume() {

        super.onResume();

        /*code snippet from a tutorial for plotting graph*/
        produce = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0;; i++) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addEntry();
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Log.d("THREAD", "thread interrupted from sleep!!-->" + ex.getMessage());
                        }
                    }
                }catch(Exception ex){
                    Log.d("THREAD", "Exception!-->" + ex.getMessage());
                }
            }
        });
    }

}