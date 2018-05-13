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

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ItemViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView name,status;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_view);
            imageView = itemView.findViewById(R.id.member_image);
            status = itemView.findViewById(R.id.status_view);
        }
    }


    private final LayoutInflater mInflater;
    private int currentFridge;
    private Context context;
    private String[] names;
    private String[] statuses;
    // TODO DATABASE:: set up images of members
    //private Bitmap[] images;

    MemberListAdapter(Context context) {
        this.context = context;
        currentFridge = SaveSharedPreference.getCurrentFridge(context);
        // TODO:: DATABASE set up the names, statuses, images of members

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fridge_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (currentFridge != -1) {
            // set up each member
            holder.name.setText(names[position]);
            holder.status.setText(statuses[position]);

            // TODO:: DATABASE set up user's image
            //holder.imageView.setImageBitmap(images[position]);
        }
        //holder.progressBar.setProgress();
        else {
            // Covers the case of data not being ready yet.
            holder.name.setText("No user");
        }
    }

    @Override
    public int getItemCount() {
        if (names != null)
            return names.length;
        return 0;
    }

    void setFridge(int item){
        currentFridge = item;
        notifyDataSetChanged();
    }
}