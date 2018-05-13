package com.example.yangliu.fridgemate.fridge_family;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yangliu.fridgemate.Fridge;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.Fridge;

import java.util.List;

public class FridgeListAdapter extends RecyclerView.Adapter<FridgeListAdapter.ItemViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout frame;
        private final ImageView imageView;
        private final TextView name;
        private final Drawable unpressed;
        private final Drawable pressed;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_view);
            imageView = itemView.findViewById(R.id.imageView);

            // on select stroker
            frame = itemView.findViewById(R.id.fridgeFamilyCanvas);
            unpressed  = itemView.getResources().getDrawable(R.drawable.round_button);
            pressed = itemView.getResources().getDrawable(R.drawable.round_button_pressed);
        }
    }

    public int selectedItemPos = -1;

    private final LayoutInflater mInflater;
    public List<Fridge> mFridges;
    private Context context;


    FridgeListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        selectedItemPos = SaveSharedPreference.getCurrentFridge(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fridge_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (mFridges != null) {
            Fridge current = mFridges.get(position);

            // focus on the current fridge
            if (position != selectedItemPos) {
                holder.frame.setBackground(holder.unpressed);
            }
            else{
                holder.frame.setBackground(holder.pressed);
            }
            // set name by name
            String name = current.getFridgeName();
            if (name!= null)
                holder.name.setText(name);
            else {// set name by id
                holder.name.setText(current.getFridgeid());
            }
        }
        //holder.progressBar.setProgress();
         else {
            // Covers the case of data not being ready yet.
            holder.name.setText("No String");
        }
    }

    void setItems(List<Fridge> items){
        mFridges = items;
        notifyDataSetChanged();
    }

    //TODO:: list management
    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mFridges != null)
            return mFridges.size();
        else return 0;
    }
}