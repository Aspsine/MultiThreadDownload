package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.demo.DataSource;
import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.aspsine.multithreaddownload.demo.listener.OnItemClickListener;
import com.aspsine.multithreaddownload.demo.service.DownloadService;
import com.aspsine.multithreaddownload.demo.ui.adapter.ListViewAdapter;
import com.aspsine.multithreaddownload.demo.util.Utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends Fragment implements OnItemClickListener<AppInfo> {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    @Bind(R.id.listView)
    ListView listView;

    private List<AppInfo> mAppInfos;
    private ListViewAdapter mAdapter;

    private File mDownloadDir;


    public ListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadDir = new File(Environment.getExternalStorageDirectory(), "Download");
        mAdapter = new ListViewAdapter();
        mAdapter.setOnItemClickListener(this);
        mAppInfos = DataSource.getInstance().getData();
        for (AppInfo info : mAppInfos) {
            DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadProgress(info.getUrl());
            if (downloadInfo != null) {
                info.setProgress(downloadInfo.getProgress());
                info.setDownloadPerSize(getDownloadPerSize(downloadInfo.getFinished(), downloadInfo.getLength()));
                info.setStatus(AppInfo.STATUS_PAUSE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(mAdapter);
        mAdapter.setData(mAppInfos);
    }


    @Override
    public void onItemClick(View v, final int position, final AppInfo appInfo) {

        if (appInfo.getStatus() == AppInfo.STATUS_DOWNLOADING || appInfo.getStatus() == AppInfo.STATUS_CONNECTING) {
            if (isCurrentListViewItemVisible(position)) {
                pause(position, appInfo.getUrl());
            }
        } else if (appInfo.getStatus() == AppInfo.STATUS_COMPLETE) {
            if (isCurrentListViewItemVisible(position)) {
                install(appInfo);
            }
        } else if (appInfo.getStatus() == AppInfo.STATUS_INSTALLED) {
            if (isCurrentListViewItemVisible(position)) {
                unInstall(appInfo);
            }
        } else {
            if (isCurrentListViewItemVisible(position)) {
                download(position, appInfo.getUrl(), appInfo);
            }
        }
    }


    public class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra("EXTRA_STATUS", 0);
            switch (status) {

            }
        }


    }

    public void download(int position, String tag, AppInfo info) {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity().startService(intent);
    }

    public void pause(int position, String tag) {

    }

    public void pauseAll() {

    }

    public void install(AppInfo appInfo) {
        Utils.installApp(getActivity(), new File(mDownloadDir, appInfo.getName() + ".apk"));
    }

    public void unInstall(AppInfo appInfo) {
        Utils.unInstallApp(getActivity(), appInfo.getPackageName());
    }

    private boolean isCurrentListViewItemVisible(int position) {
        int first = listView.getFirstVisiblePosition();
        int last = listView.getLastVisiblePosition();
        return first <= position && position <= last;
    }

    private ListViewAdapter.ViewHolder getViewHolder(int position) {
        int childPosition = position - listView.getFirstVisiblePosition();
        View view = listView.getChildAt(childPosition);
        return (ListViewAdapter.ViewHolder) view.getTag();
    }

    private String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }
}
