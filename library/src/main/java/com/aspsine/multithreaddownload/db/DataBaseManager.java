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

    public synchronized void delete(String tag) {
        mThreadInfoDao.delete(tag);
    }

    public synchronized void update(String tag, int threadId, long finished) {
        mThreadInfoDao.update(tag, threadId, finished);
    }

    public List<ThreadInfo> getThreadInfos(String tag) {
        return mThreadInfoDao.getThreadInfos(tag);
    }

    public List<ThreadInfo> getThreadInfos(){
        return mThreadInfoDao.getThreadInfos();
    }

    public boolean exists(String tag, int threadId) {
        return mThreadInfoDao.exists(tag, threadId);
    }
}
