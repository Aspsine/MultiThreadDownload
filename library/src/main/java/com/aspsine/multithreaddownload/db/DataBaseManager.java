package com.aspsine.multithreaddownload.db;

import android.content.Context;

import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class DataBaseManager {
    private static DataBaseManager sDataBaseManager;
    private final ThreadInfoDao mThreadInfoDao;

    public static DataBaseManager getInstance(Context context) {
        if (sDataBaseManager == null) {
            sDataBaseManager = new DataBaseManager(context);
        }
        return sDataBaseManager;
    }

    private DataBaseManager(Context context) {
        mThreadInfoDao = new ThreadInfoDao(context);
    }

    public synchronized void insert(ThreadInfo threadInfo) {
        mThreadInfoDao.insert(threadInfo);
    }

    public synchronized void delete(String url) {
        mThreadInfoDao.delete(url);
    }

    public synchronized void update(String url, int threadId, long finished) {
        mThreadInfoDao.update(url, threadId, finished);
    }

    public List<ThreadInfo> getThreadInfos(String url) {
        return mThreadInfoDao.getThreadInfos(url);
    }

    public boolean exists(String url, int threadId) {
        return mThreadInfoDao.exists(url, threadId);
    }
}
