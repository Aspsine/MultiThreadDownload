package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.demo.DataSource;
import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.aspsine.multithreaddownload.demo.listener.OnItemClickListener;
import com.aspsine.multithreaddownload.demo.ui.adapter.RecyclerViewAdapter;
import com.aspsine.multithreaddownload.demo.util.Utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecyclerViewFragment extends Fragment implements OnItemClickListener<AppInfo> {
    private List<AppInfo> mAppInfos;
    private RecyclerViewAdapter mAdapter;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    public RecyclerViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecyclerViewAdapter();
        mAdapter.setOnItemClickListener(this);
        mAppInfos = DataSource.getInstance().getData();
        for (AppInfo info : mAppInfos) {
            DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadProgress(info.getUrl());
            if (downloadInfo != null) {
                info.setProgress(downloadInfo.getProgress());
                info.setDownloadPerSize(getDownloadPerSize(downloadInfo.getFinished(), downloadInfo.getLength()));
                info.setStatus(AppInfo.STATUS_PAUSED);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(mAppInfos);
    }

    @Override
    public void onPause() {
        super.onPause();
        DownloadManager.getInstance().pauseAll();
    }

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    /**
     * Dir: /Download
     */
    private final File mDownloadDir = new File(Environment.getExternalStorageDirectory(), "Download");

    @Override
    public void onItemClick(View v, int position, AppInfo appInfo) {
        if (appInfo.getStatus() == AppInfo.STATUS_DOWNLOADING || appInfo.getStatus() == AppInfo.STATUS_CONNECTING) {
            pause(appInfo.getUrl());
        } else if (appInfo.getStatus() == AppInfo.STATUS_COMPLETE) {
            install(appInfo);
        } else if (appInfo.getStatus() == AppInfo.STATUS_INSTALLED) {
            unInstall(appInfo);
        } else {
            download(position, appInfo.getUrl(), appInfo);
        }
    }

    private void download(final int position, String tag, final AppInfo appInfo) {
        final DownloadRequest request = new DownloadRequest.Builder()
                .setTitle(appInfo.getName() + ".apk")
                .setUri(appInfo.getUrl())
                .setFolder(mDownloadDir)
                .build();

        DownloadManager.getInstance().download(request, tag, new DownloadCallback(position, appInfo));
    }

    private void pause(String tag) {
        DownloadManager.getInstance().pause(tag);
    }

    private void install(AppInfo appInfo) {
        Utils.installApp(getActivity(), new File(mDownloadDir, appInfo.getName() + ".apk"));
    }

    private void unInstall(AppInfo appInfo) {
        Utils.unInstallApp(getActivity(), appInfo.getPackageName());
    }

    class DownloadCallback implements CallBack {
        private int mPosition;
        private AppInfo mAppInfo;

        public DownloadCallback(int position, AppInfo appInfo) {
            mAppInfo = appInfo;
            mPosition = position;
        }

        @Override
        public void onStarted() {

        }

        @Override
        public void onConnecting() {
            mAppInfo.setStatus(AppInfo.STATUS_CONNECTING);
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onConnected(long total, boolean isRangeSupport) {
            mAppInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onProgress(long finished, long total, int progress) {
            String downloadPerSize = getDownloadPerSize(finished, total);
            mAppInfo.setProgress(progress);
            mAppInfo.setDownloadPerSize(downloadPerSize);
            mAppInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvDownloadPerSize.setText(downloadPerSize);
                holder.progressBar.setProgress(progress);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onCompleted() {
            mAppInfo.setStatus(AppInfo.STATUS_COMPLETE);
            File apk = new File(mDownloadDir, mAppInfo.getName() + ".apk");
            if (apk.isFile() && apk.exists()) {
                String packageName = Utils.getApkFilePackage(getActivity(), apk);
                mAppInfo.setPackageName(packageName);
                if (Utils.isAppInstalled(getActivity(), packageName)) {
                    mAppInfo.setStatus(AppInfo.STATUS_INSTALLED);
                }
            }

            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onDownloadPaused() {
            mAppInfo.setStatus(AppInfo.STATUS_PAUSED);
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onDownloadCanceled() {
            mAppInfo.setStatus(AppInfo.STATUS_NOT_DOWNLOAD);
            mAppInfo.setDownloadPerSize("");
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.tvDownloadPerSize.setText("");
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        @Override
        public void onFailed(DownloadException e) {
            mAppInfo.setStatus(AppInfo.STATUS_DOWNLOAD_ERROR);
            mAppInfo.setDownloadPerSize("");
            if (isCurrentListViewItemVisible(mPosition)) {
                RecyclerViewAdapter.AppViewHolder holder = getViewHolder(mPosition);
                holder.tvStatus.setText(mAppInfo.getStatusText());
                holder.tvDownloadPerSize.setText("");
                holder.btnDownload.setText(mAppInfo.getButtonText());
            }
            e.printStackTrace();
        }
    }


    private RecyclerViewAdapter.AppViewHolder getViewHolder(int position) {
        return (RecyclerViewAdapter.AppViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
    }

    private boolean isCurrentListViewItemVisible(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        return first <= position && position <= last;
    }

    private String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }
}
