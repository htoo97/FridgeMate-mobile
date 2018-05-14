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

import java.security.spec.ECField;
import java.util.List;

public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    private int currentFridge = -1;
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

    private static final int FOOTER_VIEW = 1;
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO:: DATABASE:: add a fridge member (if we are not using the floating add button)
                }
            });
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == FOOTER_VIEW) {
            itemView = mInflater.inflate(R.layout.fridge_member_list_add_footer, parent, false);
            return new FooterViewHolder(itemView);
        }
        else {
            itemView = mInflater.inflate(R.layout.fridge_member_list_item, parent, false);
            return new ItemViewHolder(itemView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        // TODO:: add one to the list
        if (currentFridge == -1 || (names == null || position == names.length)) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        try {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder iholder = (ItemViewHolder) holder;
                if (currentFridge != -1) {
                    // set up each member
                    iholder.name.setText(names[position]);
                    iholder.status.setText(statuses[position]);

                    // TODO:: DATABASE set up user's image
                    //holder.imageView.setImageBitmap(images[position]);
                }
                //holder.progressBar.setProgress();
                else {
                    // Covers the case of data not being ready yet.
                    iholder.name.setText("No user");
                }
            }
            else if (holder instanceof FooterViewHolder){
                // if it is a footer, now what to change?
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (names != null)
            return names.length + 1;
        return 1;
    }

    void setFridge(int item){
        currentFridge = item;
        notifyDataSetChanged();
    }
}