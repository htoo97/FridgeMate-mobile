package com.example.yangliu.fridgemate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.yangliu.fridgemate.data.FridgeItem;

import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView itemImageView;
        private final TextView wordItemView;
        private final TextView dateItemView;
        //private final ProgressBar progressBar;
        public RelativeLayout viewBackground, viewForeground;

        private ItemViewHolder(View itemView) {
            super(itemView);

            //progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            wordItemView = itemView.findViewById(R.id.name_view);
            dateItemView = itemView.findViewById(R.id.date_view);

            itemImageView = itemView.findViewById(R.id.item_image);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }
    }

    private final LayoutInflater mInflater;
    public List<FridgeItem> mItems; // Cached copy of items' info
    private Context context;


    ItemListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (mItems != null) {
            FridgeItem current = mItems.get(position);

            // set date
            String expDate = current.getExpDate();
            if (expDate != null)
                holder.dateItemView.setText(expDate);

            // set image
            byte[] imageByte = current.getImage();
            if (imageByte != null) {
                //Bitmap b = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                //holder.itemImageView.setImageBitmap(b);
                // TODO:: set image itemImageView size here
                //holder.itemImageView.setImageBitmap(Bitmap.createScaledBitmap(b, 60, 60, false));

                // maybe change to glide for efficiency
                Glide.with(context).load(imageByte).centerCrop()
                        .into(holder.itemImageView);


            }
            // set name
            holder.wordItemView.setText(current.getItemName());

            //TODO:: base of the progress bar for now is 10 days
            //holder.progressBar.setProgress();
        } else {
            // Covers the case of data not being ready yet.
            holder.wordItemView.setText("No Word");
        }

    }
    // TODO::
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