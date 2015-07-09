package com.aspsine.multithreaddownload.util;

import android.content.Context;
import android.content.Intent;

import com.aspsine.multithreaddownload.entity.AppInfo;
import com.aspsine.multithreaddownload.ui.activity.AppDetailActivity;

/**
 * Created by Aspsine on 2015/7/9.
 */
public class IntentUtils {

    public static void IntentToList() {

    }

    public static void IntentToDetail(Context context, AppInfo appInfo) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra("EXTRA_APPINFO", appInfo);
        context.startActivity(intent);
    }
}
