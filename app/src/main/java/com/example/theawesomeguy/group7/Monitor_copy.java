package com.example.theawesomeguy.group7;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class Monitor_copy extends AppCompatActivity {

    int running_state=0;
    private LineGraphSeries<DataPoint> series;
    private static final Random generator = new Random();
    int lastX = 0;
    Thread produce;
    private GraphView graph;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = "https://impact.asu.edu/CSE535Spring17Folder/UploadToServer.php";
    String uploadFilePath = "storage/sdcard/";
    String uploadFileName = "scr.png";
    TextView messageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        /*Own code*/
        /*UI component classes*/
        Button run = (Button) findViewById(R.id.Run);
        Button stop = (Button) findViewById(R.id.Stop);
        Button upload = (Button) findViewById(R.id.Upload);
        RadioButton male = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton female = (RadioButton) findViewById(R.id.radioButton3);
        EditText name = (EditText) findViewById(R.id.editText2);
        final EditText id = (EditText) findViewById(R.id.editText4);
        EditText age = (EditText) findViewById(R.id.age);
        messageText  = (TextView)findViewById(R.id.messageText);
        messageText.setText("Uploading file path :- '/mnt/sdcard/"+uploadFileName+"'");


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
        series = new LineGraphSeries<DataPoint>();


        /*listener for run button*/
        /*own code*/
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
                Snackbar.make(findViewById(android.R.id.content), "Plotting Graph.", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
            }
        });


        /*listener for stop button*/
        /*own code*/
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
                   Snackbar.make(findViewById(android.R.id.content), "Graph plotting stopped.", Snackbar.LENGTH_LONG)
                           .setActionTextColor(Color.RED)
                           .show();

                }
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(Monitor_copy.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });

                        uploadFile(uploadFilePath + "" + uploadFileName);

                    }
                }).start();
            }
        });


    }

    /*Code snippet from a tutorial for plotting graph*/
    private void addEntry(){
        Log.d("STATE", "add entry called...");
        series.appendData(new DataPoint(lastX++, generator.nextDouble()), true, 10);
    }


    public int uploadFile(String sourceFileUri) {

        trustEveryone();
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + uploadFileName);

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :"
                            +uploadFilePath + "" + uploadFileName);
                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);


                // Open a HTTP  connection to  the URL
                conn = (HttpsURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data ; name=\"uploaded_file\";filename=\"\"\n" +
                        "                 //               + fileName + \"\"" + lineEnd);


                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n";

                            messageText.setText(msg);
                            Toast.makeText(Monitor_copy.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(Monitor_copy.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(Monitor_copy.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);

            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
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

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }


}