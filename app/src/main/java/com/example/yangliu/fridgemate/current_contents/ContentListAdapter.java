package com.example.yangliu.fridgemate.current_contents;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContentListAdapter extends RecyclerView.Adapter<ContentListAdapter.ItemViewHolder> {


    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView itemImageView;
        private final TextView wordItemView;
        private final TextView dateItemView;
        private final TextView freshDays;
        private ProgressBar progressBar;
        public RelativeLayout viewForeground;

        private ItemViewHolder(View itemView) {
            super(itemView);

            freshDays = itemView.findViewById(R.id.freshDays);
            wordItemView = itemView.findViewById(R.id.name_view);
            dateItemView = itemView.findViewById(R.id.date_view);

            itemImageView = itemView.findViewById(R.id.item_image);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private final LayoutInflater mInflater;
    public List<FridgeItem> mItems, mItemsOnDisplay;
    private Context context;
    int today;
    public ContentListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        today = (int) (System.currentTimeMillis() / (86400000));
    }

    public void filterList(CharSequence keyWord){
        List<FridgeItem> filteredList = new LinkedList<FridgeItem>();

        if (mItems != null) {
            if (keyWord == null || keyWord.length() == 0) {
                // set the display item as everything
                mItemsOnDisplay = mItems;
            } else {
                String constraint = (keyWord.toString().toLowerCase());
                for (int i = 0; i < mItems.size(); i++) {
                    String data = (mItems.get(i)).getItemName().toLowerCase();
                    if (data.startsWith(constraint)) {
                        filteredList.add(new FridgeItem(mItems.get(i)));
                    }
                }
                mItemsOnDisplay = filteredList;
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fridge_content_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        if (mItemsOnDisplay != null) {
            FridgeItem current = mItemsOnDisplay.get(position);

            // set date
            String expDate = current.getExpDate();
            if (expDate != null && !expDate.equals(""))
                holder.dateItemView.setText(expDate);

            // set image
            Uri imageUri = current.getImage();
            if (imageUri != null && !String.valueOf(imageUri).equals("null") && !String.valueOf(imageUri).equals("")) {
//                holder.itemImageView.setImageURI(imageByte);
                Glide.with(context).load(imageUri).centerCrop()
                        .into(holder.itemImageView);

            } else{
                // avoic using Glide cache issue
                holder.itemImageView.setImageResource(R.color.white);
            }
            // set name
            holder.wordItemView.setText(current.getItemName());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date strDate = null;
            // if item has a expdate
            if (expDate.length() != 0) {
                try {
                    strDate = sdf.parse(expDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (strDate != null) {
                int dayDiff = (int) (strDate.getTime() / (86400000)) - today;
                ProgressBarAnimation anim = new ProgressBarAnimation(holder.progressBar, 0, (int)(dayDiff * 7.2));
                anim.setDuration(1000);
                holder.progressBar.startAnimation(anim);
                if (dayDiff < 0) {
                    holder.progressBar.setProgress(0);
                    holder.freshDays.setText("Expired");
                }
                else {
                    holder.progressBar.setProgress((int) (dayDiff * 7.2));
                    if (dayDiff > 1)
                        holder.freshDays.setText(String.valueOf(dayDiff) + " Days");
                    else
                        holder.freshDays.setText("1 Day");
                }
            }
            else{
                ProgressBarAnimation anim = new ProgressBarAnimation(holder.progressBar, 50, 100);
                anim.setDuration(1000);
                holder.progressBar.startAnimation(anim);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.wordItemView.setText("No Word");
        }

    }

    FridgeItem lastremoved;

    public void remove(int position){
        lastremoved = mItemsOnDisplay.get(position);
        // locate the item in the original list (if removing when searching)
        int index = mItems.indexOf(lastremoved);
        mItemsOnDisplay.remove(position);
        //mItems.remove(index);
        notifyDataSetChanged();
    }

    public void restore(){
        mItems.add(lastremoved);
        Collections.sort(mItems);
        mItemsOnDisplay = mItems;
    }

    // adding an item
    public void addNonExpiringItem(FridgeItem i) {
        mItems.add(i);
        notifyDataSetChanged();
    }

    void setItems(List<FridgeItem> items){
        Collections.sort(items);
        mItems = items;
        mItemsOnDisplay = items;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mItemsOnDisplay != null)
            return mItemsOnDisplay.size();
        else return 0;
    }
}