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
    private static final int MSG_PAUSE = 1;

    private static DownloadHandler handler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            download(fileInfo);
        } else if (ACTION_PAUSE.equals(action)) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(EXTRA_FILE_INFO);
            pause(fileInfo);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (handler == null) {
            handler = new DownloadHandler(this);
        }
    }

    public void download(FileInfo fileInfo) {
        new InitThread(this, fileInfo).start();
        Log.i(TAG, "start " + this.hashCode());
    }

    public void pause(FileInfo fileInfo) {
        handler.obtainMessage(MSG_PAUSE).sendToTarget();
        Log.i(TAG, "stop " + this.hashCode());
    }

    private static class DownloadHandler extends Handler {
        private Context mContext;
        private DownloadThread mDownloadThread;

        public DownloadHandler(Context context) {
            this.mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_INIT) {
                FileInfo fileInfo = (FileInfo) msg.obj;
                mDownloadThread = new DownloadThread(mContext, fileInfo);
                mDownloadThread.start();
            } else if (msg.what == MSG_PAUSE) {
                mDownloadThread.setPause(true);
            }
        }
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
                httpConn.setConnectTimeout(10*1000);
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

    private static class DownloadThread extends Thread {
        private Context mContext;
        private ThreadInfo mThreadInfo;
        private FileInfo mFileInfo;
        private ThreadInfoRepository mRepository;

        private Intent mIntent;

        private int mFinished = 0;
        private boolean mIsPause = false;

        public DownloadThread(Context context, FileInfo fileInfo) {
            this.mContext = context;
            this.mFileInfo = fileInfo;

            mRepository = new ThreadInfoRepositoryImpl(context);
            List<ThreadInfo> threadInfos = mRepository.getThreadInfos(fileInfo.getUrl());

            if (threadInfos.size() == 0) {
                this.mThreadInfo = new ThreadInfo(0, fileInfo.getUrl(), 0, fileInfo.getLength(), 0);
            } else {
                this.mThreadInfo = threadInfos.get(0);
            }

            this.mIntent = new Intent(ACTION_UPDATE);
        }

        public void setPause(boolean isPause) {
            this.mIsPause = isPause;
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
                httpConn.setRequestMethod("GET");
                int start = mThreadInfo.getStart() + mThreadInfo.getFinshed();
                int end = mThreadInfo.getEnd();
                httpConn.setRequestProperty("Range", "bytes=" + start + "-" + end);

                File file = new File(FileUtils.getDownloadDir(mContext), mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                mFinished = mThreadInfo.getFinshed();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = new BufferedInputStream(httpConn.getInputStream());
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            mIntent.putExtra(EXTRA_FINISHED, Integer.valueOf((mFinished / mFileInfo.getLength()) * 100));
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
                    inputStream.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
