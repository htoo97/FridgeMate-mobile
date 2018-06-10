package com.fridgemate.yangliu.fridgemate.fridge_family;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fridgemate.yangliu.fridgemate.Fridge;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.SaveSharedPreference;
import com.fridgemate.yangliu.fridgemate.current_contents.RecyclerItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeDoc;
import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeListAdapter;
import static com.fridgemate.yangliu.fridgemate.MainActivity.memberListAdapter;


public class FridgeFamilyFragment extends Fragment {

    protected static SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mfridgeListView;

    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userDoc;


    public FridgeFamilyFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fridge_family, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userDoc = setUpDatabase();

        // fridge list set up
        mfridgeListView = (RecyclerView) view.findViewById(R.id.fridgeList);
        mfridgeListView.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mfridgeListView.setLayoutManager(MyLayoutManager);
        mfridgeListView.setAdapter(MainActivity.fridgeListAdapter);
        //  on item Click:: change current fridge
        mfridgeListView.addOnItemTouchListener(
                new RecyclerItemClickListener(FridgeFamilyFragment.this, mfridgeListView,
                        new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == fridgeListAdapter.selectedItemPos)
                    return;
                // only when it is not the adding-member footer
                if (position != fridgeListAdapter.getItemCount() - 1) {
                    // change focus color
                    int oldSelectedPos = fridgeListAdapter.selectedItemPos;
                    fridgeListAdapter.selectedItemPos = position;
                    if (oldSelectedPos != -1) {
                        fridgeListAdapter.notifyItemChanged(position);
                        fridgeListAdapter.notifyItemChanged(oldSelectedPos);
                    }
                    SaveSharedPreference.setCurrentFridge(getContext(), position);

                    // allow auto sync the content list, shopping list for once
                    MainActivity.shopListSync = MainActivity.contentSync = true;

                    // Update current fridge in database
                    if (fridgeListAdapter.mFridges != null &&
                            position < fridgeListAdapter.mFridges.size()) {
                        final DocumentReference newCurrentFridgeDoc = db.collection("Fridges")
                                .document(fridgeListAdapter.mFridges.get(position).getFridgeid());

                        MainActivity.fridgeDoc = newCurrentFridgeDoc;
                        memberListAdapter.fridgeDoc = newCurrentFridgeDoc;
                        memberListAdapter.syncMemberList();
                        userDoc.update("currentFridge", newCurrentFridgeDoc);

                    }
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if (position != fridgeListAdapter.getItemCount() - 1) {
                    PopupMenu popup = new PopupMenu(Objects.requireNonNull(getContext()), view);
                    popup.getMenuInflater().inflate(R.menu.menu_fridge, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        DocumentReference selectedFridge = db.collection("Fridges")
                                .document(fridgeListAdapter.mFridges.get(position).getFridgeid());
                        public boolean onMenuItemClick(MenuItem item) {
                            // Get reference to selected fridge
                            switch (item.getItemId()) {
                                case R.id.leave_fridge:
                                    if(fridgeListAdapter.getItemCount() == 2){
                                        Toast.makeText(getContext(), R.string.one_fridge_error, Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    // Remove user from the fridge, delete fridge if they are the only one left
                                    leaveFridge(selectedFridge);
                                    return true;
                                case R.id.rename_fridge:
                                    String oldName = fridgeListAdapter.mFridges.get(position).getFridgeName();
                                    final String[] newName = new String[1];
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Rename your " + oldName);

                                    // Set up the input
                                    final EditText input = new EditText(getContext());
                                    LinearLayout linearLayout = new LinearLayout(getContext());
                                    LinearLayout.LayoutParams layoutParams =
                                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    input.setLayoutParams(layoutParams);
                                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                                    //layoutParams.gravity = Gravity.CENTER;
                                    linearLayout.addView(input);
                                    linearLayout.setPadding(40, 0, 40, 0);
                                    builder.setView(linearLayout);
                                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            newName[0] = input.getText().toString();
                                            selectedFridge.update("fridgeName",newName[0]);
                                            syncFridgeList();
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();
                                    return true;
                                case R.id.copyId:
                                    ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Your Fridge ID", fridgeDoc.getId());
                                    assert clipboard != null;
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Fridge ID copied to clipboard.", Toast.LENGTH_SHORT).show();
                                default:
                                    return true;
                            }

                        }
                    });
                    popup.show();
                }
            }
        }));

        // for presentation: using the items as member adapters
        RecyclerView mRecyclerMemberView = view.findViewById(R.id.fridgeMemberList);
        final LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        mRecyclerMemberView.setLayoutManager(llm);
        mRecyclerMemberView.setAdapter(memberListAdapter);

        // set up entrance animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.fall_in_layout);
        mRecyclerMemberView.setLayoutAnimation(animation);

        // check someone's profile
        mRecyclerMemberView.addOnItemTouchListener(new RecyclerItemClickListener(
                FridgeFamilyFragment.this, mRecyclerMemberView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                // if it is not the footer "add member" button
                if (position != memberListAdapter.getItemCount() - 1) {

                    Intent intent = new Intent(view.getContext(), MemberProfileActivity.class);
                    if (memberListAdapter.names.get(position) == null || memberListAdapter.names.get(position).get() == null)
                        return;
                    intent.putExtra("memberId", memberListAdapter.names.get(position).getId());
                    // get image cache
                    Bitmap b = llm.findViewByPosition(position).findViewById(R.id.member_image).getDrawingCache();

                    // if b has alpha that means this member doesn't have profile photo yet
                    if (b != null && !b.hasAlpha()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageInByte = baos.toByteArray();
                        intent.putExtra("photo",imageInByte);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Apply activity transition
                        // inside your activity (if you did not enable transitions in your theme)
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    } else {
                        // Swap without transition
                        startActivity(intent);
                    }

                }
            }

            // remove a member from your fridge
            @Override
            public void onItemLongClick(View view, final int position) {
                if (position != memberListAdapter.getItemCount() - 1) {
                    // if the position is not footer Nor yourself

                    final DocumentReference memberToBeDeleted = memberListAdapter.names.get(position);

                    // if member is something werid, not a document reference
                    if (memberToBeDeleted == null){
                        final DocumentReference selectedFridge = db.collection("Fridges")
                                .document(fridgeListAdapter.mFridges.get(fridgeListAdapter.selectedItemPos).getFridgeid());
                        memberListAdapter.names.remove(position);
                        selectedFridge.update("members", memberListAdapter.names);
                    }else {
                        PopupMenu popup = new PopupMenu(Objects.requireNonNull(getContext()), view);
                        popup.getMenuInflater().inflate(R.menu.menu_for_item, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                // position is the index of the member (to be deleted)
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Remove Member")
                                        .setMessage("Are you sure to remove " + memberToBeDeleted.getId() + "?")
                                        .setIcon(R.drawable.ic_dialog_alert_material)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // if you are removing yourself
                                                if (memberListAdapter.names.get(position).getId().equals(userDoc.getId()))
                                                    leaveFridge(fridgeDoc);
                                                else {
                                                    memberListAdapter.names.remove(position);
                                                    memberListAdapter.notifyDataSetChanged();
                                                    // remove user from the member's list
                                                    final DocumentReference selectedFridge = db.collection("Fridges")
                                                            .document(fridgeListAdapter.mFridges.get(fridgeListAdapter.selectedItemPos).getFridgeid());


                                                    selectedFridge.update("members", memberListAdapter.names);

                                                    // remove the fridge from the user to be removed
                                                    memberToBeDeleted.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            List<DocumentReference> fridges = (List<DocumentReference>) task.getResult().get("fridges");
                                                            if (fridges == null || fridges.size() == 0)
                                                                return;
                                                            fridges.remove(selectedFridge);
                                                            memberToBeDeleted.update("fridges", fridges);
                                                        }
                                                    });
                                                }

                                            }
                                        }).setNegativeButton(android.R.string.no, null).show();
                                return true;
                            }
                        });
                        popup.show();
                    }
                }
            }
        }));

        // set up refresh button
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                syncBothLists();
            }
        });


        // avoid abusive syncing
        if (MainActivity.familySync){
            if (userDoc == null)
                Toast.makeText(getContext(), "User Doc ref error", Toast.LENGTH_SHORT).show();
            else
                syncBothLists();
            MainActivity.familySync = false;
        }
        else
            MainActivity.showProgress(false);

        return view;
    }

    private void setupFirstFridge(){
        // Create fridge if user has no fridges
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();

                    // Perform first-time fridge setup
                    if(userData.get("currentFridge") == null
                            && ( userData.get("fridges") == null || ((List)userData.get("fridges")).isEmpty() )) {
                        Toast.makeText(getContext(), R.string.fridge_setup_message, Toast.LENGTH_SHORT).show();
                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", "My Fridge");
                        fridgeData.put("owner", userDoc);
                        List<DocumentReference> members = new ArrayList<DocumentReference>();
                        members.add(userDoc);
                        fridgeData.put("members", members);
                        SaveSharedPreference.setCurrentFridge(getContext(),0);

                        db.collection("Fridges")
                                .add(fridgeData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    public void onSuccess(DocumentReference documentReference) {
                                        List fridges = new ArrayList<DocumentReference>();
                                        if(userData.get("fridges") != null){
                                            fridges = (List)userData.get("fridges");
                                        }

                                        fridges.add(documentReference);

                                        userDoc.update(
                                                "currentFridge", documentReference,
                                                "fridges", fridges)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(),
                                                                R.string.fridge_setup_complete, Toast.LENGTH_LONG).show();
                                                        syncFridgeList();
                                                    }
                                                });
                                    }
                                });
                    }
                }
            }
        });
    }

    private void syncBothLists(){
        syncFridgeList();
        memberListAdapter.syncMemberList();
    }
    private void leaveFridge(final DocumentReference fridge){
        fridge.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot fridgeData = task.getResult();

                    // Delete fridge if user is only member
                    final List members = (List)fridgeData.get("members");
                    if(members == null || members.isEmpty() || members.size() <= 1){
                        new AlertDialog.Builder(getContext())
                                .setTitle("Leave Fridge")
                                .setMessage(R.string.delete_fridge_warning)
                                .setIcon(R.drawable.ic_dialog_alert_material)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        // remove the image of all items
                                        fridge.collection("FridgeItems").get().addOnCompleteListener(
                                                new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        QuerySnapshot q = task.getResult();
                                                        for (DocumentSnapshot dr : q.getDocuments()) {
                                                            String imageUri = (String) dr.get("imageID");
                                                            if (imageUri != null && !imageUri.equals("") && !imageUri.equals("null"))
                                                                MainActivity.storage.getReferenceFromUrl(imageUri).delete();

                                                            // delete the fridge item document, not recommended
                                                            dr.getReference().delete();
                                                        }
                                                        // just delete fridge from your data
                                                        fridge.delete();

                                                        // remove the fridge and set another current fridge
                                                        removeFromFridgeList(fridge);

                                                        // update the fridge locally
                                                        syncFridgeList();
//                                                        // remove and update the fridge locally
//                                                        fridgeListAdapter.mFridges.remove(fridge);
//                                                        if(fridgeListAdapter.getItemCount() > 0)
//                                                            fridgeListAdapter.selectedItemPos = 0;
//                                                        else
//                                                            fridgeListAdapter.selectedItemPos = -1;
//                                                        fridgeListAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                        );

                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    else{
                        new AlertDialog.Builder(getContext())
                                .setTitle("Leave Fridge")
                                .setMessage(R.string.leave_fridge_warning)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // remove the fridge
                                        removeFromFridgeList(fridge);

                                        // Remove user from fridge's members
                                        members.remove(userDoc);
                                        fridge.update("members", members);
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                }
            }
        });
    }

    // leave fridge on firebase
    private void removeFromFridgeList(final DocumentReference fridge){
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    final DocumentReference currentFridge = userData.getDocumentReference("currentFridge");

                    List fridges;
                    if(userData.get("fridges") != null){
                        fridges = (List)userData.get("fridges");

                        assert fridges != null;
                        fridges.remove(fridge);
                        // Set new current fridge if deleting the current one
                        if(currentFridge.equals(fridge) && !fridges.isEmpty()){
                            userDoc.update("currentFridge", fridges.get(0));
                        }
                        else if(currentFridge.equals(fridge) && fridges.isEmpty()){
                            userDoc.update("currentFridge", null);
                        }

                        MainActivity.contentSync = MainActivity.shopListSync = true;

                        // Update fridge list display once removed
                        userDoc.update("fridges", fridges)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        syncBothLists();
                                    }
                                });
                    }
                }
            }
        });
    }

    // sync fridge list
    public static void syncFridgeList(){
        //  int currentFridge = SaveSharedPreference.getCurrentFridge(getContext());
        final List<Fridge> userFridges = new ArrayList<>();

        MainActivity.userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    final DocumentReference currentFridge = userData.getDocumentReference("currentFridge");

                    List<DocumentReference> fridges;

                    if(userData.get("fridges") != null){
                        fridges = (List)userData.get("fridges");

                        for(final DocumentReference ref : fridges){
                            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                public void onComplete(Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        DocumentSnapshot fridgeData = task.getResult();

                                        userFridges.add(new Fridge(fridgeData.getId(),
                                                fridgeData.getString("fridgeName")));

                                        fridgeListAdapter.setItems(userFridges);

                                        // Highlight current fridge
                                        if (currentFridge != null && currentFridge.equals(ref)) {
                                            fridgeListAdapter.selectedItemPos = userFridges.size() - 1;
                                        }

                                        if (swipeRefreshLayout != null)
                                            swipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    // Set up database functions when app opened
    private DocumentReference setUpDatabase(){
        if (user == null){
            Toast.makeText(getContext(), R.string.error_load_data,
                    Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            return null;
        }

        final String email = user.getEmail();

        assert email != null;
        DocumentReference documentReference = db.collection("Users").document(email);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Create document for user if doesn't already exist
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);

                        db.collection("Users").document(email)
                                .set(userData);
                    }
                } else {
                    Log.d("set_up_database", "get failed with ", task.getException());
                }
            }
        });

        userDoc = documentReference;
        setupFirstFridge();
        return documentReference;
    }
}
