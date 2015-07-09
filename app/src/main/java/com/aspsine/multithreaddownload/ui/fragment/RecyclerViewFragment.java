package com.aspsine.multithreaddownload.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aspsine.multithreaddownload.DataSource;
import com.aspsine.multithreaddownload.R;
import com.aspsine.multithreaddownload.entity.AppInfo;
import com.aspsine.multithreaddownload.ui.adapter.RecyclerViewAdapter;

import org.apache.http.HttpConnection;

import java.net.HttpURLConnection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecyclerViewFragment extends Fragment {
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
        mAppInfos = DataSource.getInstance().getData();
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
}
