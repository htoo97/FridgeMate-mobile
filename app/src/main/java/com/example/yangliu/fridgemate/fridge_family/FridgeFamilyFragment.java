package com.example.yangliu.fridgemate.fridge_family;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.current_contents.RecyclerItemClickListener;


public class FridgeFamilyFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;

    private MemberListAdapter memberListAdapter;
    private FridgeListAdapter fridgeListAdapter;

    public FridgeFamilyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fridge_family, container, false);

        // TODO:: DATABASE:: if user doesn't have a fridge, create one and set current fridge globally
        // checking if user has a fridge, if not, creating a new fridge
        // SaveSharedPreference.setCurrentFridge(this, 0); // 0 means the first index in the fridges array

        constraintLayout = view.findViewById(R.id.cl);

        // fridge list set up
        RecyclerView mfridgeListView = (RecyclerView) view.findViewById(R.id.fridgeList);
        mfridgeListView.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mfridgeListView.setLayoutManager(MyLayoutManager);
        fridgeListAdapter = new FridgeListAdapter(view.getContext());
        mfridgeListView.setAdapter(fridgeListAdapter);

        //  on item Click:: change current fridge
        mfridgeListView.addOnItemTouchListener(new RecyclerItemClickListener(FridgeFamilyFragment.this, mfridgeListView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // change focus color
                int oldSelectedPos = fridgeListAdapter.selectedItemPos;
                fridgeListAdapter.selectedItemPos = position;
                if (oldSelectedPos != -1)  fridgeListAdapter.notifyItemChanged(oldSelectedPos);
                fridgeListAdapter.notifyItemChanged(position);
                // update the global current fridge
                SaveSharedPreference.setCurrentFridge(getContext(),position);
                // TODO:: DATABASE:: call syncList()
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if (position != fridgeListAdapter.getItemCount() - 1) {
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    popup.getMenuInflater().inflate(R.menu.menu_for_item, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            // TODO:: DATABASE: delete fridge
                            // position is the index of the fridge (to be deleted)
                            // call sync()
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        }));

        // for presentation: using the items as member adapters
        RecyclerView mRecyclerMemberView = view.findViewById(R.id.fridgeMemberList);
        memberListAdapter = new MemberListAdapter(view.getContext());
        mRecyclerMemberView.setAdapter(memberListAdapter);
        mRecyclerMemberView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mRecyclerMemberView.addOnItemTouchListener(new RecyclerItemClickListener(FridgeFamilyFragment.this, mRecyclerMemberView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO:: check someone's profile
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if (position != memberListAdapter.getItemCount() - 1) {
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    popup.getMenuInflater().inflate(R.menu.menu_for_item, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            // TODO:: DATABASE: delete member
                            // position is the index of the member (to be deleted)


                            // call sync()
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        }));


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO:: refresh adapter set data to something
                swipeRefreshLayout.setRefreshing(true);
                // TODO:: DATABASE:: call syncList()
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        // TODO:: DATABASE:: call syncList()
        return view;
    }

    public void syncList(){
        // TODO:: DATABASE: populate the fridge list and fridge member list
        // global current fridge position in the fridge list
        int currentFridge = SaveSharedPreference.getCurrentFridge(getContext());
        //fridgeListAdapter.setItems(List<Fridge> blabla);
    }

}
