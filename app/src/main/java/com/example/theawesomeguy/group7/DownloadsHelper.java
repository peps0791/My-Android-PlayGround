package com.example.theawesomeguy.group7;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

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

        UploadsHelper.getInstance().trustEveryone();
        try {

            Log.d(Constants.CUSTOM_LOG_TYPE, "Downloading ::" + Constants.UPLOAD_SERVER_FOLDER + Constants.DBNAME);
            ReadableByteChannel in = Channels.newChannel(
                    new URL(Constants.UPLOAD_SERVER_FOLDER + Constants.DBNAME).openStream());

            String downloadPath = Environment.getExternalStorageDirectory() + File.separator + Constants.DB_DIRECTORY_NAME_DOWNLOAD;
            File downloadPathDir = new File(downloadPath);
            if (downloadPathDir.exists() && downloadPathDir.isDirectory()) {
                Log.d(Constants.CUSTOM_LOG_TYPE, "db directory already exists");
            } else {
                Log.d(Constants.CUSTOM_LOG_TYPE, "Creating download DB directory");
                boolean dirCreated = downloadPathDir.mkdirs();

                Log.d(Constants.CUSTOM_LOG_TYPE, "is directory created ?" + dirCreated);
                if (!dirCreated) {
                    throw new Exception("Cant write to the storage. check!!!");
                }
            }

            Log.d(Constants.CUSTOM_LOG_TYPE, "download file path->" + downloadPath + File.separator + Constants.DBNAME);
            FileChannel out = new FileOutputStream(
                    downloadPath + File.separator + Constants.DBNAME).getChannel();
            long size = (new File(Environment.getExternalStorageDirectory() + File.separator + Constants.DB_DIRECTORY_NAME + File.separator + Constants.DBNAME)).length();
            out.transferFrom(in, 0, size);
            File newFile = new File(downloadPath + File.separator + Constants.DBNAME);
            if(newFile.exists()){
                Log.d(Constants.CUSTOM_LOG_TYPE, "downloaded file size->" +newFile.length());
            }


        }catch(MalformedURLException ex){
            ex.printStackTrace();
        }catch(IOException ex){
            ex.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
}
