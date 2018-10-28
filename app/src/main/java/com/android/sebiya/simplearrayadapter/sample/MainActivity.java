package com.android.sebiya.simplearrayadapter.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sebiya.simplearrayadapter.AbsArrayAdapter;
import com.android.sebiya.simplearrayadapter.SimpleArrayAdapter;
import com.android.sebiya.simplearrayadapter.selectmode.MultipleSelectMode;
import com.android.sebiya.simplearrayadapter.selectmode.SelectModeCallback;

public class MainActivity extends AppCompatActivity {

    private SimpleArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        setSupportActionBar(toolbar);

        mAdapter = SimpleArrayAdapter.<String>with(this)
                .setLayoutResId(R.layout.list_item_2_line)
                .addViewBinder(R.id.text1, new AbsArrayAdapter.AbsViewBinder<String, TextView>() {
                    @Override
                    protected void bindView(TextView textView, String s) {
                        textView.setText(s);
                    }
                })
                .withItemClickListener(new AbsArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int i) {
                        Toast.makeText(MainActivity.this, mAdapter.getItem(i), Toast.LENGTH_SHORT).show();
                    }
                })
                .withSelectMode(R.id.check_box, new MultipleSelectMode(
                        new SelectModeCallback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                menu.add("Delete");
                                return true;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                                long[] selectedItems = mAdapter.getSelectedItemIds();
                                mAdapter.removeItemById(selectedItems);
                                return false;
                            }
                        }))
                .build();

        for (int index = 1; index <= 4; index++) {
            mAdapter.addItem("Text " + index);
        }

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            mAdapter.addItem("New Text " + (mAdapter.getItemCount() + 1));
            return true;
        } else if (id == R.id.action_remove) {
            mAdapter.removeItemAtPosition(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
