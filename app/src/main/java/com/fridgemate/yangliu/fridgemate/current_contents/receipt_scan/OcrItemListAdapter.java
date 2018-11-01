package com.fridgemate.yangliu.fridgemate.current_contents.receipt_scan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fridgemate.yangliu.fridgemate.R;

import java.util.LinkedList;
import java.util.List;

public class OcrItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Button cancelBtn;
        private final TextView ocrItemName;

        private ItemViewHolder(View itemView) {
            super(itemView);
            ocrItemName = itemView.findViewById(R.id.ocr_item);
            cancelBtn = itemView.findViewById(R.id.delete_ocr_item);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {}
    }

    public List<String> mData;
    private final LayoutInflater mInflater;

    OcrItemListAdapter(Context context) {
        mData = new LinkedList<>();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.ocr_item, parent, false);
        return new OcrItemListAdapter.ItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (mData != null) {
            if (position <= mData.size() - 1){
                ItemViewHolder itemHolder = (ItemViewHolder)holder;
                ((ItemViewHolder) holder).ocrItemName.setText(mData.get(position));
            }

        }
    }

    void setItems(List<String> items){
        mData = items;
        notifyDataSetChanged();
    }

    void addItem(String name){
        mData.add(name);
        notifyDataSetChanged();
    }

    void removeItem(int index){
        if (index < mData.size()) {
            mData.remove(index);
            notifyDataSetChanged();
        }
    }

    void clearItems(){
        mData.clear();
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mData != null || mData.size() != 0)
            return mData.size();
        else return 0;
    }
}