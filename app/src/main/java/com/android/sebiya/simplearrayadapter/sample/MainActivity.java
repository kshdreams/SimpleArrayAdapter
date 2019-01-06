package com.android.sebiya.simplearrayadapter.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.sebiya.simplearrayadapter.AbsArrayAdapter;
import com.android.sebiya.simplearrayadapter.AbsArrayAdapter.HeaderViewListener;
import com.android.sebiya.simplearrayadapter.SimpleArrayAdapter;
import com.android.sebiya.simplearrayadapter.SpanSize;
import com.android.sebiya.simplearrayadapter.selectmode.MultipleSelectMode;
import com.android.sebiya.simplearrayadapter.selectmode.SelectModeCallback;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private static final int HEADER_TYPE_1 = 1;
    private static final int HEADER_TYPE_2 = 2;

    public static class Item {
        String name;
        String desc;
        public static Item create(String name, String desc) {
            Item item = new Item();
            item.name = name;
            item.desc = desc;
            return item;
        }
    }

    private SimpleArrayAdapter<Item> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        setSupportActionBar(toolbar);

        mAdapter = SimpleArrayAdapter.<Item>with(this)
                .setLayoutResId(R.layout.grid_item_2_line)
                .addViewBinder(R.id.text1, new AbsArrayAdapter.AbsViewBinder<Item, TextView>() {
                    @Override
                    protected void bindView(TextView textView, Item item) {
                        textView.setText(item.name);
                    }
                })
                .addViewBinder(R.id.text2, new AbsArrayAdapter.AbsViewBinder<Item, TextView>() {
                    @Override
                    protected void bindView(TextView textView, Item item) {
                        textView.setText(item.desc);
                    }
                })
                .withItemClickListener(new AbsArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this, mAdapter.getItemByAdapterPosition(position).name,
                                Toast.LENGTH_SHORT).show();
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
                .withSpanSize(new SpanSize() {
                    @Override
                    public int getSpanSize(final Adapter adapter, final int position, final int layoutSpanSize) {
                        if (adapter instanceof AbsArrayAdapter) {
                            return ((AbsArrayAdapter) adapter).isHeader(position) ? layoutSpanSize : 1;
                        }
                        return 1;
                    }
                })
                .build();

        for (int index = 1; index <= 4; index++) {
            mAdapter.addItem(Item.create("Text " + index, "description here"));
        }

        mAdapter.addHeaderView(0, HEADER_TYPE_1, R.layout.view_header, new HeaderViewListener() {
            @Override
            public void onCreateHeaderView(View view, int type) {
                Log.d(LOG_TAG, "onCreateHeaderView. type - " + type);
            }

            @Override
            public void onBindHeaderView(View view, int type) {
                Log.d(LOG_TAG, "onBindHeaderView. type - " + type);
            }
        });

        View header2 = View.inflate(this, R.layout.view_header_2, null);
        mAdapter.addHeaderView(7, HEADER_TYPE_2, header2);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                return mAdapter.getSpanSize(mAdapter, position, layoutManager.getSpanCount());
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.removeHeaderViewByType(HEADER_TYPE_2);
                Snackbar.make(view, "Header type 2 is removed", Snackbar.LENGTH_LONG)
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
            mAdapter.addItem(Item.create("New Text " + (mAdapter.getItemCount()), "description here"));
            return true;
        } else if (id == R.id.action_remove) {
            mAdapter.removeItem(mAdapter.getItem(0));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
