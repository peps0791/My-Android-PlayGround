package com.example.theawesomeguy.group7;

import android.content.Context;
import android.graphics.Canvas;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class Monitor extends AppCompatActivity {

    int running_state=0;
    private LineGraphSeries<DataPoint> series;
    private static final Random generator = new Random();
    private int lastX = 0;
    Thread produce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Switch run = (Switch) findViewById(R.id.R);
        RadioButton male = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton female = (RadioButton) findViewById(R.id.radioButton3);
        EditText name = (EditText) findViewById(R.id.editText2);
        final EditText id = (EditText) findViewById(R.id.editText4);
        EditText age = (EditText) findViewById(R.id.editText5);

        final GraphView graph = (GraphView) findViewById(R.id.graph);
        Viewport viewPort = graph.getViewport();
        viewPort.setYAxisBoundsManual(true);
        viewPort.setMinX(0);
        viewPort.setMaxY(1);
        viewPort.setScrollable(true);


        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        //graph.addSeries(series);


        run.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/
                if (running_state ==0){
                    id.setText("Running...");
                    running_state = 1;
                    produce.start();

                }else{
                    id.setText("Stopping...");
                    running_state = 0;
                    produce.stop();
                }
            }
        });

    }


    private void addEntry(){
        series.appendData(new DataPoint(lastX++, generator.nextDouble()), true, 10);
    }

    @Override
    protected void onResume() {
        super.onResume();

        produce = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<100;i++){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    try {
                        Thread.sleep(600);
                    }catch(Exception ex){
                        System.out.println("exception!!" + ex.getMessage());
                    }
                }
            }
        });
    }

    private double[] generateRandomTuple(){
        double[] x_y = new double[2];
        Random generator = new Random();
        x_y[0] = generator.nextDouble();
        x_y[1] = generator.nextDouble();
        return x_y;
    }
}