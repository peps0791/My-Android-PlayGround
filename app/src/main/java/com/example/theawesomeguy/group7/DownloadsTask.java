package com.example.theawesomeguy.group7;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by peps on 6/19/17.
 */

public class DownloadsTask extends AsyncTask<String, Integer, String>{


    private Context context;
    private PowerManager.WakeLock mWakeLock;

    public DownloadsTask(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(String... sUrl) {
        //searchButton = (Button) findViewById(R.id.button1);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
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
        } };

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

            Log.d(Constants.CUSTOM_LOG_TYPE, "server code->" +connection.getResponseCode() + " " + connection.getResponseMessage());


            dirCleanUp();

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            Log.d(Constants.CUSTOM_LOG_TYPE, "fileLength->" +fileLength);
            //downloadButton.setText(Integer.toString(fileLength));
            // download the file
            input = connection.getInputStream();
            String downloadPath = Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD + File.separator +
                    Constants.DBNAME;
            Log.d(Constants.CUSTOM_LOG_TYPE, "download path->" +downloadPath);
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

    void dirCleanUp(){

        File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD);
        for(File file: dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

}
