package com.example.theawesomeguy.group7;

/**
 * Created by peps on 6/17/17.
 */

public class Constants {

    public static int MIN_X_VIEW = 0;
    public static int MAN_X_VIEW = 5;
    public static float AXIS_TITLE_TEXT_SIZE = 25.0f;
    public static float HOR_TITLE_TEXT_SIZE = 35.0f;
    public static float VER_TITLE_TEXT_SIZE = 35.0f;

    public static int RUNNING_STATE_OFF = 0;
    public static int RUNNING_STATE_ON = 1;

    public static String DB_DIRECTORY_NAME = "CSE535_ASSIGNMENT2";
    public static String DBNAME = "group7_DB";

    public static String DELIMITER = "_";

    public static String MALE_RADIO_BTN_TXT = "MALE";
    public static String FEMALE_RADIO_BTN_TXT= "FEMALE";

    public static int SENSOR_1HZ_DELAY = 1000000;

    public static String CUSTOM_LOG_TYPE = "GROUP7-LOGS";

    public static String NAME_REGEX =  "[a-zA-Z][a-zA-Z ]*";

    public static int INPUT_INVALID = 0;
    public static int INPUT_VALID = 1;

    public static int AGE_TYPE = 0;
    public static int NAME_TYPE = 1;
    public static int ID_TYPE = 2;

    public static String INVALID_AGE__ERROR = "Invalid age value!";
    public static String INVALID_ID__ERROR = "ID cannot be empty or greater than 10 characters.!";
    public static String INVALID_NAME__ERROR = "Invalid name value!";

    public static String DATA_OK_START_ACCMTR_MSG = "Data Stored. Starting Accelerometer.";
    public static String DATA_NOT_OK_MSG = "One of the fields is invalid. Data cannot be stored until correct!";

}
