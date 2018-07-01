package com.fridgemate.yangliu.fridgemate.current_contents;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import com.fridgemate.yangliu.fridgemate.FridgeItem;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.RedirectToLogInActivity;
import com.fridgemate.yangliu.fridgemate.current_contents.receipt_scan.OcrCaptureActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.fridgemate.yangliu.fridgemate.MainActivity.contentListAdapter;
import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeDoc;
import static com.fridgemate.yangliu.fridgemate.MainActivity.mAuth;
import static com.fridgemate.yangliu.fridgemate.MainActivity.shopListAdapter;
import static com.google.firebase.firestore.DocumentChange.Type.ADDED;


public class ContentScrollingFragment extends Fragment implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{


    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    public static final int NEW_OCR_ACTIVITY_REQUEST_CODE = 3;
    public static final int RECIPE_ACTIVITY_REQUEST_CODE = 4;

    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout snackBarView;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton addManual, addOCR;

    private LinearLayout empty_list_prompt;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        snackBarView = view.findViewById(R.id.swiperefresh);

        storage = FirebaseStorage.getInstance();
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
                extras.putString("amount", String.valueOf(i.getAmount()));
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
                syncList();
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

        // swipe left to delete initialization
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FridgeItemTouchHelper(0,
                ItemTouchHelper.LEFT, (FridgeItemTouchHelper.FridgeItemTouchHelpListener) this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        // swipe right to shopping list initialization
        itemTouchHelperCallback = new FridgeItemTouchHelper(0,
                ItemTouchHelper.RIGHT, (FridgeItemTouchHelper.FridgeItemTouchHelpListener) this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // TODO:: remove this progress bar in later version
        MainActivity.showProgress(false);
        setUpRealTimeListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // notify user if the list is empty
        if (contentListAdapter.getItemCount()==0)
            empty_list_prompt.setVisibility(View.VISIBLE);
        else
            empty_list_prompt.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // detach real time listener here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lastAdded = "";
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
            if (mAuth.getCurrentUser().isAnonymous()){
                Intent i = new Intent(getContext(), RedirectToLogInActivity.class);
                startActivityForResult(i,1);
                return;
            }

            if (direction == ItemTouchHelper.LEFT) {
                deleteItem(position);
            }
            else if (direction == ItemTouchHelper.RIGHT){
                moveToShopList(viewHolder,position);
            }
        }
    }

    private void moveToShopList(RecyclerView.ViewHolder viewHolder, int position){
        String itemName = String.valueOf(((ContentListAdapter.ItemViewHolder) viewHolder).wordItemView.getText());
        if (itemName != null && itemName.length() != 0) {
            // capitalize item name
            itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);

            int num = Integer.valueOf("" + ((ContentListAdapter.ItemViewHolder) viewHolder).amountView.getText());
            if (num <= 0)
                num = 1;
            shopListAdapter.addItem(itemName, num);
//            shopListSync = true;
        }
        else
            Toast.makeText(getContext(), "Move to shopping list error", Toast.LENGTH_SHORT).show();

        String id = contentListAdapter.mItemsOnDisplay.get(position).getDocRef();
        final DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(id);

