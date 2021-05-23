package com.inventerit.skychannel;

import android.app.Application;

import com.tramsun.libs.prefcompat.Pref;

public class MyApp extends Application {
    public static String YOUTUBE_KEY = "AIzaSyDUqZdrWnKoGlKVF3cMllPqybWi2TO9NIg";// paste your youtube key here
    @Override
    public void onCreate() {
        super.onCreate();
        Pref.init(this);
    }
}
