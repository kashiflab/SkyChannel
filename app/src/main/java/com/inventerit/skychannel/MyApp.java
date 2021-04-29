package com.inventerit.skychannel;

import android.app.Application;

import com.tramsun.libs.prefcompat.Pref;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Pref.init(this);
    }
}