        contentListAdapter.remove(position);
        // DATABASE restore the item deletion (by delay or make a temporary copy)
        // showing snack bar with Undo option
        final Snackbar snackbar = Snackbar
                .make(snackBarView, R.string.toShopList, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View view) { snackbar.dismiss(); }
        });
        snackBarView.setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
        swipeRefreshLayout.setEnabled(false);
        // delete its photo
        itemDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String uri = (String) task.getResult().get("imageID");
                if (uri != null && !uri.equals("null") && !uri.equals(""))
                    if (uri.contains("firebasestorage"))
                    storage.getReferenceFromUrl(uri).delete();
                // delete its info and dismiss snack bar
                itemDoc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        swipeRefreshLayout.setEnabled(true);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                if (snackbar.isShown())
                                    snackbar.dismiss();
                            }},400);
                    }
                });
            }
        });
    }

    private void deleteItem(int position){
        // DATABASE delete the item at position
        String id = contentListAdapter.mItemsOnDisplay.get(position).getDocRef();
        final DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(id);

        final boolean[] deletePermananetly = {true};

        contentListAdapter.remove(position);
        // DATABASE restore the item deletion (by delay or make a temporary copy)
        // showing snack bar with Undo option
        final Snackbar snackbar = Snackbar
                .make(snackBarView, "Item is about to be removed!", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // undo is selected, restore the deleted item
                contentListAdapter.restore();
                contentListAdapter.notifyDataSetChanged();
                deletePermananetly[0] = false;
            }
        });
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
        swipeRefreshLayout.setEnabled(false);
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
                                if (uri.contains("firebasestorage"))
                                    storage.getReferenceFromUrl(uri).delete();
                            // delete its info
                            itemDoc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    swipeRefreshLayout.setEnabled(true);
                                }
                            });
                        }
                    });
                }

            }
        }, 2000);
    }

    // having added/edited an item from the fab or modified
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            // no need to sync after each addition or edit (because of realtime listener)
            // syncList();
        }
        else if (requestCode == RESULT_CANCELED){
            Toast.makeText(getContext(), "Item not saved", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void syncList(){
        swipeRefreshLayout.setRefreshing(true);
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

                                String num = String.valueOf(document.get("amount"));
                                int amount = 1;
                                if (num != "null")
                                    amount = Integer.parseInt(num);

                                Uri image = Uri.parse(String.valueOf(document.get("imageID")));
                                FridgeItem i = new FridgeItem(String.valueOf(document.get("itemName")),
                                        String.valueOf(document.get("expirationDate"))
                                        ,image
                                        ,String.valueOf(document.getId())
                                        ,amount);
                                mItems.add(i);
                            }
                            contentListAdapter.setItems(mItems);
                            contentListAdapter.notifyDataSetChanged();

                            MainActivity.showProgress(false);
                            if (swipeRefreshLayout != null)
                                swipeRefreshLayout.setRefreshing(false);
                        }
                    });}
            }
        });

    }

//    public byte[] getBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//        int bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//
//        int len = 0;
//        while ((len = inputStream.read(buffer)) != -1) {
//            byteBuffer.write(buffer, 0, len);
//        }
//        return byteBuffer.toByteArray();
//    }


    private static String lastAdded = "";
    private void setUpRealTimeListener(){
        // if user doesn't have fridge yet
        if (fridgeDoc == null){
            // the fridge is being set up right now, let's give it 10 sec
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    /* Create an Intent that will start the Menu-Activity. */
                    setUpRealTimeListener();
                }
            }, 10000);
            return;
        }

        // repopulate the content list adapter
        contentListAdapter.removeAll();
        final String TAG = "RealTime";
        fridgeDoc.collection("FridgeItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        swipeRefreshLayout.setRefreshing(true);
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        List<DocumentChange> changes = snapshots.getDocumentChanges();

                        //avoid duplicates adding (this is some weird bug)
                        if (changes.size() > 0 &&(changes.get(0).getType()).equals(ADDED)) {
                            if (lastAdded.equals(changes.get(0).getDocument().getId())) {
                                swipeRefreshLayout.setRefreshing(false);
                                return;
                            }
                            else
                                lastAdded = changes.get(0).getDocument().getId();
                        }

                        for (DocumentChange dc : changes) {
                            Map<String, Object> a = dc.getDocument().getData();
                            int amt = 1;
                            String num  = String.valueOf(a.get("amount"));
                            if (!num.equals("null") && !num.equals(""))
                                amt = Integer.valueOf(String.valueOf(a.get("amount")));
                            FridgeItem i = new FridgeItem(
                                    String.valueOf(a.get("itemName")),
                                    String.valueOf(a.get("expirationDate")),
                                    Uri.parse(String.valueOf(a.get("imageID"))),
                                    String.valueOf(dc.getDocument().getId()),
                                    amt);
                            switch (dc.getType()) {
                                case ADDED:
                                    // avoid duplicate adding
                                    contentListAdapter.add(i);
                                    lastAdded = i.getDocRef();
                                    break;
                                case MODIFIED:
                                    contentListAdapter.update(i);
                                    break;
                                case REMOVED:
                                    contentListAdapter.remove(i);
                                    break;
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        contentListAdapter.notifyDataSetChanged();
                        // After populating, notify user if the list is empty
                        if (contentListAdapter.getItemCount() > 0)
                            empty_list_prompt.setVisibility(View.GONE);
                        else
                            empty_list_prompt.setVisibility(View.VISIBLE);
                    }
                });
    }
}
