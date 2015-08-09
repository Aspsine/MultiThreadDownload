package com.aspsine.multithreaddownload;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.aspsine.multithreaddownload.core.DownloadRequest;
import com.aspsine.multithreaddownload.core.DownloadStatus;
import com.aspsine.multithreaddownload.core.DownloadStatusDelivery;
import com.aspsine.multithreaddownload.core.DownloadStatusDeliveryImpl;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.L;

import java.io.File;
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
        mDownloadRequestMap = new LinkedHashMap<String, DownloadRequest>();
    }

    public void init(DownloadConfiguration configuration) {
        if (configuration == null) {
            throw new RuntimeException("configuration can not be null!");
        }
        this.mConfig = configuration;
        mExecutorService = Executors.newFixedThreadPool(configuration.maxThreadNum);
        mDelivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));

        mDBManager = DataBaseManager.getInstance(configuration.context);
    }

    /**
     * core method: download a file using a http/https url.
     *
     * @param fileName  the file's name.
     * @param url       http or https download url
     * @param callBack  {@link CallBack} of download
     */
    public void download(String fileName, String url, File dir, CallBack callBack) {
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
            L.i("DownloadManager", "use cached request");
            request = mDownloadRequestMap.get(tag);
        } else {
            L.i("DownloadManager", "use new request");
            if (dir == null) {
                dir = mConfig.downloadDir;
            }
            downloadInfo = new DownloadInfo(fileName, url, dir);
            request = new DownloadRequest(downloadInfo, mDBManager, mExecutorService, new DownloadStatus(), mDelivery);
            mDownloadRequestMap.put(tag, request);
        }
        if (!request.isStarted()) {
            request.start(callBack);
        } else {
            L.i("DownloadManager", fileName + " : has started!");
        }
    }

    /**
     * <p>Core method: pause the downloading task.
     *
     * <p>Pause the downloading task and record the progress data in database.
     * Once you invoke{@link #download(String, String, File, CallBack)} method again,
     * the task will automatically continue downloading. The task will be resumed from
     * the exactly progress you had paused.
     *
     * @param url the url of the download task you want to pause
     */
    public void pause(String url) {
        String tag = createTag(url);
        DownloadRequest request = mDownloadRequestMap.get(tag);
        if (request != null) {
            request.pause();
        } else {
            L.i("DownloadManager", "pause " + url + " request == null");
        }
    }

    /**
     * <p>Core method: cancel the download task.
     *
     * <p>The difference between {@link #pause(String url)} and {@link #cancel(String url)}
     * is that {@link #cancel(String url)} release the reference of the thread task, and
     * {@link #cancel(String url)} will delete the unfinished file created in the download
     * path you have configured in {@link DownloadConfiguration#setDownloadDir(File)} and
     * delete the download progress data in database.
     *
     * <p>Note: if your downloading task is connecting the server you can only invoke {@link #cancel(String url)}
     * to cancel {@link com.aspsine.multithreaddownload.core.ConnectTask} task.
     *
     * @param url the url of the download task you want to cancel
     */
    public void cancel(String url) {
        String tag = createTag(url);
        DownloadRequest request = mDownloadRequestMap.get(tag);
        if (request != null) {
            request.cancel();
        } else {
            L.i("DownloadManager", "cancel " + url + " request == null");
        }
        mDownloadRequestMap.remove(tag);
    }

    public DownloadInfo getDownloadProgress(String url) {
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(url);
        DownloadInfo downloadInfo = null;
        if (!threadInfos.isEmpty()) {
            int finished = 0;
            int progress = 0;
            int total = 0;
            for (ThreadInfo info : threadInfos) {
                finished += info.getFinished();
                total += (info.getEnd() - info.getStart());
            }
            progress = (int) ((long) finished * 100 / total);
            downloadInfo = new DownloadInfo();
            downloadInfo.setFinished(finished);
            downloadInfo.setLength(total);
            downloadInfo.setProgress(progress);
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
