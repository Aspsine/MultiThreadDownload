package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/22.
 */
public interface DownloadTask extends Runnable {

    interface OnDownloadListener {
        void onProgress(int finished, int length);

        void onComplete();

        void onPause();

        void onCancel();

        void onFailure(DownloadException de);
    }

    void cancel();

    void pause();

    boolean isDownloading();

    boolean isComplete();

    boolean isPaused();

    boolean isCanceled();

    boolean isFailure();

    @Override
    void run();
}
