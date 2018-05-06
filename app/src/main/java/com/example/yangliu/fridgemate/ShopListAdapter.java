package com.example.yangliu.fridgemate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yangliu.fridgemate.data.FridgeItem;

import java.util.List;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ItemViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView wordItemView;
        private CheckBox checkbox;
        //private final ProgressBar progressBar

        private ItemViewHolder(View itemView) {
            super(itemView);

            checkbox = itemView.findViewById(R.id.checkBox);
            wordItemView = itemView.findViewById(R.id.editText);

        }
    }

    private final LayoutInflater mInflater;
    public List<FridgeItem> mItems; // Cached copy of items' info
    private Context context;


    ShopListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ShopListAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.shoplist_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {


    }

    void setItems(List<FridgeItem> items){
        mItems = items;
        notifyDataSetChanged();
    }

    // TODO:: clear items


    //TODO:: list management
    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        else return 0;
    }
}