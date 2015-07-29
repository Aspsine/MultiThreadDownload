package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.core.DownloadException;
import com.aspsine.multithreaddownload.demo.DataSource;
import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.aspsine.multithreaddownload.demo.listener.OnItemClickListener;
import com.aspsine.multithreaddownload.demo.ui.adapter.ListViewAdapter;
import com.aspsine.multithreaddownload.demo.util.Utils;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.util.L;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends Fragment implements OnItemClickListener<AppInfo> {
    @Bind(R.id.listView)
    ListView listView;

    private List<AppInfo> mAppInfos;
    private ListViewAdapter mAdapter;

    private AppInstallBroadcastReceiver mReceiver;

    public ListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onDetach() {
        super.onDetach();
        removeBroadcastReceiver();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        registerBroadcastReceiver();
    }

    private void removeBroadcastReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }

    private void registerBroadcastReceiver() {
        mReceiver = new AppInstallBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    public class AppInstallBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                Log.i("App", intent.getDataString() + " add");
//                updateListViewItem(intent, AppInfo.STATUS_INSTALLED);
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                Log.i("App", intent.getDataString() + " removed");
//                updateListViewItem(intent, AppInfo.STATUS_NOT_DOWNLOAD);
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                Log.i("App", intent.getDataString() + " replace");
//                updateListViewItem(intent, AppInfo.STATUS_INSTALLED);
            }
        }

    }

    private void updateListViewItem(Intent intent, int status) {
        String packageName = intent.getDataString();
        int position = getPosition(packageName);
        if (position >= 0) {
            AppInfo appInfo = mAppInfos.get(position);
            appInfo.setStatus(status);
            appInfo.setProgress(0);
            if (isCurrentListViewItemVisible(position)) {
                ListViewAdapter.ViewHolder holder = getViewHolder(position);
                holder.btnDownload.setText(appInfo.getButtonText());
                holder.tvStatus.setText(appInfo.getStatusText());
                holder.progressBar.setProgress(appInfo.getProgress());
            }
        }
    }

    private int getPosition(String packageName) {
        int position = -1;

        for (int i = 0; i < mAppInfos.size(); i++) {
            String packName = mAppInfos.get(i).getPackageName();
            if (!TextUtils.isEmpty(packageName) || packName.equals(packageName)) {
                position = i;
                break;
            }
        }

        return position;
    }

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    /**
     * Dir: /Download
     */
    private final File dir = new File(Environment.getExternalStorageDirectory(), "Download");

    @Override
    public void onItemClick(View v, final int position, final AppInfo appInfo) {

        if (appInfo.getStatus() == AppInfo.STATUS_DOWNLOADING || appInfo.getStatus() == AppInfo.STATUS_CONNECTING) {
            if (isCurrentListViewItemVisible(position)) {
                DownloadManager.getInstance().pause(appInfo.getUrl());
            }
            return;
        } else if (appInfo.getStatus() == AppInfo.STATUS_COMPLETE) {
            if (isCurrentListViewItemVisible(position)) {
                Utils.installApp(getActivity(), new File(dir, appInfo.getName() + ".apk"));
            }
            return;
        } else if (appInfo.getStatus() == AppInfo.STATUS_INSTALLED) {
            if (isCurrentListViewItemVisible(position)) {
                Utils.unInstallApp(getActivity(), appInfo.getPackageName());
            }
        } else {
            download(position, appInfo);
        }
    }

    private void download(final int position, final AppInfo appInfo) {
        DownloadManager.getInstance().download(appInfo.getName() + ".apk", appInfo.getUrl(), dir, new CallBack() {

            @Override
            public void onDownloadStart() {
                appInfo.setStatus(AppInfo.STATUS_CONNECTING);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onConnected(int total, boolean isRangeSupport) {
                appInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onProgress(int finished, int total, int progress) {
                String downloadPerSize = getDownloadPerSize(finished, total);
                appInfo.setProgress(progress);
                appInfo.setDownloadPerSize(downloadPerSize);
                appInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvDownloadPerSize.setText(downloadPerSize);
                    holder.progressBar.setProgress(progress);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onComplete() {
                appInfo.setStatus(AppInfo.STATUS_COMPLETE);
                File apk = new File(dir, appInfo.getName() + ".apk");
                if (apk.isFile() && apk.exists()) {
                    String packageName = Utils.getApkFilePackage(getActivity(), apk);
                    appInfo.setPackageName(packageName);
                    if (Utils.isAppInstalled(getActivity(), packageName)) {
                        appInfo.setStatus(AppInfo.STATUS_INSTALLED);
                    }
                }

                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onDownloadPause() {
                appInfo.setStatus(AppInfo.STATUS_PAUSE);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onDownloadCancel() {
                appInfo.setStatus(AppInfo.STATUS_NOT_DOWNLOAD);
                appInfo.setDownloadPerSize("");
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.tvDownloadPerSize.setText("");
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
            }

            @Override
            public void onFailure(DownloadException e) {
                appInfo.setStatus(AppInfo.STATUS_DOWNLOAD_ERROR);
                appInfo.setDownloadPerSize("");
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvStatus.setText(appInfo.getStatusText());
                    holder.tvDownloadPerSize.setText("");
                    holder.btnDownload.setText(appInfo.getButtonText());
                }
                e.printStackTrace();
            }
        });
    }

    private String getDownloadPerSize(int finished, int total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
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
}
