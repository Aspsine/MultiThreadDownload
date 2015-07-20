package com.aspsine.multithreaddownload.service;

/**
 * Created by Aspsine on 2015/7/14.
 */
public interface CallBack {

    void onConnected(int total);

    void onProgress(int finished, int total, int progress);

    void onComplete();

    void onDownloadPause();

    void onDownloadCancel();

    void onFailure(Exception e);
}
