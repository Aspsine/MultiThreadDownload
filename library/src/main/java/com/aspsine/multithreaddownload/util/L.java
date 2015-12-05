package com.aspsine.multithreaddownload.util;

import android.util.Log;

import com.aspsine.multithreaddownload.Constants;


/**
 * Created by Aspsine on 2015/4/30.
 */
public class L {
    private static final String TAG = "MultiThreadDownload";

    /**
     * d
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (Constants.CONFIG.DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * d
     *
     * @param msg
     */
    public static void d(String msg) {
        d(TAG, msg);
    }

    /**
     * i
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (Constants.CONFIG.DEBUG) {
            Log.i(tag, msg);
        }
    }

    /**
     * i
     *
     * @param msg
     */
    public static void i(String msg) {
        i(TAG, msg);
    }

    /**
     * e
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (Constants.CONFIG.DEBUG) {
            Log.e(tag, msg);
        }
    }

    /**
     * w
     *
     * @param msg
     */
    public static void w(String msg) {
        w(TAG, msg);
    }

    /**
     * w
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (Constants.CONFIG.DEBUG) {
            Log.w(tag, msg);
        }
    }
}
