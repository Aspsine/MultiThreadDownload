package com.aspsine.multithreaddownload.service;

import android.os.AsyncTask;

/**
 * Created by Aspsine on 2015/7/14.
 */
public interface CallBack {

    void onProgressUpdate(int finished, int total, int percent);

    void onComplete();

    void onFailure(Exception e);
}
