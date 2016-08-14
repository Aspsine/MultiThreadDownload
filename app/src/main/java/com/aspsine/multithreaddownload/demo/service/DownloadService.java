package com.aspsine.multithreaddownload.demo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.aspsine.multithreaddownload.demo.util.Utils;
import com.aspsine.multithreaddownload.util.L;

import java.io.File;

/**
 * Created by aspsine on 15/7/28.
 */
public class DownloadService extends Service {

    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String ACTION_DOWNLOAD_BROAD_CAST = "com.aspsine.multithreaddownload.demo:action_download_broad_cast";

    public static final String ACTION_DOWNLOAD = "com.aspsine.multithreaddownload.demo:action_download";

    public static final String ACTION_PAUSE = "com.aspsine.multithreaddownload.demo:action_pause";

    public static final String ACTION_CANCEL = "com.aspsine.multithreaddownload.demo:action_cancel";

    public static final String ACTION_PAUSE_ALL = "com.aspsine.multithreaddownload.demo:action_pause_all";

    public static final String ACTION_CANCEL_ALL = "com.aspsine.multithreaddownload.demo:action_cancel_all";

    public static final String EXTRA_POSITION = "extra_position";

    public static final String EXTRA_TAG = "extra_tag";

    public static final String EXTRA_APP_INFO = "extra_app_info";

    /**
     * Dir: /Download
     */
    private File mDownloadDir;

    private DownloadManager mDownloadManager;

    private NotificationManagerCompat mNotificationManager;

    public static void intentDownload(Context context, int position, String tag, AppInfo info) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_APP_INFO, info);
        context.startService(intent);
    }

    public static void intentPause(Context context, String tag) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_TAG, tag);
        context.startService(intent);
    }

    public static void intentPauseAll(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }

    public static void destory(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int position = intent.getIntExtra(EXTRA_POSITION, 0);
            AppInfo appInfo = (AppInfo) intent.getSerializableExtra(EXTRA_APP_INFO);
            String tag = intent.getStringExtra(EXTRA_TAG);
            switch (action) {
                case ACTION_DOWNLOAD:
                    download(position, appInfo, tag);
                    break;
                case ACTION_PAUSE:
                    pause(tag);
                    break;
                case ACTION_CANCEL:
                    cancel(tag);
                    break;
                case ACTION_PAUSE_ALL:
                    pauseAll();
                    break;
                case ACTION_CANCEL_ALL:
                    cancelAll();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void download(final int position, final AppInfo appInfo, String tag) {
        final DownloadRequest request = new DownloadRequest.Builder()
                .setName(appInfo.getName() + ".apk")
                .setUri(appInfo.getUrl())
                .setFolder(mDownloadDir)
                .build();
        mDownloadManager.download(request, tag, new DownloadCallBack(position, appInfo, mNotificationManager, getApplicationContext()));
    }

    private void pause(String tag) {
        mDownloadManager.pause(tag);
    }

    private void cancel(String tag) {
        mDownloadManager.cancel(tag);
    }

    private void pauseAll() {
        mDownloadManager.pauseAll();
    }

    private void cancelAll() {
        mDownloadManager.cancelAll();
    }

    public static class DownloadCallBack implements CallBack {

        private int mPosition;

        private AppInfo mAppInfo;

        private LocalBroadcastManager mLocalBroadcastManager;

        private NotificationCompat.Builder mBuilder;

        private NotificationManagerCompat mNotificationManager;

        private long mLastTime;

        public DownloadCallBack(int position, AppInfo appInfo, NotificationManagerCompat notificationManager, Context context) {
            mPosition = position;
            mAppInfo = appInfo;
            mNotificationManager = notificationManager;
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            mBuilder = new NotificationCompat.Builder(context);
        }

        @Override
        public void onStarted() {
            L.i(TAG, "onStart()");
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(mAppInfo.getName())
                    .setContentText("Init Download")
                    .setProgress(100, 0, true)
                    .setTicker("Start download " + mAppInfo.getName());
            updateNotification();
        }

        @Override
        public void onConnecting() {
            L.i(TAG, "onConnecting()");
            mBuilder.setContentText("Connecting")
                    .setProgress(100, 0, true);
            updateNotification();

            mAppInfo.setStatus(AppInfo.STATUS_CONNECTING);
            sendBroadCast(mAppInfo);
        }

        @Override
        public void onConnected(long total, boolean isRangeSupport) {
            L.i(TAG, "onConnected()");
            mBuilder.setContentText("Connected")
                    .setProgress(100, 0, true);
            updateNotification();
        }

        @Override
        public void onProgress(long finished, long total, int progress) {

            if (mLastTime == 0) {
                mLastTime = System.currentTimeMillis();
            }

            mAppInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
            mAppInfo.setProgress(progress);
            mAppInfo.setDownloadPerSize(Utils.getDownloadPerSize(finished, total));

            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime > 500) {
                L.i(TAG, "onProgress()");
                mBuilder.setContentText("Downloading");
                mBuilder.setProgress(100, progress, false);
                updateNotification();

                sendBroadCast(mAppInfo);

                mLastTime = currentTime;
            }
        }

        @Override
        public void onCompleted() {
            L.i(TAG, "onCompleted()");
            mBuilder.setContentText("Download Complete");
            mBuilder.setProgress(0, 0, false);
            mBuilder.setTicker(mAppInfo.getName() + " download Complete");
            updateNotification();

            mAppInfo.setStatus(AppInfo.STATUS_COMPLETE);
            mAppInfo.setProgress(100);
            sendBroadCast(mAppInfo);
        }

        @Override
        public void onDownloadPaused() {
            L.i(TAG, "onDownloadPaused()");
            mBuilder.setContentText("Download Paused");
            mBuilder.setTicker(mAppInfo.getName() + " download Paused");
            mBuilder.setProgress(100, mAppInfo.getProgress(), false);
            updateNotification();

            mAppInfo.setStatus(AppInfo.STATUS_PAUSED);
            sendBroadCast(mAppInfo);
        }

        @Override
        public void onDownloadCanceled() {
            L.i(TAG, "onDownloadCanceled()");
            mBuilder.setContentText("Download Canceled");
            mBuilder.setTicker(mAppInfo.getName() + " download Canceled");
            updateNotification();

            //there is 1000 ms memory leak, shouldn't be a problem
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(mPosition + 1000);
                }
            }, 1000);

            mAppInfo.setStatus(AppInfo.STATUS_NOT_DOWNLOAD);
            mAppInfo.setProgress(0);
            mAppInfo.setDownloadPerSize("");
            sendBroadCast(mAppInfo);
        }

        @Override
        public void onFailed(DownloadException e) {
            L.i(TAG, "onFailed()");
            e.printStackTrace();
            mBuilder.setContentText("Download Failed");
            mBuilder.setTicker(mAppInfo.getName() + " download failed");
            mBuilder.setProgress(100, mAppInfo.getProgress(), false);
            updateNotification();

            mAppInfo.setStatus(AppInfo.STATUS_DOWNLOAD_ERROR);
            sendBroadCast(mAppInfo);
        }

        private void updateNotification() {
            mNotificationManager.notify(mPosition + 1000, mBuilder.build());
        }

        private void sendBroadCast(AppInfo appInfo) {
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
            intent.putExtra(EXTRA_POSITION, mPosition);
            intent.putExtra(EXTRA_APP_INFO, appInfo);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadManager = DownloadManager.getInstance();
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        mDownloadDir = new File(Environment.getExternalStorageDirectory(), "Download");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadManager.pauseAll();
    }


}
