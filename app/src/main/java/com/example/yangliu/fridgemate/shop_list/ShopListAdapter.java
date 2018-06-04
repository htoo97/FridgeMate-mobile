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
import android.widget.TextView;

import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.MainActivity;
import com.example.yangliu.fridgemate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.example.yangliu.fridgemate.shop_list.ShopListFragment.addSelectedToFrdige;

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
//            selectedItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (selectedItem.isChecked())
//                        selectedItem.setChecked(false);
//                    else
//                        selectedItem.setChecked(true);
//                }
//            });
        }
    }


    private DocumentReference fridgeDoc;

    private final LayoutInflater mInflater;
    private List<Pair<String,Integer>> mShopList;
    private Context context;
    public List<Boolean> mSelectedItems;
    public int sumAmount = 0;

    public ShopListAdapter(final Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        // Database connection set up
        DocumentReference userDoc = MainActivity.userDoc;
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
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
        List<Pair<String, Integer>> addedItems = new LinkedList<>();

        // upload to database
        for (int i = 0; i < mSelectedItems.size(); i++){
            if (mSelectedItems.get(i) == true){
                // DATABASE:: add this to the fridge database
                final Map<String, Object> itemData = new HashMap<>();
                Pair<String,Integer> itemToAdd = mShopList.get(i);
                String itemName = "";
                if (mShopList.get(i).second == 0)
                    itemName = String.valueOf(itemToAdd.first);
                else
                    itemName = String.valueOf(itemToAdd.second) + " " + itemToAdd.first;

                itemData.put("itemName", itemName);
                itemData.put("expirationDate","");
                fridgeDoc.collection("FridgeItems").add(itemData);
                addedItems.add(itemToAdd);

                mSelectedItems.set(i,false);
                // update local adapter (bad idea)
                //MainActivity.adapter.addNonExpiringItem(new FridgeItem(itemName,""));
            }
        }

        if (addedItems.size() != 0) {
            // update list locally
            for (Pair<String, Integer> item: addedItems){
                mShopList.remove(item);
                // resize the selected list
                mSelectedItems.remove(0);
            }


            fridgeDoc.update("shoppingList", mShopList);
            notifyDataSetChanged();

            // Let content list sync
            MainActivity.contentSync = true;
        }
        sumAmount = 0;
        ShopListFragment.addSelectedToFrdige.setText("FRIDGE THEM");
        addSelectedToFrdige.setClickable(true);
    }

    void addItem(final String name, final Integer amount){
        // DATABASE: Add to the database
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
        //DATABASE: remove this item
        mShopList.remove(pos);
        if (mSelectedItems.get(pos)) {
            --sumAmount;
            ShopListFragment.addSelectedToFrdige.setText("FRIDGE THEM (" + sumAmount + ")");
        }

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
        // DATABASE Sync the shoplist on database with the local
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot fridgeData = task.getResult();
                List<String> dataList = (List) fridgeData.get("shoppingList");
                if (mShopList != null)
                    mShopList.clear();
                mShopList = new LinkedList<Pair<String, Integer>>();
                mSelectedItems = new LinkedList<Boolean>();
                if (dataList != null) {
                    for (int i = 0; i < dataList.size(); ++i) {
                        String[] data = dataList.get(i).split("#");
                        mShopList.add(new Pair<String, Integer>(data[0], Integer.valueOf(data[1])));
                        mSelectedItems.add(false);
                    }
                    notifyDataSetChanged();
                }
            }
        });
        ShopListFragment.addSelectedToFrdige.setText("FRIDGE THEM");
        sumAmount = 0;
    }



    @Override
    public int getItemCount() {
        if (mShopList != null)
            return mShopList.size();
        return 0;
    }
}