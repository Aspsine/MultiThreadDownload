package com.aspsine.multithreaddownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by aspsine on 15-4-19.
 */
public abstract class AbstractDao<T> {
    private DBOpenHelper mHelper;

    public AbstractDao(Context context) {
        mHelper = new DBOpenHelper(context);
    }

    protected SQLiteDatabase getWritableDatabase() {
        return mHelper.getWritableDatabase();
    }

    protected SQLiteDatabase getReadableDatabase() {
        return mHelper.getReadableDatabase();
    }

    public void close() {
        mHelper.close();
    }
}
