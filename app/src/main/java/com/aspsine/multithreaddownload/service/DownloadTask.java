package com.aspsine.multithreaddownload.service;

import android.content.Context;
import android.content.Intent;

import com.aspsine.multithreaddownload.db.ThreadInfoRepository;
import com.aspsine.multithreaddownload.db.ThreadInfoRepositoryImpl;
import com.aspsine.multithreaddownload.entity.FileInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadInfoRepository mRepository;

    private int mFinished = 0;
    private boolean mIsPause = false;

    public DownloadTask(Context context, FileInfo fileInfo) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        mRepository = new ThreadInfoRepositoryImpl(context);

    }

    public void download(){
        mIsPause = false;
        List<ThreadInfo> threadInfos = mRepository.getThreadInfos(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threadInfos.get(0);
        }
        new DownloadThread(threadInfo).start();
    }

    public void pause(){
        mIsPause = true;
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        private Intent mIntent;

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
            this.mIntent = new Intent(DownloadService.ACTION_UPDATE);
        }

        @Override
        public void run() {
            if (!mRepository.exists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mRepository.insert(mThreadInfo);
            }

            HttpURLConnection httpConn = null;
            InputStream inputStream = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setConnectTimeout(5*1000);
                httpConn.setRequestMethod("GET");

                int start = mThreadInfo.getStart() + mThreadInfo.getFinshed();
                int end = mThreadInfo.getEnd();
                httpConn.setRequestProperty("Range", "bytes=" + start + "-" +end);

                File file = new File(FileUtils.getDownloadDir(mContext), mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                mFinished = mThreadInfo.getFinshed();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    inputStream = new BufferedInputStream(httpConn.getInputStream());
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        int progress = Integer.valueOf(mFinished * 100/ mFileInfo.getLength());
                        if (System.currentTimeMillis() - time > 500 || progress == 100) {
                            time = System.currentTimeMillis();
                            mIntent.putExtra(DownloadService.EXTRA_FINISHED, progress);
                            mContext.sendBroadcast(mIntent);
                        }
                        if (mIsPause) {
                            mRepository.update(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }

                    mRepository.delete(mThreadInfo.getUrl(), mThreadInfo.getId());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpConn.disconnect();
                try {
                    if (inputStream != null){
                        inputStream.close();
                    }
                    if(raf != null){
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface ProgressCallBacks{
        public void onProgress(int progress);
    }
}
