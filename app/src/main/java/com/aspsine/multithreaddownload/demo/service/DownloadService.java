package com.aspsine.multithreaddownload.demo.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.core.DownloadException;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;

/**
 * Created by aspsine on 15/7/28.
 */
public class DownloadService extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    private void download(AppInfo appInfo){

        DownloadManager.getInstance().download(appInfo.getName(), appInfo.getUrl(), null, new CallBack() {
            @Override
            public void onDownloadStart() {
                Notification notification = new NotificationCompat.Builder(getApplicationContext()).build();
            }

            @Override
            public void onConnected(long total, boolean isRangeSupport) {

            }

            @Override
            public void onProgress(long finished, long total, int progress) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onDownloadPause() {

            }

            @Override
            public void onDownloadCancel() {

            }

            @Override
            public void onFailure(DownloadException e) {

            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
