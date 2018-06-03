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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.Fridge;
import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.MainActivity;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.SplashActivity;
import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.example.yangliu.fridgemate.current_contents.receipt_scan.OcrCaptureActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ContentScrollingFragment extends Fragment implements FridgeItemTouchHelper.FridgeItemTouchHelpListener{

    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    public static final int NEW_OCR_ACTIVITY_REQUEST_CODE = 3;
    public static final int RECIPE_ACTIVITY_REQUEST_CODE = 4;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton addManual, addOCR;
    private ImageView recipeBtn;



    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private DocumentReference fridgeDoc;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        constraintLayout = view.findViewById(R.id.cl);

        storage = FirebaseStorage.getInstance();
        fridgeDoc = MainActivity.fridgeDoc;

        // List view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        // Connect list to its adapter
        recyclerView.setAdapter(((MainActivity) getActivity()).adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // on item Click (modifying item)
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ContentScrollingFragment.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(view.getContext(), AddItemManual.class);
                Bundle extras = new Bundle();
                FridgeItem i = ((MainActivity) getActivity()).adapter.mItemsOnDisplay.get(position);
                extras.putString("name",i.getItemName());
                extras.putString("expDate",i.getExpDate());
                extras.putString("image",i.getImage().toString());
                extras.putByteArray("imageCache",i.getImageCache());
                extras.putString("docRef",i.getDocRef());
                intent.putExtras(extras);
                getActivity().setResult(RESULT_OK, intent);
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
            }
        });
        addOCR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OcrCaptureActivity.class);
                startActivityForResult(intent, NEW_OCR_ACTIVITY_REQUEST_CODE);

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // set up the search button for content list view
        setHasOptionsMenu(true);
        // the fragment won't get pushed up by the keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scrolling, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search) return false;

        //Floating action button for recipe suggest
        Intent intent = new Intent(getActivity(), RecipeSuggestion.class);
        String toSearch = "";
        if (((MainActivity) getActivity()).adapter.mItems != null) {
            for (FridgeItem i : ((MainActivity) getActivity()).adapter.mItems) {
                toSearch += i.getItemName() + ',';
            }
        }
        intent.putExtra("search string", toSearch);
        startActivityForResult(intent, RECIPE_ACTIVITY_REQUEST_CODE);


        return super.onOptionsItemSelected(item);
    }

    // Search button & Recipe button set up
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(ContentScrollingFragment.this.getActivity().SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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
                        ((MainActivity) getActivity()).adapter.filterList(newText);
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

            String id = ((MainActivity) getActivity()).adapter.mItemsOnDisplay.get(position).getDocRef();
            final DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(id);

            final boolean[] deletePermananetly = {true};

            ((MainActivity) getActivity()).adapter.remove(position);
            // TODO:: DATABASE restore the item deletion (by delay or make a temporary copy)
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(constraintLayout, "Item removed!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // undo is selected, restore the deleted item
                    ((MainActivity) getActivity()).adapter.restore();
                    ((MainActivity) getActivity()).adapter.notifyDataSetChanged();
                    deletePermananetly[0] = false;
                    return;
                }
            });
            snackbar.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (deletePermananetly[0])
                        itemDoc.delete();
                }
            }, 3000);

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
            // Toast.makeText(getContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
        }
    }

    public void syncList(){
        // TODO:: DATABASE: populate the local list

        final List<FridgeItem> mItems = new LinkedList<>();
        if (fridgeDoc == null) return;
        fridgeDoc.collection("FridgeItems").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    Uri image = Uri.parse(String.valueOf(document.get("imageID")));
                    InputStream iStream = null;
                    byte[] inputData = null;
                    try {
                        iStream = getContext().getContentResolver().openInputStream(image);
                        inputData = getByteArray(iStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    FridgeItem i = new FridgeItem(String.valueOf(document.get("itemName")),
                            String.valueOf(document.get("expirationDate")),image, String.valueOf(document.getId()),inputData);
                    mItems.add(i);
                }
                ((MainActivity) getActivity()).adapter.setItems(mItems);
                ((MainActivity) getActivity()).adapter.notifyDataSetChanged();
            }
        });
        // global current fridge position in the fridge list
        //int currentFridge = SaveSharedPreference.getCurrentFridge(getContext());
    }

    public byte[] getByteArray(InputStream inputStream) throws IOException {
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
