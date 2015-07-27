package com.aspsine.multithreaddownload.core;


import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * Created by Aspsine on 2015/7/22.
 */
public class SingleDownloadTask extends AbsDownloadTask {

    public SingleDownloadTask(DownloadInfo mDownloadInfo, ThreadInfo mThreadInfo, OnDownloadListener mOnDownloadListener) {
        super(mDownloadInfo, mThreadInfo, mOnDownloadListener);
    }

    @Override
    protected void insertIntoDB(ThreadInfo info) {

    }

    @Override
    protected void updateDBProgress(ThreadInfo info) {

    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        return null;
    }

    @Override
    protected RandomAccessFile getFile(ThreadInfo threadInfo, DownloadInfo downloadInfo) throws IOException {
        File file = new File(downloadInfo.getDir(), downloadInfo.getName());
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }

    @Override
    protected String getTag() {
        return this.getClass().getSimpleName();
    }
}

