package com.example.theawesomeguy.group7;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by peps on 6/18/17.
 */

public class UploadsHelper {

    private static UploadsHelper uploadsHelper = null;

    private String upLoadServerUri = null;
    private String uploadFile = null;
    private int serverResponseCode = 0;
    private final String CrLf = "\r\n";

    public static UploadsHelper getInstance(){
        if (uploadsHelper==null){
            uploadsHelper = new UploadsHelper();
        }
        return uploadsHelper;
    }

    private UploadsHelper(){

    }

    public void setUploadServerURI(String uploadServerURI){
        this.upLoadServerUri = uploadServerURI;
    }

    public void setUploadFile(String uploadFile){
        this.uploadFile = uploadFile;
    }

    public void trustEveryone() {
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

    public int uploadFile() {

        //trustEveryone();
        try {

            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        }catch(Exception ex){
            ex.printStackTrace();
        }


        //String fileName = this.uploadFile.substring(this.uploadFile.lastIndexOf('/') +1);
        String fileName = this.uploadFile;
        Log.d(Constants.CUSTOM_LOG_TYPE, "File details : File name"
                + fileName + " File size -->" + new File(fileName).length());
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "***";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(this.uploadFile);

        if (!sourceFile.isFile()) {

            //dialog.dismiss();
            Log.d(Constants.CUSTOM_LOG_TYPE, "Source File not exist :"
                    + uploadFile);
            return 0;
        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                Log.d(Constants.CUSTOM_LOG_TYPE, "serverURi-->" +upLoadServerUri);
                URL url = new URL(upLoadServerUri);


                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);    //remote php server
                //conn.setRequestProperty("uploadedfile", fileName);   //local php server
                //conn.setRequestProperty("fileUploaded", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                //String tmp = "Content-Disposition: form-data ; name=\"fileUploaded\";filename=\"" + fileName + "\""+ lineEnd;
                String tmp = "Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\""+ lineEnd;
                //String tmp = "Content-Disposition: form-data ; name=\"uploadedfile\";filename=\"" + fileName + "\""+ lineEnd;
                Log.d(Constants.CUSTOM_LOG_TYPE, "tmp string ::" +tmp);
                dos.writeBytes(tmp);

                //dos.writeBytes("Content-Disposition: form-data ;name : fileUploaded" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    Log.d(Constants.CUSTOM_LOG_TYPE, "Bytes read ::" +bytesRead);

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
                Log.d(Constants.CUSTOM_LOG_TYPE, "Server response code : "
                        + serverResponseCode);
                String serverResponseMessage = conn.getResponseMessage();


                Log.d(Constants.CUSTOM_LOG_TYPE, "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    Log.d(Constants.CUSTOM_LOG_TYPE, "File Upload Complete");
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //dialog.dismiss();
                ex.printStackTrace();

                Log.d(Constants.CUSTOM_LOG_TYPE, "MalformedURLException Exception : check script url.");
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();
                Log.d(Constants.CUSTOM_LOG_TYPE, "Exception : check script url.");

            }
            //dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

    public static void main(String[] args){

        String str = "test1/test2/test3";
        System.out.println(str.substring(str.lastIndexOf('/')));

    }
}