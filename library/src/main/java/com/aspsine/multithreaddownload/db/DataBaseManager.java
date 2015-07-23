package com.aspsine.multithreaddownload.db;

import android.content.Context;

import com.aspsine.multithreaddownload.entity.ThreadInfo;

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

    public void insert(ThreadInfo threadInfo) {
        mThreadInfoDao.insert(threadInfo);
    }

    public void delete(String url) {
        mThreadInfoDao.delete(url);
    }

    public void update(String url, int threadId, int finished) {
        mThreadInfoDao.update(url, threadId, finished);
    }

    public List<ThreadInfo> getThreadInfos(String url) {
        return mThreadInfoDao.getThreadInfos(url);
    }

    public boolean exists(String url, int threadId) {
        return mThreadInfoDao.exists(url, threadId);
    }
}
