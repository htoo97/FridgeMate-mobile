package com.example.yangliu.fridgemate.shop_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yangliu.fridgemate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShopListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    class shopItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private ImageButton delete;
        private TextView amount;
        private CheckBox selectedItem;

        public shopItemViewHolder(View itemView) {
            super(itemView);
            name= itemView.findViewById(R.id.shop_list_item_name);
            amount = itemView.findViewById(R.id.amount);
            delete = itemView.findViewById(R.id.delete_shop_item);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    removeItem(pos);
                }
            });
            selectedItem = itemView.findViewById(R.id.select_item);
            selectedItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(selectedItem.isChecked()) {
                        mSelectedItems.set(pos, true);
                        sumAmount += mShopList.get(pos).second;
                    }
                    else {
                        mSelectedItems.set(pos, false);
                        sumAmount -= mShopList.get(pos).second;
                    }
                }
            });

        }
    }

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference fridgeDoc;

    private final LayoutInflater mInflater;
    private List<Pair<String,Integer>> mShopList;
    private Context context;
    private List<Boolean> mSelectedItems;
    int sumAmount = 0;

    ShopListAdapter(final Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);


        // Database connection set up
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        DocumentReference  userDoc = db.collection("Users").document(user.getEmail());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                    syncItems();
                }
            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.shop_list_item, parent, false);
        return new shopItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        shopItemViewHolder iholder = (shopItemViewHolder) holder;
        if (mShopList != null) {
            Pair<String, Integer> current = mShopList.get(position);
            iholder.name.setText(current.first);
            iholder.amount.setText(String.valueOf(current.second));
            iholder.selectedItem.setChecked(mSelectedItems.get(position));
        }
    }


    void addSelectedToFridge(){
        for (int i = 0; i < getItemCount(); i++){
            if (mSelectedItems.get(i) == true){
                // TODO:: DATABASE:: add this to the fridge database
                final Map<String, Object> itemData = new HashMap<>();
                itemData.put("itemName", String.valueOf(mShopList.get(i).second) + " " + mShopList.get(i).first);
//                SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy");
//                itemData.put("purchaseDate", mdyFormat.format(Calendar.getInstance().getTime()).toString());
//                itemData.put("lastModifiedBy", user);
                fridgeDoc.collection("FridgeItems").add(itemData);
                removeItem(i);
            }
        }

        notifyDataSetChanged();
    }

    void addItem(final String name, final Integer amount){
        // TODO:: DATABASE: Add to the database
        // Create fridge document in database
        mShopList.add(new Pair<String, Integer>(name,amount));
        mSelectedItems.add(false);
        notifyDataSetChanged();
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();

                List<String> shopList = new LinkedList();
                if (fridgeData.get("shoppingList") != null) {
                    shopList = ((List) fridgeData.get("shoppingList"));
                }
                shopList.add(name+"#"+String.valueOf(amount));
                Map<String, Object> shopListHolder = new HashMap<>();
                shopListHolder.put("shoppingList", shopList);
                fridgeDoc.update(shopListHolder);
            }
        });


    }
    void removeItem(final int pos){
        //TODO:: DATABASE: remove this item
        mShopList.remove(pos);
        mSelectedItems.remove(pos);
        notifyDataSetChanged();
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();

                List<String> shopList = new LinkedList();
                if (fridgeData.get("shoppingList") != null){
                    shopList = (List)fridgeData.get("shoppingList");
                }
                shopList.remove(pos);
                Map<String, Object> shopListHolder = new HashMap<>();
                shopListHolder.put("shoppingList", shopList);
                fridgeDoc.update(shopListHolder);
            }
        });
    }

    public void syncItems(){
        // TODO:: DATABASE Sync the shoplist on database with the local
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();
                List<String> dataList = (List) fridgeData.get("shoppingList");
                if (mShopList != null)
                    mShopList.clear();
                mShopList = new LinkedList<Pair<String,Integer>>();
                mSelectedItems = new LinkedList<Boolean>();
                if (dataList != null) {
                    for (int i = 0; i < dataList.size(); ++i){
                        String[] data = dataList.get(i).split("#");
                        mShopList.add(new Pair<String,Integer>(data[0],Integer.valueOf(data[1])));
                        mSelectedItems.add(false);
                    }
                    notifyDataSetChanged();
                }
            }
        });

        // TODO:: also update sumAmount

    }


    @Override
    public int getItemCount() {
        if (mShopList != null)
            return mShopList.size();
        return 0;
    }
}