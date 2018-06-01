package com.example.yangliu.fridgemate.fridge_family;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.Fridge;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.Fridge;

import java.util.List;

public class FridgeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
            imageView = itemView.findViewById(R.id.item_image);

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

    private static final int FOOTER_VIEW = 1;
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), CreateJoinFridgeActivity.class);
                    itemView.getContext().startActivity(intent);
                    notifyDataSetChanged();
                }
            });
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        if (viewType == FOOTER_VIEW) {
            itemView = mInflater.inflate(R.layout.fridge_list_add_footer, parent, false);
            return new FooterViewHolder(itemView);
        }
        else {
            itemView = mInflater.inflate(R.layout.fridge_list_item, parent, false);
            return new ItemViewHolder(itemView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (mFridges == null || position == mFridges.size()) {
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        try {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder iholder = (ItemViewHolder) holder;
                if (mFridges != null) {

                    Fridge current = mFridges.get(position);

                    if (position != selectedItemPos) {
                        iholder.frame.setBackground(iholder.unpressed);
                    } else {
                        iholder.frame.setBackground(iholder.pressed);
                    }
                    // set name by name
                    String name = current.getFridgeName();
                    if (name != null)
                        iholder.name.setText(name);
                    else {// set name by id
                        iholder.name.setText(current.getFridgeid());
                    }
                }
                else {
                    // Covers the case of data not being ready yet.
                    iholder.name.setText("No String");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void setItems(List<Fridge> items){
        mFridges = items;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mFridges != null)
            return mFridges.size()+ 1;
        else return 1;
    }
}