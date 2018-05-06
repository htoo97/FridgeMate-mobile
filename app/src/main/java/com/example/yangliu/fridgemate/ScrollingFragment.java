package com.example.yangliu.fridgemate;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.yangliu.fridgemate.data.FridgeItem;
import com.example.yangliu.fridgemate.data.FridgeItemViewModel;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ScrollingFragment extends Fragment implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{


    public static final String NAME_KEY = "name";
    public static final String DATE_KEY = "date";
    public static final String IMAGE_KEY = "img";
    public static final String Other_KEY = "other";
    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    private FridgeItemViewModel mItemViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    //private TextView mTextMessage
    // TODO:: add profile image


    // Setup any handles to view objects here
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        constraintLayout = view.findViewById(R.id.cl);
        // database <-> RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final ItemListAdapter adapter = new ItemListAdapter(view.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        //recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        // on item Click (modifying item)
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ScrollingFragment.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(view.getContext(), AddItemManual.class);
                Bundle extras = new Bundle();
                Log.d("edit item", "put " + adapter.mItems.get(position).getItemName() + " to the bundle");
                extras.putString(NAME_KEY,adapter.mItems.get(position).getItemName());
                Log.d("edit item", "put " + adapter.mItems.get(position).getExpDate() + " to the bundle");
                extras.putString(DATE_KEY,adapter.mItems.get(position).getExpDate());
                Log.d("edit item", "put its image to the bundle");
                extras.putByteArray(IMAGE_KEY,adapter.mItems.get(position).getImage());
                intent.putExtras(extras);
                getActivity().setResult(RESULT_OK, intent);
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

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddItemManual.class);
                startActivityForResult(intent, NEW_ITEM_ACTIVITY_REQUEST_CODE);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scrolling, container, false);
    }


    // Define what a swipe on an item does
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
                    .make(constraintLayout, item.getItemName() + " removed!", Snackbar.LENGTH_LONG);
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

    // having added/edited an item from the fab or modified
    @Override
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
        }
        else {
            Toast.makeText(
                    getContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }


}
