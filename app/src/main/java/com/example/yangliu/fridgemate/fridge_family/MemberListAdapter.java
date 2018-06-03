package com.example.yangliu.fridgemate.fridge_family;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yangliu.fridgemate.Fridge;
import com.example.yangliu.fridgemate.MainActivity;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.Fridge;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.spec.ECField;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView name,status,textMemberView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_view);
            imageView = itemView.findViewById(R.id.member_image);
            status = itemView.findViewById(R.id.status_view);
            textMemberView = itemView.findViewById(R.id.text_MemberView);

        }
    }

    private final LayoutInflater mInflater;
    private int currentFridge = -1;
    private Context context;
    public List<DocumentReference> names;
    // TODO DATABASE:: set up images of members
    //private Bitmap[] images;


    private FirebaseUser user;
    private FirebaseFirestore db;
    public DocumentReference fridgeDoc;

    public MemberListAdapter(Context context) {
        this.context = context;
        currentFridge = SaveSharedPreference.getCurrentFridge(context);
        mInflater = LayoutInflater.from(context);

        names = new LinkedList<DocumentReference>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        MainActivity.userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                }
            }
        });
    }

    public void syncMemberList(){
        // if database connection is not ready yet, wait for 1.5 seconds
        if (fridgeDoc == null){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    fetchMemberList();
                }
            }, 500);
        }
        else
            fetchMemberList();
    }

    public void fetchMemberList(){
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();

                names = (List) fridgeData.get("members");
                if (names == null)
                    names = new LinkedList();
                notifyDataSetChanged();

            }
        });
    }

    private static final int FOOTER_VIEW = 1;
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), InviteFridgeMateActivity.class);
                    v.getContext().startActivity(intent);
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
        if (currentFridge == -1 || (names == null || position == names.size())) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        try {
            if (holder instanceof ItemViewHolder) {
                final ItemViewHolder iholder = (ItemViewHolder) holder;
                if (currentFridge != -1) {

                    // set up each member
                    // set name
                    final String name = names.get(position).getId();
                    iholder.name.setText(name);
                    names.get(position).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot userData = task.getResult();

                                // set up user's status
                                String status = String.valueOf(userData.get("status"));
                                if (status != null && !status.equals("null"))
                                iholder.status.setText(status);

                                // DATABASE set up user's image
                                String image = String.valueOf(userData.get("profilePhoto"));
                                if (image != null && !image.equals("null"))
                                    Glide.with(context).load(Uri.parse(image)).centerCrop().into(iholder.imageView);
                                else
                                    iholder.textMemberView.setText(String.valueOf(name.charAt(0)).toUpperCase());
                            }
                        }
                    });

                }
                else {
                    // Covers the case of data not being ready yet.
                    iholder.name.setText("No user");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (names != null)
            return names.size() + 1;
        return 1;
    }

    void setFridge(int item){
        currentFridge = item;
        notifyDataSetChanged();
    }
}