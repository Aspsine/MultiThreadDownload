package com.aspsine.multithreaddownload.demo.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.aspsine.multithreaddownload.demo.listener.OnItemClickListener;
import com.aspsine.multithreaddownload.demo.ui.activity.AppDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Aspsine on 2015/7/8.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AppInfo> mAppInfos;

    private OnItemClickListener mListener;

    public RecyclerViewAdapter() {
        this.mAppInfos = new ArrayList<AppInfo>();
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<AppInfo> appInfos) {
        this.mAppInfos.clear();
        this.mAppInfos.addAll(appInfos);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent,false);
        final AppViewHolder holder = new AppViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AppDetailActivity.class);
                intent.putExtra("EXTRA_APPINFO", mAppInfos.get(holder.getLayoutPosition()));
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindData((AppViewHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        return mAppInfos.size();
    }

    private void bindData(AppViewHolder holder, final int position) {
        final AppInfo appInfo = mAppInfos.get(position);
        holder.tvName.setText(appInfo.getName());
        holder.tvDownloadPerSize.setText(appInfo.getDownloadPerSize());
        holder.tvStatus.setText(appInfo.getStatusText());
        holder.progressBar.setProgress(appInfo.getProgress());
        holder.btnDownload.setText(appInfo.getButtonText());
        Picasso.with(holder.itemView.getContext()).load(appInfo.getImage()).into(holder.ivIcon);
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(v, position, appInfo);
                }
            }
        });
    }

    public static final class AppViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivIcon)
        public ImageView ivIcon;

        @Bind(R.id.tvName)
        public TextView tvName;

        @Bind(R.id.btnDownload)
        public Button btnDownload;

        @Bind(R.id.tvDownloadPerSize)
        public TextView tvDownloadPerSize;

        @Bind(R.id.tvStatus)
        public TextView tvStatus;

        @Bind(R.id.progressBar)
        public ProgressBar progressBar;

        public AppViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
