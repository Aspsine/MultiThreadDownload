package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadManager;
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

        DownloadManager.getInstance().download(appInfo.getName(), appInfo.getUrl(), new CallBack() {
            @Override
            public void onConnected(int total, boolean isRangeSupport) {
                String downloadPerSize = getDownloadPerSize(0, total);
                appInfo.setDownloadPerSize(downloadPerSize);
                appInfo.setStatus("");
                if (isCurrentListViewItemVisible(position)) {
                    TextView tvDownloadPerSize = getViewHolder(position).tvDownloadPerSize;
                    tvDownloadPerSize.setText(downloadPerSize);
                }
            }

            @Override
            public void onProgress(int finished, int total, int progress) {
                String downloadPerSize = getDownloadPerSize(finished, total);
                appInfo.setProgress(progress);
                appInfo.setDownloadPerSize(downloadPerSize);
                if (isCurrentListViewItemVisible(position)) {
                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
                    TextView tvDownloadPerSize = holder.tvDownloadPerSize;
                    tvDownloadPerSize.setText(downloadPerSize);
                    ProgressBar pb = holder.progressBar;
                    pb.setProgress(progress);
                }
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onDownloadPause() {

            }

            @Override
            public void onDownloadCancel() {

            }

            @Override
            public void onFailure(Exception e) {

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
