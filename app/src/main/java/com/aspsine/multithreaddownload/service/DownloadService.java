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
import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    public static final String EXTRA_FILE_INFO = "file_info";
    public static final String EXTRA_FINISHED = "finished";

    public static final String ACTION_START = "action_start";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_UPDATE = "action_update";

    private static final int MSG_INIT = 0;

    private DownloadTask mDownloadTask;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_INIT) {
                FileInfo fileInfo = (FileInfo) msg.obj;
                mDownloadTask = new DownloadTask(DownloadService.this, fileInfo);
                mDownloadTask.download();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null){
            Log.i(TAG, "intent = null");
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            Log.i(TAG, "start " + this.hashCode());
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            download(fileInfo);
        } else if (ACTION_PAUSE.equals(action)) {
            Log.i(TAG, "pause " + this.hashCode());
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            if(mDownloadTask != null){
                mDownloadTask.pause();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void download(FileInfo fileInfo) {
        new InitThread(this, fileInfo).start();
    }

    class InitThread extends Thread {
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
                httpConn.setConnectTimeout(10 * 1000);
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
                    if (raf != null) raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
