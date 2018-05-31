package com.example.yangliu.fridgemate.shop_list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.yangliu.fridgemate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class ShopListFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;

    private EditText name;
    public Button addSelectedToFrdige;
    private Button addItemToShopList;
    private ImageButton incQuantity;
    private ImageButton decQuantity;
    private TextView amount;

    private ShopListAdapter shopListAdapter;

    public ShopListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_list, container, false);

        name = view.findViewById(R.id.et_content1);
        incQuantity =  view.findViewById(R.id.ibn_add1);
        decQuantity = view.findViewById(R.id.ibn_del1);
        amount = view.findViewById(R.id.et_content2);
        incQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                temp += 1;
                amount.setText(String.valueOf(temp));
            }
        });
        decQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                if (temp!=0) {
                    temp -= 1;
                }
                else {
                    temp = 0;
                }
                amount.setText(String.valueOf(temp));
            }
        });

        constraintLayout = view.findViewById(R.id.cl);

        // fridge list
        final RecyclerView itemListView = (RecyclerView) view.findViewById(R.id.shopping_list);
        itemListView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        itemListView.setLayoutManager(llm);

        shopListAdapter = new ShopListAdapter(view.getContext());
        itemListView.setAdapter(shopListAdapter);
        itemListView.setVisibility(View.VISIBLE);

        addItemToShopList = view.findViewById(R.id.add_to_shop_list);
        addItemToShopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopListAdapter.addItem(String.valueOf(name.getText()),Integer.valueOf(""+ amount.getText()));
                name.setText("");
                amount.setText("0");
            }
        });
        addSelectedToFrdige  = view.findViewById(R.id.add_selected_to_fridge);
        addSelectedToFrdige.setText("Fridge them");
        addSelectedToFrdige.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopListAdapter.addSelectedToFridge();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO:: refresh adapter set data to something
                swipeRefreshLayout.setRefreshing(true);
                shopListAdapter.syncItems();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

}
