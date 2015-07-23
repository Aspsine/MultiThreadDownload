package com.aspsine.multithreaddownload.demo.listener;

import android.view.View;

/**
 * Created by Aspsine on 2015/7/22.
 */
public interface OnItemClickListener<T> {
    void onItemClick(View v, int position, T t);
}
