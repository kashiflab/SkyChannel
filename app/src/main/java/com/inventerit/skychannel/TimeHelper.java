package com.inventerit.skychannel;

import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeHelper {

    private static final String SEPARATOR = " ";

    private static final String TAG = "TimeHelper";

    @Nullable
    public static long getDateTime(int seconds) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(date);               // sets calendar time/date
        cal.add(Calendar.SECOND, seconds);      // adds one hour
        String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        Log.i(TAG,newDate +": newDate");

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = sdf.parse(newDate);

            return date.getTime();
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
            return 0;
        }

    }

}
