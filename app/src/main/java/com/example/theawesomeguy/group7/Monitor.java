package com.example.theawesomeguy.group7;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.Random;
import com.example.theawesomeguy.group7.Constants;


public class Monitor extends AppCompatActivity {

    int running_state=Constants.RUNNING_STATE_OFF;
    private LineGraphSeries<DataPoint> series;
    private static final Random generator = new Random();
    int lastX = 0;
    Thread produce;
    private GraphView graph;
    DBHelper dbHelper;

    EditText ageField = null;
    EditText idField = null;
    EditText nameField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        /*Own code*/
        /*UI component classes*/
        Button runBtn = (Button) findViewById(R.id.Run);
        Button stopBtn = (Button) findViewById(R.id.Stop);
        RadioButton maleRadioBtn = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton femaleRadioBtn = (RadioButton) findViewById(R.id.radioButton3);
        nameField = (EditText) findViewById(R.id.editText2);
        idField = (EditText) findViewById(R.id.editText4);
        ageField = (EditText) findViewById(R.id.age);

        /*Own code*/
        /*DB set up code*/
        dbHelper = new DBHelper();

        /*graph set up code*/
        /*own code*/
        graph = (GraphView) findViewById(R.id.graph);
        Viewport viewPort = graph.getViewport();
        viewPort.setXAxisBoundsManual(true);
        viewPort.setMinX(Constants.MIN_X_VIEW);
        viewPort.setMaxX(Constants.MAN_X_VIEW);
        viewPort.setScrollable(true);

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time (in seconds)");
        gridLabel.setVerticalAxisTitle("Sensor value (in units)");
        gridLabel.setVerticalAxisTitleTextSize(Constants.VER_TITLE_TEXT_SIZE);
        gridLabel.setHorizontalAxisTitleTextSize(Constants.HOR_TITLE_TEXT_SIZE);
        gridLabel.setTextSize(Constants.AXIS_TITLE_TEXT_SIZE);
        series = new LineGraphSeries<DataPoint>();


        /*listener for run button*/
        /*own code*/
        runBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/

                /*start new series with each run click. so it can be started from 0*/
                graph.removeSeries(series);
                series = new LineGraphSeries<DataPoint>();
                lastX = 0;
                graph.addSeries(series);

                if (running_state ==Constants.RUNNING_STATE_OFF){
                    Log.d("THREAD", "run onclick listener called...");
                    running_state = Constants.RUNNING_STATE_ON;


                }else{
                    running_state = Constants.RUNNING_STATE_ON;
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
               if(running_state==Constants.RUNNING_STATE_ON){
                   Log.d("THREAD", "stop onclick listener called...");
                   running_state = Constants.RUNNING_STATE_OFF;
                   Log.d("THREAD", "thread about to be interrupted...");
                   //lastX=0;
                   //series = null;
                   graph.removeSeries(series);
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
                    //check for texts in id, name and sex fields
                    Log.d("LOGGING", "focus lost called in name field");
                    if (areFieldsNotEmpty()){
                        //create table

                        String age = ageField.getText().toString();
                        String id = idField.getText().toString();
                        String name = nameField.getText().toString();

                        String table_name = age + Constants.DELIMITER + id + Constants.DELIMITER + name;
                        dbHelper.createTable(table_name);
                    }
                }
            }
        });

        ageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //check for texts in id, name and sex fields
                    Log.d("LOGGING", "focus lost called in age field");
                    if (areFieldsNotEmpty()){
                        //create table
                        dbHelper.createTable("TABLE NAME");
                    }
                }
            }
        });

        idField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //check for texts in id, name and sex fields
                    Log.d("LOGGING", "focus lost called in id field");
                    if (areFieldsNotEmpty()){
                        //create table
                        dbHelper.createTable("TABLE NAME");
                    }
                }
            }
        });

    }

    /*code snippet to check if the age, name, sex and id fields are empty or not*/
    private boolean areFieldsNotEmpty(){
        return (ageField!=null && !ageField.getText().toString().isEmpty() &&
                idField!=null && !idField.getText().toString().isEmpty() &&
                nameField!=null && !nameField.getText().toString().isEmpty());
    }

    /*Code snippet from a tutorial for plotting graph*/
    private void addEntry(){
        Log.d("STATE", "add entry called...");
        series.appendData(new DataPoint(lastX++, generator.nextDouble()), true, 10);
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
                            Thread.sleep(200);
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