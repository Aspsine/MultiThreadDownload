package com.aspsine.multithreaddownload.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

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

/**
 * Created by aspsine on 15-4-19.
 */
public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    public static final String EXTRA_FILE_INFO = "file_info";
    public static final String ACTION_START = "action_start";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_UPDATE = "action_update";

    private static final int MSG_INIT = 0;

    static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_INIT) {
                FileInfo fileInfo = (FileInfo) msg.obj;

            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            Log.i(TAG, "start " + this.hashCode());
            new InitThread(this, fileInfo).start();
        } else if (ACTION_PAUSE.equals(action)) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            Log.i(TAG, "stop " + this.hashCode());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private static class InitThread extends Thread {
        FileInfo mFileInfo;
        Context mContext;

        private InitThread(Context context, FileInfo fileInfo) {
            mFileInfo = fileInfo;
            mContext = context;
        }

        @Override
        public void run() {
            HttpURLConnection httpConn = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                httpConn = (HttpURLConnection) url.openConnection();
                int length = -1;
                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = httpConn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = FileUtils.getDownloadDir(mContext);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                handler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpConn.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class DownloadThread extends Thread {
        private Context mContext;
        private ThreadInfo mThreadInfo;
        private FileInfo mFileInfo;
        private ThreadInfoRepository mRepository;

        public DownloadThread(Context context, ThreadInfo threadInfo, FileInfo fileInfo) {
            this.mContext = context;
            this.mThreadInfo = threadInfo;
            this.mFileInfo = fileInfo;
            this.mRepository = new ThreadInfoRepositoryImpl(context);
        }

        @Override
        public void run() {
            if(!mRepository.exists(mThreadInfo.getUrl(), mThreadInfo.getId())){
                mRepository.insert(mThreadInfo);
            }

            HttpURLConnection httpConn = null;
            InputStream inputStream = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("GET");
                int start = mThreadInfo.getStart() + mThreadInfo.getFinshed();
                int end = mThreadInfo.getEnd();
                httpConn.setRequestProperty("Range", "bytes=" + start + "-" + end);

                File file = new File(FileUtils.getDownloadDir(mContext), mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    inputStream = new BufferedInputStream(httpConn.getInputStream());
                    byte[] buffer = new byte[1024*4];
                    int len = -1;
                    while ((len =inputStream.read(buffer)) != -1){
                        raf.write(buffer, 0, len) ;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
