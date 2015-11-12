package com.aspsine.multithreaddownload.demo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;

import java.io.File;

/**
 * Created by aspsine on 15/7/28.
 */
public class DownloadService extends Service {

    /**
     * Dir: /Download
     */
    private File mDownloadDir;

    public static final String ACTION_DOWNLOAD = "com.aspsine.multithreaddownload.demo:action_download";

    public static final String ACTION_PAUSE = "com.aspsine.multithreaddownload.demo:action_pause";

    public static final String ACTION_CANCEL = "com.aspsine.multithreaddownload.demo:action_cancel";

    public static final String ACTION_PAUSE_ALL = "com.aspsine.multithreaddownload.demo:action_pause_all";

    public static final String ACTION_CANCEL_ALL = "com.aspsine.multithreaddownload.demo:action_cancel_all";

    private DownloadManager mDownloadManager;

    private NotificationManagerCompat mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int position = intent.getIntExtra("EXTRA_POSITION", 0);
            AppInfo appInfo = (AppInfo) intent.getSerializableExtra("EXTRA_APPINFO");
            String tag = intent.getStringExtra("EXTRA_TAG");
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
                .setTitle(appInfo.getName() + ".apk")
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

        public DownloadCallBack(int position, AppInfo appInfo, NotificationManagerCompat notificationManager, Context context) {
            mPosition = position;
            mAppInfo = appInfo;
            mNotificationManager = notificationManager;
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            mBuilder = new NotificationCompat.Builder(context);
        }

        @Override
        public void onStarted() {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);

        }

        @Override
        public void onConnecting() {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);

        }

        @Override
        public void onConnected(long total, boolean isRangeSupport) {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);

        }

        @Override
        public void onProgress(long finished, long total, int progress) {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);
        }


        @Override
        public void onCompleted() {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onDownloadPaused() {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onDownloadCanceled() {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
            mLocalBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailed(DownloadException e) {
            mNotificationManager.notify(mPosition, mBuilder.build());

            Intent intent = new Intent();
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
