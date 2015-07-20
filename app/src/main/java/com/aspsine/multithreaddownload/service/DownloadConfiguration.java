package com.aspsine.multithreaddownload.service;

import android.content.Context;

import com.aspsine.multithreaddownload.util.FileUtils;

import java.io.File;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadConfiguration {
    /**
     * dir of the download file will be stored
     */
    public File downloadDir;
    /**
     * the max num of thread that  create
     */
    public int maxThreadNum;

    public final Context context;

    /**
     * init with default value
     *
     * @param context
     */
    public DownloadConfiguration(Context context) {
        this.context = context.getApplicationContext();

        final File defaultDownloadDir = FileUtils.getDownloadDir(this.context);
        final int defaultMaxThreadNum = 10;

        downloadDir = defaultDownloadDir;
        maxThreadNum = defaultMaxThreadNum;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    public int getMaxThreadNum() {
        return maxThreadNum;
    }

    public void setMaxThreadNum(int maxThreadNum) {
        this.maxThreadNum = maxThreadNum;
    }
}
