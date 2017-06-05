package com.example.theawesomeguy.group7;

import android.content.Context;
import android.graphics.Canvas;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
//import com.example.theawesomeguy.group7.GraphView;

import java.util.Random;

import static com.example.theawesomeguy.group7.R.id.graph;

public class Monitor extends AppCompatActivity {

    int running_state=0;
    private LineGraphSeries<DataPoint> series;
    private static final Random generator = new Random();
    int lastX = 0;
    Thread produce;
    private GraphView graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Button run = (Button) findViewById(R.id.Run);
        Button stop = (Button) findViewById(R.id.Stop);
        RadioButton male = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton female = (RadioButton) findViewById(R.id.radioButton3);
        EditText name = (EditText) findViewById(R.id.editText2);
        final EditText id = (EditText) findViewById(R.id.editText4);
        EditText age = (EditText) findViewById(R.id.age);

        graph = (GraphView) findViewById(R.id.graph);
        Viewport viewPort = graph.getViewport();
        viewPort.setXAxisBoundsManual(true);
        viewPort.setMinX(0);
        viewPort.setMaxX(5);
        viewPort.setScrollable(true);


        series = new LineGraphSeries<DataPoint>();
        //graph.addSeries(series);

        run.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/

                /*start new series with each run click. so it can be started from 0*/

                graph.removeSeries(series);
                series = new LineGraphSeries<DataPoint>();
                lastX = 0;
                graph.addSeries(series);


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
            }
        });


        stop.setOnClickListener( new View.OnClickListener() {

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
                   graph.removeSeries(series);
                   produce.interrupt();

                }
            }
        });
    }

    private void addEntry(){

        Log.d("STATE", "add entry called...");
        series.appendData(new DataPoint(lastX++, generator.nextDouble()), true, 10);
    }

    @Override
    protected void onResume() {

        super.onResume();
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
                            //graph.removeAllSeries();
                            //graph.removeSeries(series);
                        }
                    }
                }catch(Exception ex){
                    Log.d("THREAD", "thread interrupted!!-->" + ex.getMessage());
                }
            }
        });
    }

}