package com.example.theawesomeguy.group7;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Monitor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Switch run = (Switch) findViewById(R.id.R);
        RadioButton male = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton female = (RadioButton) findViewById(R.id.radioButton3);
        EditText name = (EditText) findViewById(R.id.editText2);
        EditText id = (EditText) findViewById(R.id.editText4);
        EditText age = (EditText) findViewById(R.id.editText5);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

}
