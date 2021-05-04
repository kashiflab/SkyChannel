package com.inventerit.skychannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Utils {
    private static ProgressDialog pDialog;

    public static void initpDialog(Context context, String msg) {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage(msg);
        pDialog.setCancelable(false);
    }

    public static void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    public static void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    public static String getDateTime(){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Log.d("Hello", "Current DATETIME: " + df.format(date).toString());
        return df.format(date);
    }
}
