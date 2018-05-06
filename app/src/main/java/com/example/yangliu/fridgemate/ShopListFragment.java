package com.example.yangliu.fridgemate;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.data.FridgeItem;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopListFragment extends Fragment {

    private static final String SHOP_LIST_TEXT_KEY = "shoplisttext";
    private static final int EDIT_SHOP_LIST = 112;
    private static final String ITEM_BOUGHT = "itembought";


    private EditText shoppingInputText;
    private ImageButton refreshList;

    public ShopListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final ShopListAdapter adapter = new ShopListAdapter(view.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        //recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        // Generate the shopping list
        shoppingInputText = view.findViewById(R.id.editText);
        String shopListCache = SaveSharedPreference.getShopList(view.getContext());
        shoppingInputText.setText(shopListCache);

        // refresh: generate a list of items from the text
        refreshList = view.findViewById(R.id.refreshShopListButton);
        refreshList.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // add to local cache
                        String shoppingText = String.valueOf(shoppingInputText.getText());
                        SaveSharedPreference.setShopList(getContext(), shoppingText);

                        // update list
                        // todo adapter

                    }
                }
        );
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shoplist, container, false);

    }
}