package com.example.theawesomeguy.group7;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by peps on 6/17/17.
 */

public class DBHelper {

    String sdCardPath = null;
    String dbDir = null;
    File dbDirFilePath = null;
    private static SQLiteDatabase db;

    private static String tableName = null;

    private static DBHelper dbHelper = null;


    public static DBHelper getInstance(){
        if (dbHelper==null){
            dbHelper = new DBHelper();
        }
        return dbHelper;
    }


    public void switchToDownloadDB(){


        //download location
        String downloadLoc = Environment.getExternalStorageDirectory() + File.separator +
                Constants.DB_DIRECTORY_NAME_DOWNLOAD + File.separator + Constants.DBNAME;

        db = SQLiteDatabase.openOrCreateDatabase(downloadLoc, null);


    }
    private DBHelper() {
        //default constructor
        try {

            Log.d(Constants.CUSTOM_LOG_TYPE, "External Storage state ->" + isExternalStorageWritable());
            //create directory CSE535_ASSIGNMENT2 if it doesn't exist already
            sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(Constants.CUSTOM_LOG_TYPE, "sdcard path-->" + sdCardPath);

            dbDir = Constants.DB_DIRECTORY_NAME;
            Log.d(Constants.CUSTOM_LOG_TYPE, "db Directory-->" + dbDir);

            String dbPath = sdCardPath + File.separator + dbDir;
            Log.d(Constants.CUSTOM_LOG_TYPE, "db Directory-->" + dbDir);

            dbDirFilePath = new File(dbPath);
            if (dbDirFilePath.exists() && dbDirFilePath.isDirectory()) {
                Log.d(Constants.CUSTOM_LOG_TYPE, "db directory already exists");
            } else {
                Log.d(Constants.CUSTOM_LOG_TYPE, "Creating  DB directory");
                boolean dirCreated = dbDirFilePath.mkdirs();

                Log.d(Constants.CUSTOM_LOG_TYPE, "is directory created ?" + dirCreated);
                if (!dirCreated) {
                    throw new Exception("Cant write to the storage. check!!!");
                }
            }

            db = SQLiteDatabase.openOrCreateDatabase(dbPath + File.separator + Constants.DBNAME, null);
            Log.d(Constants.CUSTOM_LOG_TYPE, "DB created successfully");

        } catch (Exception ex) {
            Log.d(Constants.CUSTOM_LOG_TYPE, "Exception while creating DB->" + ex.getMessage());
            ex.printStackTrace();
        }

    }


    public void createTableWhenConditionsMet(String name){

        try {

            db.beginTransaction();
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + name
                    + " (timestamp VARCHAR(10), x Long(10), y Long(10), z Long(10));");

            db.setTransactionSuccessful(); //commit your changes

            DBHelper.tableName = name;

        }
        catch (Exception e){
            Log.d("Table create issue", e.toString());
        }

    }


    public void insertInTable(float x, float y, float z){
        try{
            String datetimeTimeStamp;
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            datetimeTimeStamp = dateFormat.format(date);
            String query="INSERT INTO "+ DBHelper.tableName+"(timestamp, x, y, z) VALUES( '"+datetimeTimeStamp+"',"+String.valueOf(x)+","+String.valueOf(y)+","+String.valueOf(z)+")";
            Log.v("Query ", query);
            db.execSQL(query);
        }
        catch (Exception e){
            Log.v("Error in insert", e.toString());
        }
    }


    public void createTable(String tableName){
        //tableName = "testtable";
        Log.d(Constants.CUSTOM_LOG_TYPE, "createTable called with table name -->" +tableName);

        try {
            db.beginTransaction();

            String query = "create table " +  tableName + " ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " x_val long, "
                    + " y_val long,"
                    + " z_val long,"
                    + " timestamp varchar(10)); ";

            Log.d(Constants.CUSTOM_LOG_TYPE, "QUERY->" +query);
            //perform your database operations here ...
            db.execSQL(query);

            db.setTransactionSuccessful(); //commit your changes
            DBHelper.tableName = tableName;
        }
        catch (SQLiteException e) {
            //report problem

            /*Some propmt ???*/
            Log.e("ERROR", "Some error");
            e.printStackTrace();

        }
        finally {
            try {
                db.endTransaction();
            }catch(Exception ex){
                Log.d(Constants.CUSTOM_LOG_TYPE, ex.getMessage());
            }

        }

    }

    public void insert(float x, float y, float z, long timestamp){

        try {
            //perform your database operations here ...
            ContentValues values = new ContentValues();
            values.put("x_val", x);
            values.put("y_val", y);
            values.put("z_val", z);
            values.put("timestamp", timestamp);

            db.insertOrThrow(DBHelper.tableName, null, values);
            //db.execSQL( "insert into " + DBHelper.tableName + " (x_val, y_val, z_val, timestamp) values ('"+patientIDText+"', '"+ageText+"' );" );
            //db.setTransactionSuccessful(); //commit your changes
            Log.d(Constants.CUSTOM_LOG_TYPE, "Inserted successfully");

        }
        catch (SQLiteException e) {
            //report problem
            Log.d(Constants.CUSTOM_LOG_TYPE, e.getMessage());
            e.printStackTrace();
        }
        finally {
            //db.endTransaction();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public List<Float> fetchDataList(){

        Log.d(Constants.CUSTOM_LOG_TYPE, "fetch data function called");
        Cursor cur = db.rawQuery("SELECT * FROM " + DBHelper.tableName, null);
        ArrayList temp = new ArrayList();
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String timestamp = cur.getString(cur.getColumnIndex("x_val"));
                    Log.d(Constants.CUSTOM_LOG_TYPE, "x values->" +timestamp);
                    temp.add(timestamp);
                } while (cur.moveToNext());
            }
        }

        Log.d(Constants.CUSTOM_LOG_TYPE, "number of rows fetched-->" + temp.size());
        return temp;
    }

    public Cursor fetchData(){

        Log.d(Constants.CUSTOM_LOG_TYPE, "fetch data function called on table name ->");

            if (DBHelper.tableName != null) {
                Cursor cur = db.rawQuery("SELECT * FROM " + DBHelper.tableName, null);
                return cur;
            } else {
                return null;
            }


        /*ArrayList temp = new ArrayList();

        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String timestamp = cur.getString(cur.getColumnIndex("x_val"));
                    Log.d(Constants.CUSTOM_LOG_TYPE, "x values->" +timestamp);
                    temp.add(timestamp);
                } while (cur.moveToNext());
            }
        }

        Log.d(Constants.CUSTOM_LOG_TYPE, "number of rows fetched-->" + temp.size());*/

    }
}