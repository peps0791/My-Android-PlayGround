package com.example.theawesomeguy.group7;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by peps on 6/17/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    String sdCardPath = null;
    String dbDir = null;
    File dbDirFilePath = null;
    private static SQLiteDatabase db;

    private static String tableName = null;

    private static DBHelper dbHelper = null;

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion){

        Log.d(Constants.CUSTOM_LOG_TYPE, "OnUpgrade");

    }

    @Override
    public void onCreate(SQLiteDatabase db){

        Log.d(Constants.CUSTOM_LOG_TYPE, "OnCreate");

    }

    public static DBHelper getInstance(Context ctx){
        if (dbHelper==null){
            dbHelper = new DBHelper(ctx.getApplicationContext());
        }
        return dbHelper;
    }

    public boolean isTableSet(){
        return (tableName !=null);
    }


    public SQLiteDatabase getDBInstance(){

        String dbPath = Environment.getExternalStorageDirectory() + File.separator + Constants.DB_DIRECTORY_NAME;
        if(this.db == null){
            this.db = SQLiteDatabase.openOrCreateDatabase(dbPath + File.separator + Constants.DBNAME, null);
        }
        return this.db;
    }


    private DBHelper(Context ctx) {
        //default constructor

        super(ctx, Constants.DBNAME, null, 1);
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


    public void createTableWhenConditionsMet1(String name){

        try {
            //in case db is set to null;
            db = this.getDBInstance();

            db.beginTransaction();
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + name
                    + " (timestamp VARCHAR(10), x Long(10), y Long(10), z Long(10));");

            db.setTransactionSuccessful(); //commit your changes

            DBHelper.tableName = name;

        }
        catch (Exception e){
            Log.d(Constants.CUSTOM_LOG_TYPE, e.toString());
        }

    }

    public void createTableWhenConditionsMet(String tableName){
        //tableName = "testtable";
        Log.d(Constants.CUSTOM_LOG_TYPE, "createTable called with table name -->" +tableName);

        try {
            db.beginTransaction();

            String query = "create table " +  tableName + " ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " x_val real, "
                    + " y_val real,"
                    + " z_val real,"
                    + " timestamp integer); ";

            Log.d(Constants.CUSTOM_LOG_TYPE, "QUERY->" +query);
            //perform your database operations here ...
            db.execSQL(query);

            db.setTransactionSuccessful(); //commit your changes
            DBHelper.tableName = tableName;
        }
        catch (SQLiteException e) {
            //report problem

            //Some propmt ???/
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


    public void insertInTable1(float x, float y, float z, long timestamp){
        try{

            db = this.getDBInstance();
            String query="INSERT INTO "+ DBHelper.tableName+"(timestamp, x, y, z) VALUES( '"+String.valueOf(timestamp)+"',"+
                    String.valueOf(x)+","+String.valueOf(y)+","+String.valueOf(z)+")";
            Log.d(Constants.CUSTOM_LOG_TYPE, query);
            db.execSQL(query);
        }
        catch (Exception e){
            Log.d(Constants.CUSTOM_LOG_TYPE, e.toString());
        }
    }

    public void insertInTable(float x, float y, float z, int timestamp){

        try {
            //perform your database operations here ...
           /* String datetimeTimeStamp;
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            String timestamp = dateFormat.format(date);*/

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

    public void setTableName(){

        Cursor cur = db.rawQuery("SELECT name FROM SQLITE_MASTER WHERE type='table'", null);
        ArrayList temp = new ArrayList();
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String table_name = cur.getString(cur.getColumnIndex("name"));

                    //check data in table
                    Log.d(Constants.CUSTOM_LOG_TYPE, "table_name->" +table_name);

                } while (cur.moveToNext());
            }else{
                Log.d(Constants.CUSTOM_LOG_TYPE, "Cursor was empty!!!");
            }
        }else{
            Log.d(Constants.CUSTOM_LOG_TYPE, "Cursor was null!!!");
        }

    }

    public void switchToDownloadDB(){


        //download location
        Log.d(Constants.CUSTOM_LOG_TYPE, " DB being switched!!");
        String downloadLoc = Environment.getExternalStorageDirectory() + File.separator +
                Constants.DB_DIRECTORY_NAME_DOWNLOAD + File.separator + Constants.DBNAME;

        db = SQLiteDatabase.openOrCreateDatabase(downloadLoc, null);
        setTableName();

    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public Map<Integer, List<List<Float>>> fetchDataList(int timestamp, int limit){

        Log.d(Constants.CUSTOM_LOG_TYPE, "fetch data function called with table name ::" +tableName);
        Cursor cur = db.rawQuery("SELECT * FROM " + tableName + " WHERE TIMESTAMP > " + String.valueOf(timestamp) + " ORDER BY TIMESTAMP DESC LIMIT " +
                String.valueOf(limit), null);
        List<Float> xList = new ArrayList();
        List<Float> yList = new ArrayList();
        List<Float> zList = new ArrayList();
        List<List<Float>> xyzList = new ArrayList<>();
        Map<Integer, List<List<Float>>> map= new HashMap<>();


        int count = 0;
        int lastTimeStamp =0;
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String xVal = cur.getString(0);
                    String yVal = cur.getString(1);
                    String zVal = cur.getString(2);
                    if(count==0){
                        lastTimeStamp = cur.getInt(3);
                    }

                    //Log.d(Constants.CUSTOM_LOG_TYPE, "x value->" +xVal + " y value->" +yVal + " z value->" +zVal);
                    xList.add(Float.valueOf(xVal));
                    yList.add(Float.valueOf(yVal));
                    zList.add(Float.valueOf(zVal));
                } while (cur.moveToNext());
            }
        }

        //close cursor
        cur.close();
        cur = null;

        xyzList.add(xList);
        xyzList.add(yList);
        xyzList.add(zList);
        map.put(lastTimeStamp, xyzList);
        Log.d(Constants.CUSTOM_LOG_TYPE, "number of rows fetched-->" + xList.size());
        return map;
    }

    public void closeDB(){
        //this.db.close();
        //this.db = null;
    }

    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}