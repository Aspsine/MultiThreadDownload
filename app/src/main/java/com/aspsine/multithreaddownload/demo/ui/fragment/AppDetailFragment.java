package com.aspsine.multithreaddownload.demo.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.entity.AppInfo;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppDetailFragment extends Fragment {

    private AppInfo mAppInfo;

    @Bind(R.id.ivIcon)
    ImageView ivIcon;

    @Bind(R.id.tvName)
    TextView tvName;

    @Bind(R.id.btnDownload)
    Button btnDownload;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    public static AppDetailFragment newInstance(AppInfo appInfo) {
        AppDetailFragment fragment = new AppDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("EXTRA_APPINFO", appInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    public AppDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mAppInfo = (AppInfo) bundle.getSerializable("EXTRA_APPINFO");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Picasso.with(getActivity()).load(mAppInfo.getImage()).into(ivIcon);
        tvName.setText(mAppInfo.getName());
        progressBar.setVisibility(View.GONE);
    }
}
