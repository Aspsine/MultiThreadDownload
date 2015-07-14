package com.aspsine.multithreaddownload;

import android.app.Application;
import android.content.Context;

import com.aspsine.multithreaddownload.db.ThreadInfoRepository;
import com.aspsine.multithreaddownload.db.ThreadInfoRepositoryImpl;
import com.squareup.picasso.Picasso;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class App extends Application {

    private static ThreadInfoRepository sThreadInfoRepository;

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        CrashHandler.getInstance(getApplicationContext());
    }

    public static ThreadInfoRepository getThreadInfoRepository() {
        if (sThreadInfoRepository == null) {
            synchronized (App.class) {
                sThreadInfoRepository = new ThreadInfoRepositoryImpl(sContext);
            }
        }
        return sThreadInfoRepository;
    }
}
