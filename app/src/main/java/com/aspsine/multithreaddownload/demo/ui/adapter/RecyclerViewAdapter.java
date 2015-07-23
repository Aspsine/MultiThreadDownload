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

    public RecyclerViewAdapter() {
        this.mAppInfos = new ArrayList<>();
    }

    public void setData(List<AppInfo> appInfos) {
        this.mAppInfos.clear();
        this.mAppInfos.addAll(appInfos);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, null);
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

    private void bindData(AppViewHolder holder, int position) {
        AppInfo appInfo = mAppInfos.get(position);
        holder.tvName.setText(appInfo.getName());
        Picasso.with(holder.itemView.getContext()).load(appInfo.getImage()).into(holder.ivIcon);
        holder.progressBar.setProgress(position * 5);
    }

    public static final class AppViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivIcon)
        ImageView ivIcon;
        @Bind(R.id.tvName)
        TextView tvName;
        @Bind(R.id.btnDownload)
        Button btnDownload;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;

        public AppViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
