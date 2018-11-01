package com.fridgemate.yangliu.fridgemate.current_contents;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fridgemate.yangliu.fridgemate.R;

import java.util.LinkedList;
import java.util.List;

class RecipeSuggestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private final TextView name;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            imageView = itemView.findViewById(R.id.recipeImg);
        }

        @Override
        public void onClick(View v) {}
    }

    private List<RecipeItem> mData;
    private LayoutInflater mInflater;
    private Context context;

    RecipeSuggestionAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mData = new LinkedList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recipe_list_item, parent, false);
        return new RecipeSuggestionAdapter.ItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (mData != null) {
            if (position < mData.size()){
                ItemViewHolder itemHolder = (ItemViewHolder)holder;
                itemHolder.name.setText(mData.get(position).getItemName());
                String imgUri = mData.get(position).getImageUri();
                if (imgUri != null && !imgUri.equals("")) {
                    Glide.with(context).load(Uri.parse(imgUri)).centerCrop().into(itemHolder.imageView);
                }


            }

        }
    }

    public String getLink(int position){
        return mData.get(position).getItemLink();
    }
    void setItems(List<RecipeItem> items){
        mData = items;
        notifyDataSetChanged();
    }

    void addItem(RecipeItem name){
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
        if (mData != null && mData.size() != 0)
            return mData.size();
        else return 0;
    }
}