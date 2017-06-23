package com.example.theawesomeguy.group7;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
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

public class DownloadsHelper {

    private static DownloadsHelper downloadsHelper = null;

    private DownloadsHelper(){
        //private default constructor
    }

    public static DownloadsHelper getInstance(){
        if (downloadsHelper == null){
            downloadsHelper = new DownloadsHelper();
        }
        return downloadsHelper;
    }

    public void download(){

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
            URL url = new URL(Constants.UPLOAD_SERVER_FOLDER + File.separator + Constants.DBNAME);
            Log.d("LOGGING", "url->" + url);
            connection = (HttpURLConnection) url.openConnection();

            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            Log.d("", " Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            //downloadButton.setText(Integer.toString(fileLength));
            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD +
                    File.separator + Constants.DBNAME);
            //downloadButton.setText("Connecting .....");
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button

                total += count;
                // publishing the progress....

                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    }
}
