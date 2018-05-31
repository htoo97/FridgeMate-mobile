package com.example.yangliu.fridgemate.current_contents;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.current_contents.receipt_scan.OcrCaptureActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.FirebaseDatabase;

import static android.app.Activity.RESULT_OK;

public class ContentScrollingFragment extends Fragment implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{

    public static final String NAME_KEY = "name";
    public static final String ITEM_ID = "item_id";
    public static final String DATE_KEY = "date";
    public static final String IMAGE_KEY = "img";
    public static final String Other_KEY = "other";
    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    public static final int NEW_OCR_ACTIVITY_REQUEST_CODE = 3;
    public static final int RECIPE_ACTIVITY_REQUEST_CODE = 4;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton addManual, addOCR;
    private android.support.design.widget.FloatingActionButton recipeFAB;

    private ContentListAdapter adapter = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        constraintLayout = view.findViewById(R.id.cl);

        // List view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        // Connect list to its adapter
        adapter = new ContentListAdapter(view.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        // TODO:: DATABASE:: call syncList()

        // on item Click (modifying item)
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ContentScrollingFragment.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO:: DATABASE pass the item id to AddItemManual.class for edit
                Intent intent = new Intent(view.getContext(), AddItemManual.class);
                Bundle extras = new Bundle();
//              extras.putString(ITEM_ID,database.getValue("passing the itemid"));
                intent.putExtras(extras);
                getActivity().setResult(RESULT_OK, intent);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.item_image), "item_image");
                startActivity(intent, options.toBundle());
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        // TODO:: I'm not sure if it's needed; but view model persists even when app is destroyed
//        mItemViewModel = ViewModelProviders.of(this).get(FridgeItemViewModel.class);
//        mItemViewModel.getAllItems().observe(this, new Observer<List<FridgeItem>>() {
//            @Override
//            public void onChanged(@Nullable final List<FridgeItem> items) {
//                // Update the cached copy of the items in the adapter.
//                adapter.setItems(items);
//            }
//        });


        // swipe refresh
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                // TODO:: DATABASE:: call syncList()
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.red, R.color.yellow, R.color.green);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);

        // floating button menu
        materialDesignFAM = (FloatingActionMenu) view.findViewById(R.id.material_design_android_floating_action_menu);
        addManual = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item1);
        addOCR = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item2);
        addManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddItemManual.class);
                startActivityForResult(intent, NEW_ITEM_ACTIVITY_REQUEST_CODE);
            }
        });
        addOCR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OcrCaptureActivity.class);
                startActivityForResult(intent, NEW_OCR_ACTIVITY_REQUEST_CODE);

            }
        });

        //Floating action button for recipe suggest
        recipeFAB = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.recipe_button);
        recipeFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RecipeSuggestion.class);
                startActivityForResult(intent, RECIPE_ACTIVITY_REQUEST_CODE);

            }
        });

        // swipe to delete initialization
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FridgeItemTouchHelper(0,
                ItemTouchHelper.LEFT, (FridgeItemTouchHelper.FridgeItemTouchHelpListener) this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // set up the search button for content list view
        setHasOptionsMenu(true);
        // the fragment won't get pushed up by the keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scrolling, container, false);
    }

    // Search button set up
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(ContentScrollingFragment.this.getActivity().SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Toast.makeText(getContext(), "Searching for " + query, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.filterList(newText);
                        return false;
                    }

                }
        );
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Define what a delete swipe on an item does
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ContentListAdapter.ItemViewHolder) {
            // TODO:: DATABASE delete the item at position

            // TODO:: DATABASE restore the item deletion (by delay or make a temporary copy)
//            // showing snack bar with Undo option
//            Snackbar snackbar = Snackbar
//                    .make(constraintLayout, item.getItemName() + " removed!", Snackbar.LENGTH_LONG);
//            snackbar.setAction("UNDO", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    // undo is selected, restore the deleted item
//                    mItemViewModel.restoreItem(item);
//                }
//            });
//            snackbar.show();
        }
    }

    // having added/edited an item from the fab or modified
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            // fetch results
//            Bundle extras = data.getExtras();
//            String name_string = extras.getString(NAME_KEY);
//            String date_string = extras.getString(DATE_KEY);
//            byte[] imageByte = extras.getByteArray(IMAGE_KEY);

            syncList();
        }
        else {
            Toast.makeText(getContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
        }
    }

    public void syncList(){
        // TODO:: DATABASE: populate the local list

        // global current fridge position in the fridge list
        int currentFridge = SaveSharedPreference.getCurrentFridge(getContext());
        // get a list of items and set it to the list view's adapter
        //adapter.setItems(List< FridgeItem> blabla);

        adapter.notifyDataSetChanged();
    }

}
