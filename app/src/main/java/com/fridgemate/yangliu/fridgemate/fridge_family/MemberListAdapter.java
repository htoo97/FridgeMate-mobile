package com.fridgemate.yangliu.fridgemate.fridge_family;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.RedirectToLogInActivity;
import com.fridgemate.yangliu.fridgemate.SaveSharedPreference;

import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeDoc;
import static com.fridgemate.yangliu.fridgemate.MainActivity.user;
import static com.fridgemate.yangliu.fridgemate.fridge_family.FridgeFamilyFragment.swipeRefreshLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.LinkedList;
import java.util.List;

//import android.view.animation.AnimationUtils;

public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView name,status,textMemberView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_view);
            imageView = itemView.findViewById(R.id.member_image);
            imageView.setDrawingCacheEnabled(true);
            status = itemView.findViewById(R.id.status_view);
            textMemberView = itemView.findViewById(R.id.text_MemberView);
        }
    }

    private final LayoutInflater mInflater;
    private int currentFridge = -1;
    private Context context;
    public List<DocumentReference> names;
    private final Animation fadeOutAnim;
    private final Animation fadeInAnim;
    //private Animation animation;

    public MemberListAdapter(Context context) {
        this.context = context;
        currentFridge = SaveSharedPreference.getCurrentFridge(context);
        mInflater = LayoutInflater.from(context);

        fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        fadeInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        names = new LinkedList<>();
    }

    public void populateAdapter(List<DocumentReference> dataList){
        names = dataList;
        notifyDataSetChanged();
    }

    public void syncMemberList(){

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.startAnimation(fadeOutAnim);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        MainActivity.userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                    fetchMemberList();
                    MainActivity.showProgress(false);
                }
            }
        });
    }

    private void fetchMemberList(){
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();

                names = (List) fridgeData.get("members");
                if (names == null)
                    names = new LinkedList();
                notifyDataSetChanged();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.startAnimation(fadeInAnim);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    final int REQUEST_NEW_ACCOUNT = 233;
    private static final int FOOTER_VIEW = 1;
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (user.isAnonymous()){
                        Intent i = new Intent(v.getContext(), RedirectToLogInActivity.class);
                        ((Activity) v.getContext()).startActivityForResult(i,REQUEST_NEW_ACCOUNT);
                        return;
                    }

                    Intent intent = new Intent(v.getContext(), InviteFridgeMateActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        try {
            // indivisual animation
            if (holder instanceof ItemViewHolder) {
                final ItemViewHolder iholder = (ItemViewHolder) holder;
                if (currentFridge != -1) {

                    // set up each member
                    // set name
                    String name = names.get(position).getId();
                    // parse away @ part and capitalize string
                    if (name.length() != 0 && name.contains("@"))
                        name = name.substring(0,1).toUpperCase() + name.substring(1,name.indexOf("@"));
                    final String finalName = name;
                    iholder.name.setText(finalName);
                    names.get(position).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot userData = task.getResult();

                                // if user has a nickname set nickname
                                String nickname = String.valueOf(userData.get("name"));
                                if (nickname != null && !nickname.equals("null") && !nickname.equals(""))
                                    iholder.name.setText(nickname);

                                // set up user's status
                                String status = String.valueOf(userData.get("status"));
                                if (status != null && !status.equals("null"))
                                    iholder.status.setText(status);
                                else
                                    iholder.status.setText(R.string.example_status);

                                // DATABASE set up user's image
                                String image = String.valueOf(userData.get("profilePhoto"));
                                if (image != null && !image.equals("null")) {
                                    Glide.with(context).load(Uri.parse(image)).centerCrop().into(iholder.imageView);
                                }
                                else {
                                    // no profile image
                                    iholder.imageView.setImageResource(0);
                                    if (nickname != null && !nickname.equals("null") && !nickname.equals(""))
                                        iholder.textMemberView.setText(String.valueOf(nickname.charAt(0)).toUpperCase());
                                    else
                                        iholder.textMemberView.setText(String.valueOf(finalName.charAt(0)).toUpperCase());
                                }
                            }
                        }
                    });

                }
                else {
                    // Covers the case of data not being ready yet.
                    iholder.name.setText(R.string.this_is_null);
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