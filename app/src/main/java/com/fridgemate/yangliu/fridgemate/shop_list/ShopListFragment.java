package com.fridgemate.yangliu.fridgemate.shop_list;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import static com.fridgemate.yangliu.fridgemate.MainActivity.shopListAdapter;

public class ShopListFragment extends Fragment {

    public static SwipeRefreshLayout shopListRefresh;


    private EditText name;
    @SuppressLint("StaticFieldLeak")
    public static Button addSelectedToFrdige;
    private TextView amount;
    private SwipeRefreshLayout snackBarView;

    public ShopListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_shop_list, container, false);

        snackBarView = view.findViewById(R.id.swiperefresh);
        name = view.findViewById(R.id.et_content1);
        ImageButton incQuantity = view.findViewById(R.id.ibn_add1);
        ImageButton decQuantity = view.findViewById(R.id.ibn_del1);
        amount = view.findViewById(R.id.et_content2);
        incQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp;
                String a = String.valueOf(amount.getText());
                if (a.equals(""))
                    temp = 2;
                else{
                    temp = Integer.parseInt(a);
                    temp += 1;
                }
                if (temp>99)
                    amount.setText("99");
                else
                    amount.setText(String.valueOf(temp));
            }
        });
        decQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = String.valueOf(amount.getText());
                int temp;
                if (a.equals(""))
                    temp = 1;
                else{
                    temp = Integer.parseInt(a);
                }
                if (temp!=1) {
                    temp -= 1;
                }
                else {
                    temp = 1;
                }
                amount.setText(String.valueOf(temp));
            }
        });

        // fridge list
        final RecyclerView itemListView = view.findViewById(R.id.shopping_list);
        itemListView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        itemListView.setLayoutManager(llm);
        itemListView.setAdapter(shopListAdapter);
        itemListView.setVisibility(View.VISIBLE);
        ViewCompat.setNestedScrollingEnabled(itemListView, false);

        Button addItemToShopList = view.findViewById(R.id.add_to_shop_list);
        addItemToShopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = String.valueOf(name.getText());
                if (itemName.length() != 0) {
                    // capitalize item name
                    itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
                    String num = String.valueOf(amount.getText());
                    int amt;
                    if (num.equals(""))
                        amt = 1;
                    else
                        amt = Integer.valueOf(num);
                    shopListAdapter.addItem(itemName, amt);
                }
                else
                    Toast.makeText(getContext(), "Please give it a name", Toast.LENGTH_SHORT).show();
                name.setText("");
            }
        });
        addSelectedToFrdige  = view.findViewById(R.id.add_selected_to_fridge);
        addSelectedToFrdige.setText(R.string.fridge_all);
        addSelectedToFrdige.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSelectedToFrdige.setClickable(false);
                shopListAdapter.addSelectedToFridge();
            }
        });

        shopListRefresh = view.findViewById(R.id.swiperefresh);
        shopListRefresh.setEnabled(true);
        shopListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shopListAdapter.syncItems();
            }
        });

        // TODO remove this progress bar
        MainActivity.showProgress(false);

        setUpRealTimeListener();
        return view;
    }

    public void setUpRealTimeListener(){
        MainActivity.fridgeDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                final String TAG = "RealTime Listener";
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
//                    Log.d(TAG, source + " data: " + snapshot.getData());
                    shopListAdapter.populateAdapter((List<String>) snapshot.getData().get("shoppingList"));

                } else {
//                    Log.d(TAG, source + " data: null");

                }
            }
        });
    }

}
