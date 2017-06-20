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
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
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


    public void uploadFile1() {
        URLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        String fileName = this.uploadFile.substring(this.uploadFile.lastIndexOf('/') +1);
        trustEveryone();
        try {
            URL url = new URL(this.upLoadServerUri);
            System.out.println("url:" + url);
            conn = url.openConnection();
            conn.setDoOutput(true);

            String postData = "";

            System.out.println("upload file ->" +this.uploadFile);
            conn = url.openConnection();

            InputStream imgIs = new FileInputStream(this.uploadFile);
            //InputStream imgIs = getClass().getResourceAsStream(this.uploadFile);
            byte[] imgData = new byte[imgIs.available()];
            imgIs.read(imgData);

            String message1 = "";
            message1 += "-----------------------------4664151417711" + CrLf;
            message1 += "Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + CrLf;
            message1 += "Content-Type : multipart/form-data" + CrLf;
            message1 += CrLf;

            // the image is sent between the messages in the multipart message.

            String message2 = "";
            message2 += CrLf + "-----------------------------4664151417711--"
                    + CrLf;

            conn.setRequestProperty("method", "post");

            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=---------------------------4664151417711");
            // might not need to specify the content-length when sending chunked
            // data.
            conn.setRequestProperty("Content-Length", String.valueOf((message1
                    .length() + message2.length() + imgData.length)));

            System.out.println("open os");
            os = conn.getOutputStream();

            System.out.println(message1);
            os.write(message1.getBytes());

            // SEND THE IMAGE
            int index = 0;
            int size = 1024;
            do {
                System.out.println("write:" + index);
                if ((index + size) > imgData.length) {
                    size = imgData.length - index;
                }
                os.write(imgData, index, size);
                index += size;
            } while (index < imgData.length);
            System.out.println("written:" + index);

            System.out.println(message2);
            os.write(message2.getBytes());
            os.flush();

            System.out.println("open is");
            is = conn.getInputStream();

            char buff = 512;
            int len;
            byte[] data = new byte[buff];
            do {
                System.out.println("READ");
                len = is.read(data);

                if (len > 0) {
                    System.out.println(new String(data, 0, len));
                }
            } while (len > 0);

            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Close connection");
            try {
                os.close();
            } catch (Exception e) {
            }
            try {
                is.close();
            } catch (Exception e) {
            }
            try {

            } catch (Exception e) {
            }
        }
    }


    public int uploadFile() {

        trustEveryone();
        String fileName = this.uploadFile.substring(this.uploadFile.lastIndexOf('/') +1);

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
