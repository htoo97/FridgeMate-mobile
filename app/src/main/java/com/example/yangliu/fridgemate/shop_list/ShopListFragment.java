package com.example.yangliu.fridgemate.shop_list;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.yangliu.fridgemate.R;



public class ShopListFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;

    private EditText name;
    private Button addSelectedToFrdige;
    private Button addItemToShopList;
    private ImageButton incQuantity;
    private ImageButton decQuantity;
    private TextView amount;

    public ShopListFragment() {
    }

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
        final RecyclerView mRecyclerFridgeView = (RecyclerView) view.findViewById(R.id.shopping_list);
        mRecyclerFridgeView.setHasFixedSize(true);
        final ShopListAdapter shopListAdapter = new ShopListAdapter(view.getContext());
        mRecyclerFridgeView.setAdapter(shopListAdapter);
        registerForContextMenu(mRecyclerFridgeView);

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
        addSelectedToFrdige.setText("Fridge them (" + shopListAdapter.sumAmount +")" );
        addSelectedToFrdige.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }


}
