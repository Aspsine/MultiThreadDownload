package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.core.DownloadException;

/**
 * Created by Aspsine on 2015/7/14.
 */
public interface CallBack {

    void onDownloadStart();

    void onConnected(int total, boolean isRangeSupport);

    void onProgress(int finished, int total, int progress);

    void onComplete();

    void onDownloadPause();

    void onDownloadCancel();

    void onFailure(DownloadException e);
}
