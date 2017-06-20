package com.example.theawesomeguy.group7;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peps on 6/18/17.
 */

public class Misc {

    public static boolean isFieldValid(TextView field, int formatType){

        Log.d(Constants.CUSTOM_LOG_TYPE, "field->" +field + " formatType->" + formatType);

        boolean isValid = true;
        String fieldVal = field.getText().toString();

        if(formatType == Constants.AGE_TYPE){
            try {
                int val = Integer.parseInt(fieldVal);
                if (val <=0 || val >120){
                    isValid = false;
                }
            }catch(Exception ex){
                Log.d(Constants.CUSTOM_LOG_TYPE, Constants.INVALID_AGE__ERROR);
                ex.printStackTrace();
                isValid = false;
            }

        }else if (formatType == Constants.ID_TYPE){
            isValid = true;
            if(fieldVal.isEmpty() || fieldVal.length()>10){
                isValid = false;
            }

        }else if(formatType == Constants.NAME_TYPE){

            CharSequence inputStr = fieldVal;
            Pattern pattern = Pattern.compile(Constants.NAME_REGEX);
            Matcher matcher = pattern.matcher(inputStr);
            if(!matcher.matches()) {
                isValid = false;
            }
        }

        return isValid;
    }


}