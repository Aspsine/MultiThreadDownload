package com.aspsine.multithreaddownload.demo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.aspsine.multithreaddownload.demo.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnListView).setOnClickListener(this);
        findViewById(R.id.btnRecyclerView).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AppListActivity.class);
        switch (v.getId()) {
            case R.id.btnListView:
                intent.putExtra("EXTRA_TYPE", AppListActivity.TYPE.TYPE_LISTVIEW);
                break;
            case R.id.btnRecyclerView:
                intent.putExtra("EXTRA_TYPE", AppListActivity.TYPE.TYPE_RECYCLERVIEW);
                break;
            default:
                return;
        }
        startActivity(intent);
    }

}
