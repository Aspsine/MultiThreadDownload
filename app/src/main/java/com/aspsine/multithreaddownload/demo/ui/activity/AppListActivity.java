package com.aspsine.multithreaddownload.demo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

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

//        int type = intent.getIntExtra("EXTRA_TYPE", 0);
        int type = 1;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
