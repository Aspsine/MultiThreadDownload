package com.aspsine.multithreaddownload.demo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.aspsine.multithreaddownload.demo.R;
import com.aspsine.multithreaddownload.demo.ui.fragment.ListViewFragment;
import com.aspsine.multithreaddownload.demo.ui.fragment.RecyclerViewFragment;

public class AppListActivity extends AppCompatActivity {

    public static final class TYPE {
        public static final int TYPE_LISTVIEW = 0;
        public static final int TYPE_RECYCLERVIEW = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        Intent intent = getIntent();
        int type = intent.getIntExtra("EXTRA_TYPE", TYPE.TYPE_LISTVIEW);

        if (savedInstanceState == null) {
            Fragment fragment =
                    type == TYPE.TYPE_LISTVIEW ?
                            new ListViewFragment() : new RecyclerViewFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }

        getSupportActionBar().setTitle(type == TYPE.TYPE_LISTVIEW ? "ListView Demo" : "RecyclerView Demo");
    }
}
