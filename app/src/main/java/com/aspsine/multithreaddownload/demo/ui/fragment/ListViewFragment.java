package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    public ListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ListViewAdapter();
        mAdapter.setOnItemClickListener(this);
        mAppInfos = DataSource.getInstance().getData();
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(mAdapter);
        mAdapter.setData(mAppInfos);
    }

    private static final DecimalFormat DF = new DecimalFormat("0.00");


    @Override
    public void onItemClick(View v, final int position, final AppInfo appInfo) {
        if (appInfo.getStatus() == AppInfo.STATUS_DOWNLOADING){
            if (isCurrentListViewItemVisible(position)) {
                DownloadManager.getInstance().pause(appInfo.getUrl());
            }
            return;
        }

        appInfo.setStatus(AppInfo.STATUS_CONNECTING);
        if (isCurrentListViewItemVisible(position)) {
            ListViewAdapter.ViewHolder holder = getViewHolder(position);
            holder.tvStatus.setText(appInfo.getStatusText());
            holder.btnDownload.setText(appInfo.getButtonText());
        }

        DownloadManager.getInstance().download(appInfo.getName(), appInfo.getUrl(), new CallBack() {
            @Override
            public void onConnected(int total, boolean isRangeSupport) {
                String downloadPerSize = getDownloadPerSize(0, total);
                appInfo.setDownloadPerSize(downloadPerSize);
                appInfo.setStatus(AppInfo.STATUS_DOWNLOADING);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    holder.tvDownloadPerSize.setText(downloadPerSize);
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
                appInfo.setStatus(AppInfo.STATUS_NOT_DOWNLOAD);
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
