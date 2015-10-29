package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/20.
 */

import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.db.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * download thread
 */
public class MultiDownloadTask extends DownloadTaskImpl {

    private DataBaseManager mDBManager;

    public MultiDownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, DataBaseManager dbManager, OnDownloadListener listener) {

        super(downloadInfo, threadInfo, listener);
        this.mDBManager = dbManager;
    }

    @Override
    protected void insertIntoDB(ThreadInfo info) {
        if (!mDBManager.exists(info.getUrl(), info.getId())) {
            mDBManager.insert(info);
        }
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_PARTIAL;
    }

    @Override
    protected void updateDBProgress(ThreadInfo info) {
        mDBManager.update(info.getUrl(), info.getId(), info.getFinished());
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        Map<String, String> headers = new HashMap<String, String>();
        long start = info.getStart() + info.getFinished();
        long end = info.getEnd();
        headers.put("Range", "bytes=" + start + "-" + end);
        return headers;
    }

    @Override
    protected RandomAccessFile getFile(ThreadInfo threadInfo, DownloadInfo downloadInfo) throws IOException {
        File file = new File(downloadInfo.getDir(), downloadInfo.getName());
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        long start = threadInfo.getStart() + threadInfo.getFinished();
        raf.seek(start);
        return raf;
    }

    @Override
    protected String getTag() {
        return this.getClass().getSimpleName();
    }
}