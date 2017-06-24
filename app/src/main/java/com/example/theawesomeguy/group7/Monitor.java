package com.example.theawesomeguy.group7;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.theawesomeguy.group7.R.id.age;
import static com.example.theawesomeguy.group7.R.id.graph;
import static com.example.theawesomeguy.group7.R.id.time;

public class Monitor extends AppCompatActivity implements SensorEventListener {

    int running_state = 0;
    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    private static final Random generator = new Random();
    int lastX = 0;
    Thread produce;
    private GraphView graph;
    File outDir;
    SQLiteDatabase myDB;
    int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 10;
    String maleOrFemale;
    int isDBSwitched = 0;
    int lastTimestamp = 0;


    Button runBtn;
    Button stopBtn;
    RadioButton maleRadioBtn;
    RadioButton femaleRadioBtn;
    Button uploadBtn;
    Button downloadBtn;
    EditText nameField;
    EditText idField;
    EditText ageField;
    RadioGroup gender;

    int lastTimedigit = -1;


    private SensorManager mSensorManager;
    private Sensor mSensor;
    String tableName;

    DBHelper dbHelper;
    UploadsHelper uploadsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Monitor.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Monitor.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showMessageOKCancel("You need to allow access to External Storage",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                            }
                        });

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(Monitor.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        /*Own code*/
        /*UI component classes*/
        Button runBtn = (Button) findViewById(R.id.Run);
        Button stopBtn = (Button) findViewById(R.id.Stop);
        uploadBtn = (Button) findViewById(R.id.Upload);
        downloadBtn = (Button) findViewById(R.id.Download);
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


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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

        //clear directory
        //dirCleanUp();


        dbHelper = DBHelper.getInstance(Monitor.this);
        uploadsHelper = UploadsHelper.getInstance();

        uploadsHelper.setUploadServerURI(Constants.UPLOAD_SERVER_URI);
        uploadsHelper.setUploadFile(Environment.getExternalStorageDirectory() +
                File.separator + Constants.DB_DIRECTORY_NAME + File.separator + Constants.DBNAME);
        //Step 2, Listener on gender and check if all entries have been filled

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                if (nameField.getText() != null && ageField.getText() != null && idField.getText() != null) {

                    //gender has been selected, do your thing
                    if (gender.getCheckedRadioButtonId() == 0) {
                        maleOrFemale = "f";
                    } else {
                        maleOrFemale = "m";
                    }


                    if (areFieldsNotEmpty() && areFieldsValid()) {
                        //Step 3, Create Table, Stop Validation Listener
                        createTable();
                        gender.setOnCheckedChangeListener(null);
                        //Step 4, Service to store Data at 1GHZ
                        startAccService();
                    }
                }

            }
        });


        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Monitor.this, "Downloading file...", Toast.LENGTH_LONG).show();

                final DownloadsTask downloadTask2 = new DownloadsTask(Monitor.this);
                //downloadTask2.execute("http://10.143.3.163/uploads/group9","group9");
                try {
                    downloadTask2.execute(Constants.UPLOAD_SERVER_FOLDER + File.separator + Constants.DBNAME, Constants.DBNAME);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Monitor.this, "Uploading file...", Toast.LENGTH_LONG).show();


                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(Constants.CUSTOM_LOG_TYPE, "Upload started");
                            }
                        });

                        uploadsHelper.uploadFile();
                        mSensorManager.unregisterListener(Monitor.this);

                        dbHelper.closeDB();
                        try {
                            dbHelper.finalize();
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                        dbHelper.setTableName();
                        lastTimestamp = 0;


                    }
                }).start();
            }
        });


        //Step 5, Pull Info

        //Step 6, refresh Info

        /*listener for run button*/
        /*own code*/
        runBtn.setOnClickListener(new View.OnClickListener() {

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

                if (running_state == 0) {
                    Log.d(Constants.CUSTOM_LOG_TYPE, "run onclick listener called...");
                    running_state = 1;


                } else {
                    running_state = 1;
                    //produce.stop();
                    Log.d(Constants.CUSTOM_LOG_TYPE, "run onclick listener called again called...");
                    produce.interrupt();
                }

                //produce.start();
                /*start thread only if not in RUNNING state already*/
                Log.d(Constants.CUSTOM_LOG_TYPE, "produce thread get state-->" + produce.getState());

                dbHelper.setTableName();
                if (produce.getState() == Thread.State.NEW) {

                    if (dbHelper.isTableSet()) {
                        Log.d(Constants.CUSTOM_LOG_TYPE, "Starting new thread");
                        produce.start();

                        Snackbar.make(findViewById(android.R.id.content), "Plotting Graph.", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Cant plot graph.", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                    }

                }

            }
        });


        /*listener for stop button*/
        /*own code*/
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /***Do what you want with the click here***/
                if (running_state == 1) {
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


                /*mSensorManager.unregisterListener(Monitor.this);

                dbHelper.closeDB();
                try{
                    dbHelper.finalize();
                }catch(Throwable ex){
                    ex.printStackTrace();
                }

                dbHelper.setTableName();*/


            }
        });


        nameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //check if the name is a text and not numeric
                    if (!Misc.isFieldValid(nameField, Constants.NAME_TYPE)) {
                        Toast.makeText(Monitor.this, Constants.INVALID_NAME__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if (areFieldsNotEmpty() && areFieldsValid()) {
                        createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        startAccService();
                    } else {
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }
                }
            }
        });

        ageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //check for texts in id, name and sex fields
                    if (!Misc.isFieldValid(ageField, Constants.AGE_TYPE)) {
                        Toast.makeText(Monitor.this, Constants.INVALID_AGE__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if (areFieldsNotEmpty() && areFieldsValid()) {
                        createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        startAccService();
                    } else {
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }

                }
            }
        });

        idField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //check for texts in id, name and sex fields
                    if (!Misc.isFieldValid(idField, Constants.ID_TYPE)) {
                        Toast.makeText(Monitor.this, Constants.INVALID_ID__ERROR, Toast.LENGTH_LONG).show();
                    }

                    if (areFieldsNotEmpty() && areFieldsValid()) {
                        createTable();
                        Toast.makeText(Monitor.this, Constants.DATA_OK_START_ACCMTR_MSG, Toast.LENGTH_LONG).show();
                        startAccService();
                    } else {
                        Log.d(Constants.CUSTOM_LOG_TYPE, Constants.DATA_NOT_OK_MSG);
                    }

                }
            }
        });
    }


    private boolean areFieldsValid() {
        return (Misc.isFieldValid(nameField, Constants.NAME_TYPE) &&
                Misc.isFieldValid(ageField, Constants.AGE_TYPE) &&
                Misc.isFieldValid(idField, Constants.ID_TYPE));
    }


    /*code snippet to check if the age, name, sex and id fields are empty or not*/
    private boolean areFieldsNotEmpty() {
        return (ageField != null && !ageField.getText().toString().isEmpty() &&
                idField != null && !idField.getText().toString().isEmpty() &&
                nameField != null && !nameField.getText().toString().isEmpty() &&
                gender.getCheckedRadioButtonId() != -1);
    }

    private void createTable() {

        Log.d(Constants.CUSTOM_LOG_TYPE, "Creating DB table.");
        String age = ageField.getText().toString();
        String id = idField.getText().toString();
        String name = nameField.getText().toString();

        String table_name = name + Constants.DELIMITER + id + Constants.DELIMITER + age + Constants.DELIMITER + maleOrFemale;

        //dbHelper.createTable(table_name);
        dbHelper.createTableWhenConditionsMet(table_name);

    }

    void dirCleanUp() {

        /*File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.DB_DIRECTORY_NAME);
        for(File file: dir.listFiles())
            if (!file.isDirectory())
                file.delete();*/
    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        //Create the thread with 1 second pause
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long timestamp = event.timestamp;

        long timeInMillis = (new Date()).getTime()
                + (timestamp - System.nanoTime()) / 1000000L;

        //Log.d(Constants.CUSTOM_LOG_TYPE, "time in millis-->" + (int)timeInMillis/1000);
        timeInMillis /= 1000;
        String temp = String.valueOf((int) timeInMillis);
        String lastDigit = temp.substring(temp.length() - 1);
        if (Integer.parseInt(lastDigit) != lastTimedigit) {
            Log.d(Constants.CUSTOM_LOG_TYPE, "Inserting-->" + (int) timeInMillis);
            dbHelper.insertInTable(x, y, z, (int) timeInMillis);
        }
        lastTimedigit = Integer.parseInt(lastDigit);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        //Do nothing
        //Log.d("Sensor changed", name.getText().toString());

    }


    private void startAccService() {

        Log.d(Constants.CUSTOM_LOG_TYPE, "Starting Acclerometer service");
        mSensorManager.registerListener(Monitor.this, mSensor, Constants.SENSOR_1HZ_DELAY);

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Monitor.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /*Code snippet from a tutorial for plotting graph*/
    private void addEntry() {

        if (running_state == Constants.RUNNING_STATE_OFF) {
            return;
        }

        //Cursor c = dbHelper.fetchData();

        List<List<Float>> xyzList = null;
        Map<Integer, List<List<Float>>> map = null;
        if (lastTimestamp == 0) {
            map = dbHelper.fetchDataList(lastTimestamp, 10);
        } else {
            map = dbHelper.fetchDataList(lastTimestamp, 1);
        }

        for (Map.Entry<Integer, List<List<Float>>> entry : map.entrySet()) {
            lastTimestamp = entry.getKey();
            xyzList = entry.getValue();

        }

        for (int i = 0; i < xyzList.get(0).size(); i++) {
            series1.appendData(new DataPoint(lastX++, xyzList.get(0).get(i)), true, 10);
            series2.appendData(new DataPoint(lastX++, xyzList.get(1).get(i)), true, 10);
            series3.appendData(new DataPoint(lastX++, xyzList.get(2).get(i)), true, 10);
        }
    }


    private class DownloadsTask extends AsyncTask<String, Integer, String> {


        private Context context;
        //private PowerManager.WakeLock mWakeLock;

        public DownloadsTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            //searchButton = (Button) findViewById(R.id.button1);
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            }};

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                URL url = new URL(sUrl[0]);
                Log.d("LOGGING", "url->" + sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Log.d(Constants.CUSTOM_LOG_TYPE, "server code->" + connection.getResponseCode() + " " + connection.getResponseMessage());


                dirCleanUp();

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                Log.d(Constants.CUSTOM_LOG_TYPE, "fileLength->" + fileLength);
                //downloadButton.setText(Integer.toString(fileLength));
                // download the file
                input = connection.getInputStream();
                String downloadPath = Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD + File.separator +
                        Constants.DBNAME;
                Log.d(Constants.CUSTOM_LOG_TYPE, "download path->" + downloadPath);
                //output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/downloads/"+sUrl[1]);
                output = new FileOutputStream(downloadPath);
                //downloadButton.setText("Connecting .....");
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;


        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //make sure the download directory exists

            String downloadPathStr = Environment.getExternalStorageDirectory() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD;
            Log.d(Constants.CUSTOM_LOG_TYPE, "downloadPath-->" + downloadPathStr);
            File downloadPath = new File(downloadPathStr);

            if (downloadPath.exists() && downloadPath.isDirectory()) {
                Log.d(Constants.CUSTOM_LOG_TYPE, "db download directory already exists");
            } else {
                Log.d(Constants.CUSTOM_LOG_TYPE, "Creating  DB download directory");
                boolean dirCreated = downloadPath.mkdirs();

                Log.d(Constants.CUSTOM_LOG_TYPE, "is directory created ?" + dirCreated);
            }
        }

        @Override
        public void onPostExecute(String result) {
            //mWakeLock.release();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                Log.d(Constants.CUSTOM_LOG_TYPE, "Download Error:" + result);


            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                Log.d(Constants.CUSTOM_LOG_TYPE, "File downloaded");

                Log.d(Constants.CUSTOM_LOG_TYPE, "result" + result);
                dbHelper.switchToDownloadDB();
                isDBSwitched = 1;

                //plot graph
                //fetch 10 seconds data from db
                graph.removeAllSeries();
                series1 = new LineGraphSeries<>();
                series2 = new LineGraphSeries<>();
                series3 = new LineGraphSeries<>();




                lastX=0;
                Map<Integer, List<List<Float>>> map = null;
                List<List<Float>> xyzList = null;
                for (int i = 0; i < 10; i++) {
                    map = dbHelper.fetchDataList(lastTimestamp, 1);

                    for (Map.Entry<Integer, List<List<Float>>> entry : map.entrySet()) {
                        lastTimestamp = entry.getKey();
                        xyzList = entry.getValue();
                    }

                    Log.d(Constants.CUSTOM_LOG_TYPE, "list sizes->" +xyzList.get(0).size() +
                            " ,  " + xyzList.get(1).size() + " ," + xyzList.get(2).size() +
                    "value of i-->" + i);
                    series1.appendData(new DataPoint(lastX++, xyzList.get(0).get(0)), true, 10);
                    series2.appendData(new DataPoint(lastX++, xyzList.get(1).get(0)), true, 10);
                    series3.appendData(new DataPoint(lastX++, xyzList.get(2).get(0)), true, 10);


                }


                graph.addSeries(series1);
                graph.addSeries(series2);
                graph.addSeries(series3);

            }
        }

    }


        @Override
        protected void onResume() {

            super.onResume();

        /*code snippet from a tutorial for plotting graph*/
            produce = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; ; i++) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addEntry();
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Log.d(Constants.CUSTOM_LOG_TYPE, "thread interrupted from sleep!!-->" + ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        Log.d(Constants.CUSTOM_LOG_TYPE, "Exception!-->" + ex.getMessage());
                    }
                }
            });
        }


}