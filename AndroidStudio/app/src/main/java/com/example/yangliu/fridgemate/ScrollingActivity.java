package com.example.yangliu.fridgemate;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.data.AppDatabase;
import com.example.yangliu.fridgemate.data.FridgeItem;
import com.example.yangliu.fridgemate.data.FridgeItemViewModel;

import java.util.Collections;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{


    public static final String NAME_KEY = "name";
    public static final String DATE_KEY = "date";
    public static final String IMAGE_KEY = "img";
    public static final String Other_KEY = "other";
    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    private Handler handler;

    private FridgeItemViewModel mItemViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout cl;
    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);


        cl = findViewById(R.id.cl);
        // database <-> RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final ItemListAdapter adapter = new ItemListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        // on item Click (modifying item)
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ScrollingActivity.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ScrollingActivity.this, AddItemManual.class);
                Bundle extras = new Bundle();
                Log.d("edit item", "put " + adapter.mItems.get(position).getItemName() + " to the bundle");
                extras.putString(NAME_KEY,adapter.mItems.get(position).getItemName());
                Log.d("edit item", "put " + adapter.mItems.get(position).getExpDate() + " to the bundle");
                extras.putString(DATE_KEY,adapter.mItems.get(position).getExpDate());
                Log.d("edit item", "put its image to the bundle");
                extras.putByteArray(IMAGE_KEY,adapter.mItems.get(position).getImage());
                intent.putExtras(extras);
                setResult(RESULT_OK, intent);
                startActivityForResult(intent,EDIT_ITEM_ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));


        // swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FridgeItemTouchHelper(0,
                ItemTouchHelper.LEFT, (FridgeItemTouchHelper.FridgeItemTouchHelpListener) this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        // view model persists even when app is destroyed
        mItemViewModel = ViewModelProviders.of(this).get(FridgeItemViewModel.class);
        mItemViewModel.getAllItems().observe(this, new Observer<List<FridgeItem>>() {
            @Override
            public void onChanged(@Nullable final List<FridgeItem> items) {
                // Update the cached copy of the items in the adapter.
                adapter.setItems(items);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO:: refresh adapter set data to something

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        // set color
        swipeRefreshLayout.setColorSchemeResources(
                R.color.blue,       //This method will rotate
                R.color.red,        //colors given to it when
                R.color.yellow,     //loader continues to
                R.color.green);     //refresh.
        //setSize() Method Sets The Size Of Loader
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        //Below Method Will set background color of Loader
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScrollingActivity.this, AddItemManual.class);
                startActivityForResult(intent, NEW_ITEM_ACTIVITY_REQUEST_CODE);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });


        //bottom navigation
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // having added/edited an item from the fab or modified
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            // fetch results
            Bundle extras = data.getExtras();
            String name_string = extras.getString(NAME_KEY);
            String date_string = extras.getString(DATE_KEY);
            byte[] imageByte = extras.getByteArray(IMAGE_KEY);
            FridgeItem item = new FridgeItem(name_string, date_string, imageByte);

            if (requestCode == NEW_ITEM_ACTIVITY_REQUEST_CODE) {
                mItemViewModel.insert(item);
            } else if (requestCode == EDIT_ITEM_ACTIVITY_REQUEST_CODE) {
                mItemViewModel.updateItemInfo(item);
            }
            } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                //TODO:: log out
                SaveSharedPreference.clearUserName(ScrollingActivity.this);
                Intent i = new Intent(ScrollingActivity.this, LoginActivity.class);
                startActivity(i);
                return true;
            case R.id.action_settings:
                //TODO::settings
                return true;
            case R.id.menu_refresh:

                //TODO: refresh here
                final ItemListAdapter adapter = new ItemListAdapter(this);
                adapter.notifyDataSetChanged();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Define what a wipe does
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ItemListAdapter.ItemViewHolder) {
            // get the removed item name to display it in snack bar
            List<FridgeItem> items = mItemViewModel.getAllItems().getValue();
            final FridgeItem item = items.get(viewHolder.getAdapterPosition());

            // backup of removed item for undo purpose
            final FridgeItem deletedItem = items.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            mItemViewModel.removeItem(item);

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(cl, item.getItemName() + " removed!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    mItemViewModel.restoreItem(item);
                }
            });
            snackbar.show();
        }
    }

    // bottom view navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.current_fridge:
                    // TODO: change color and go to activity

                    return true;
                case R.id.navigation_dashboard:

                    return true;
                case R.id.shopping_list:

                    return true;
            }
            return false;
        }
    };

}
