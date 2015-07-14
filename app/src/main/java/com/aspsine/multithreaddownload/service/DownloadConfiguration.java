package com.aspsine.multithreaddownload.service;

import android.content.Context;

import com.aspsine.multithreaddownload.util.FileUtils;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadConfiguration {
    /**
     * dir of the download file will be stored
     */
    public File downloadDir;
    /**
     * the max num of thread that Downloader create
     */
    public int maxThreadNum;

    /**
     * init with default value
     *
     * @param context
     */
    public DownloadConfiguration(Context context) {
        final File defaultDownloadDir = FileUtils.getDownloadDir(context.getApplicationContext());
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
