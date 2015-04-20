package com.aspsine.multithreaddownload;

import android.app.Application;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance(getApplicationContext());
    }
}
