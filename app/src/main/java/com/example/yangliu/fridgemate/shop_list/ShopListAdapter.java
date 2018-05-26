package com.example.yangliu.fridgemate.shop_list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.yangliu.fridgemate.R;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ShopListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private ImageButton delete;
        private TextView amount;
        private CheckBox selectedItem;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name= itemView.findViewById(R.id.shop_list_item_name);
            amount = itemView.findViewById(R.id.amount);
            delete = itemView.findViewById(R.id.delete_shop_item);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    removeItem(pos);
                }
            });
            selectedItem = itemView.findViewById(R.id.select_item);
            selectedItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(selectedItem.isChecked())
                        mSelectedItems.set(pos,true);
                    else
                        mSelectedItems.set(pos,false);
                }
            });

        }
    }

    private final LayoutInflater mInflater;
    private List<Pair<String,Integer>> mShopList;
    private Context context;
    private List<Boolean> mSelectedItems;
    int sumAmount = 0;

    ShopListAdapter(Context context) {
        setItems();
        this.context = context;
        mSelectedItems = new ArrayList<>(getItemCount());
        mInflater = LayoutInflater.from(context);

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.shop_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder iholder = (ItemViewHolder) holder;
        if (mShopList != null) {
            Pair<String, Integer> current = mShopList.get(position);
            iholder.name.setText(current.first);
            iholder.amount.setText(String.valueOf(current.second));
        }
    }

    void addSelectedToFridge(){
        for (int i = 0; i < getItemCount(); i++){
            if (mSelectedItems.get(i) == true){
                // TODO:: DATABASE:: add this to the fridge database

                removeItem(i);
            }
        }

        notifyDataSetChanged();
    }


    void setItems(){
        // TODO:: DATABASE Sync the shoplist on database with the local
        // TOOD:: also update sumAmount
        notifyDataSetChanged();
    }
    void addItem(String name, Integer amount){
        // TODO:: DATABASE: Add to the database
    }
    void removeItem(int pos){
        //TODO:: DATABASE: remove this item
    }

    @Override
    public int getItemCount() {
        if (mShopList != null)
            return mShopList.size();
        else return 0;
    }
}