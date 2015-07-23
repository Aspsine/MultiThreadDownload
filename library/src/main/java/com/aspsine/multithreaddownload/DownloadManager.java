package com.aspsine.multithreaddownload;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.aspsine.multithreaddownload.core.DownloadRequest;
import com.aspsine.multithreaddownload.core.DownloadStatus;
import com.aspsine.multithreaddownload.core.DownloadStatusDelivery;
import com.aspsine.multithreaddownload.core.DownloadStatusDeliveryImpl;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadManager {

    /**
     * singleton of DownloadManager
     */
    private static DownloadManager sDownloadManager;

    private DataBaseManager mDBManager;

    /**
     * key: Tag
     * value:DownloadRequest
     */
    private Map<String, DownloadRequest> mDownloadRequestMap;

    private DownloadConfiguration mConfig;

    private ExecutorService mExecutorService;

    private DownloadStatusDelivery mDelivery;

    public static DownloadManager getInstance() {
        if (sDownloadManager == null) {
            synchronized (DownloadManager.class) {
                sDownloadManager = new DownloadManager();
            }
        }
        return sDownloadManager;
    }

    /**
     * private construction
     */
    private DownloadManager() {
        mDownloadRequestMap = new LinkedHashMap<>();
    }

    public void init(DownloadConfiguration configuration) {
        if (configuration == null) {
            throw new RuntimeException("configuration can not be null!");
        }
        this.mConfig = configuration;
        mExecutorService = Executors.newFixedThreadPool(configuration.maxThreadNum);
        mDelivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));

        mDBManager = DataBaseManager.getInstance(mConfig.context);
    }

    /**
     * @param fileName
     * @param url
     * @param callBack
     */
    public void download(String fileName, String url, CallBack callBack) {
        if (mConfig == null) {
            throw new RuntimeException("Please config first!");
        }
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(url)) {
            throw new RuntimeException("fileName or url can not be null or empty!");
        }
        final String tag = createTag(url);
        final DownloadInfo downloadInfo;
        final DownloadRequest request;
        if (mDownloadRequestMap.containsKey(tag)) {
            Log.i("DownloadManager", "use cached request");
            request = mDownloadRequestMap.get(tag);
        } else {
            Log.i("DownloadManager", "use new request");
            downloadInfo = new DownloadInfo(fileName, url);
            request = new DownloadRequest(downloadInfo, mConfig.downloadDir, mDBManager, mExecutorService, new DownloadStatus(), mDelivery);
            mDownloadRequestMap.put(tag, request);
        }
        if (!request.isStarted()) {
            request.start(callBack);
        }
    }

    public void pause(String url) {
        String tag = createTag(url);
        DownloadRequest request = mDownloadRequestMap.get(tag);
        request.pause();
    }

    public void cancel(String url) {
        String tag = createTag(url);
        DownloadRequest request = mDownloadRequestMap.get(tag);
        request.cancel();
        mDownloadRequestMap.remove(tag);
    }

    public DownloadInfo getDownloadProgress(String url) {
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(url);
        DownloadInfo downloadInfo = null;
        if (!threadInfos.isEmpty()) {

        }
        return downloadInfo;
    }

    private DownloadRequest getDownloadRequest(String url) {
        return mDownloadRequestMap.get(createTag(url));
    }

    private static String createTag(String url) {
        return String.valueOf(url.hashCode());
    }

}
