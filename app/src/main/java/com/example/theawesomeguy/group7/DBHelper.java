package com.example.theawesomeguy.group7;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by peps on 6/17/17.
 */

public class DBHelper {

    String sdCardPath = null;
    String dbDir = null;
    File dbDirFilePath = null;
    SQLiteDatabase db;

    public DBHelper(){
        //default constructor

        //TODO: make this a singleton
        try{
            //create directory CSE535_ASSIGNMENT2 if it doesn't exist already
            sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d("LOGGING","sdcard path-->" +sdCardPath);

            dbDir = Constants.DB_DIRECTORY_NAME;
            Log.d("LOGGING","db Directory-->" +dbDir);

            String dbPath = sdCardPath + File.separator + dbDir;
            Log.d("LOGGING","db Directory-->" +dbDir);

            dbDirFilePath = new File(dbPath);
            if (dbDirFilePath.exists() && dbDirFilePath.isDirectory()) {
                Log.d("LOGGING","db directory already exists");
            }else{
                Log.d("LOGGING", "Creating  DB directory");
                dbDirFilePath.mkdirs();
            }
            db = SQLiteDatabase.openOrCreateDatabase(dbPath + File.separator + Constants.DBNAME, null);
            Log.d("LOGGING", "DB created successfully");
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void createTable(String tableName){
        Log.d("LOGGING", "createTable called with table name -->" +tableName);

        db.beginTransaction();

        try {
            //perform your database operations here ...
            db.execSQL("create table tblPat ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " name text, "
                    + " age text ); " );

            db.setTransactionSuccessful(); //commit your changes
        }
        catch (SQLiteException e) {
            //report problem
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            Log.d("LOGGING", "Table created successfully!");
        }

    }
}
