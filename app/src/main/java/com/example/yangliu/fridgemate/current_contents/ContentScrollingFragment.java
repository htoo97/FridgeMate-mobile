package com.example.yangliu.fridgemate.current_contents;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.MainActivity;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.current_contents.receipt_scan.OcrCaptureActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.yangliu.fridgemate.MainActivity.contentListAdapter;

public class ContentScrollingFragment extends Fragment implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{

    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    public static final int NEW_OCR_ACTIVITY_REQUEST_CODE = 3;
    public static final int RECIPE_ACTIVITY_REQUEST_CODE = 4;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton addManual, addOCR;

    private LinearLayout empty_list_prompt;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private DocumentReference fridgeDoc;

    public static int currentListYPos = 0;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        constraintLayout = view.findViewById(R.id.cl);

        storage = FirebaseStorage.getInstance();
        fridgeDoc = MainActivity.fridgeDoc;
        empty_list_prompt = view.findViewById(R.id.empty_list);

        // List view
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        // Connect list to its adapter
        recyclerView.setAdapter(contentListAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // set up fall in animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.fall_in_layout);
        recyclerView.setLayoutAnimation(animation);

        // on item Click (modifying item)
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ContentScrollingFragment.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(view.getContext(), AddItemManual.class);
                Bundle extras = new Bundle();
                FridgeItem i = contentListAdapter.mItemsOnDisplay.get(position);
                extras.putString("name",i.getItemName());
                extras.putString("expDate",i.getExpDate());
                extras.putString("image",i.getImage().toString());
                extras.putString("docRef",i.getDocRef());
                intent.putExtras(extras);
                Objects.requireNonNull(getActivity()).setResult(RESULT_OK, intent);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.item_image), "item_image");
                startActivityForResult(intent, EDIT_ITEM_ACTIVITY_REQUEST_CODE, options.toBundle());
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        // swipe refresh
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                syncList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,R.color.green);

        // floating button menu
        materialDesignFAM = (FloatingActionMenu) view.findViewById(R.id.material_design_android_floating_action_menu);
        materialDesignFAM.setClosedOnTouchOutside(true);
        addManual = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item1);
        addOCR = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item2);
        addManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddItemManual.class);
                startActivityForResult(intent, NEW_ITEM_ACTIVITY_REQUEST_CODE);
                materialDesignFAM.close(true);
            }
        });
        addOCR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OcrCaptureActivity.class);
                startActivityForResult(intent, NEW_OCR_ACTIVITY_REQUEST_CODE);
                materialDesignFAM.close(true);

            }
        });

        // swipe to delete initialization
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FridgeItemTouchHelper(0,
                ItemTouchHelper.LEFT, (FridgeItemTouchHelper.FridgeItemTouchHelpListener) this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // avoid abusive syncing
        if (MainActivity.contentSync){
            syncList();
            MainActivity.contentSync = false;
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // set up the search button for content list view
        setHasOptionsMenu(true);
        // the fragment won't get pushed up by the keyboard
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scrolling, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search) return false;

        //Floating action button for recipe suggest
        Intent intent = new Intent(getActivity(), RecipeSuggestion.class);
        StringBuilder toSearch = new StringBuilder();
        if (contentListAdapter.mItems != null) {
            for (FridgeItem i : contentListAdapter.mItems) {
                String searchStr = i.getItemName();
                toSearch.append(searchStr.replace(' ', ',')).append(',');
            }
        }
        intent.putExtra("search string", toSearch.toString());
        startActivityForResult(intent, RECIPE_ACTIVITY_REQUEST_CODE);


        return super.onOptionsItemSelected(item);
    }

    // Search button & Recipe button set up
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(getActivity()).getSystemService(ContentScrollingFragment.this.getActivity().SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        // searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Toast.makeText(getContext(), "Searching for " + query, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        contentListAdapter.filterList(newText);
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
            // DATABASE delete the item at position

            String id = contentListAdapter.mItemsOnDisplay.get(position).getDocRef();
            final DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(id);

            final boolean[] deletePermananetly = {true};

            contentListAdapter.remove(position);
            // DATABASE restore the item deletion (by delay or make a temporary copy)
            // showing snack bar with Undo option
            final Snackbar snackbar = Snackbar
                    .make(constraintLayout, "Item removed!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // undo is selected, restore the deleted item
                    contentListAdapter.restore();
                    contentListAdapter.notifyDataSetChanged();
                    deletePermananetly[0] = false;
                }
            });
//            snackbar.setAction("MOVE To SHOPPING LIST", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // undo is selected, restore the deleted item
//                    MainActivity.adapter.restore();
//                    MainActivity.adapter.notifyDataSetChanged();
//                    deletePermananetly[0] = false;
//                    return;
//                }
//            });
            snackbar.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    snackbar.dismiss();
                    if (deletePermananetly[0]) {
                        // delete its photo
                        itemDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                String uri = (String) task.getResult().get("imageID");
                                if (uri != null && !uri.equals("null") && !uri.equals(""))
                                    storage.getReferenceFromUrl(uri).delete();
                                // delete its info
                                itemDoc.delete();
                            }
                        });

                    }
                }
            }, 2000);

        }
    }

    // having added/edited an item from the fab or modified
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == EDIT_ITEM_ACTIVITY_REQUEST_CODE) {

            }
            syncList();
        }
        else if (requestCode == RESULT_CANCELED){
            Toast.makeText(getContext(), "Item not saved", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void syncList(){
        // DATABASE: populate the local list
        final List<FridgeItem> mItems = new LinkedList<>();
        MainActivity.userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                    assert fridgeDoc != null;
                    fridgeDoc.collection("FridgeItems").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Uri image = Uri.parse(String.valueOf(document.get("imageID")));
                                FridgeItem i = new FridgeItem(String.valueOf(document.get("itemName")),
                                        String.valueOf(document.get("expirationDate")),image, String.valueOf(document.getId()));
                                mItems.add(i);
                            }
                            contentListAdapter.setItems(mItems);
                            contentListAdapter.notifyDataSetChanged();

                            // After populating, notify user if the list is empty
                            if (contentListAdapter.getItemCount()==0)
                                empty_list_prompt.setVisibility(View.VISIBLE);
                            else
                                empty_list_prompt.setVisibility(View.INVISIBLE);
                        }
                    });}
            }
        });

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
