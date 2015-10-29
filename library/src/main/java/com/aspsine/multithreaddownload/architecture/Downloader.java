package com.aspsine.multithreaddownload.architecture;

/**
 * Created by Aspsine on 2015/10/29.
 */
public interface Downloader {

    public interface OnDownloaderDestroyedListener {
        void onDestroyed(String key, Downloader downloader);
    }

    boolean isRunning();

    void start();

    void pause();

    void cancel();

    void onDestroy();

}
