package com.fridgemate.yangliu.fridgemate.fridge_family;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
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
import com.fridgemate.yangliu.fridgemate.RedirectToLogInActivity;
import com.fridgemate.yangliu.fridgemate.SaveSharedPreference;
import com.fridgemate.yangliu.fridgemate.current_contents.RecyclerItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeDoc;
import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeListAdapter;
import static com.fridgemate.yangliu.fridgemate.MainActivity.memberListAdapter;
import static com.fridgemate.yangliu.fridgemate.MainActivity.user;
import static com.fridgemate.yangliu.fridgemate.MainActivity.userDoc;


public class FridgeFamilyFragment extends Fragment {

    protected static SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore db;

    private ListenerRegistration memberListRegistration;
    private Context context;

    public FridgeFamilyFragment() { }

    private RecyclerView mRecyclerMemberView, mfridgeListView ;
    final int REQUEST_NEW_ACCOUNT = 233;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fridge_family, container, false);
        db = FirebaseFirestore.getInstance();

        // fridge list set up
        mfridgeListView = (RecyclerView) view.findViewById(R.id.fridgeList);
        mfridgeListView.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mfridgeListView.setLayoutManager(MyLayoutManager);
        mfridgeListView.setAdapter(MainActivity.fridgeListAdapter);
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.fall_from_left_layout);
        mfridgeListView.setLayoutAnimation(animation);
        mfridgeListView.scheduleLayoutAnimation();
        setupFridgeOnClickListener(mfridgeListView);

        // member list set up
        mRecyclerMemberView = view.findViewById(R.id.fridgeMemberList);
        final LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        mRecyclerMemberView.setLayoutManager(llm);
        mRecyclerMemberView.setAdapter(memberListAdapter);
        // set up entrance animation
        LayoutAnimationController animation1 = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.fall_in_layout);
        mRecyclerMemberView.setLayoutAnimation(animation1);
        mRecyclerMemberView.scheduleLayoutAnimation();
        setupMemberOnClickListener(mRecyclerMemberView,llm);

        // set up refresh button
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,R.color.green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                syncBothLists();
            }
        });

        context = getContext();
        if (!user.isAnonymous()) {
            checkInvitations();
        }
        setUpRealTimeListener();

        return view;
    }

    private void syncBothLists(){
        syncFridgeList();
        memberListAdapter.syncMemberList();
    }
    private void leaveFridge(final DocumentReference fridge){

        // Remove usemr from the fridge, delete fridge if they are the only one left
        if(fridgeListAdapter.getItemCount() == 2){
            Toast.makeText(getContext(), R.string.one_fridge_error, Toast.LENGTH_SHORT).show();
            return;
        }

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
                                                        fridge.delete().addOnCompleteListener(
                                                                new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        // remove the fridge and set another current fridge
                                                                        removeFromFridgeList(fridge);

                                                                        // update the fridge locally
                                                                        syncFridgeList();
                                                                    }
                                                                }
                                                        );

                                                    }
                                                }
                                        );

                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    else{
                        // remove user from the fridge (user is not the only member)
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

    // leave fridge regarding firebase and realtime listener
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
                            fridgeDoc = (DocumentReference) fridges.get(0);
                        }
                        else if(currentFridge.equals(fridge) && fridges.isEmpty()){
                            userDoc.update("currentFridge", null);
                            fridgeDoc = null;
                        }

                        // Update fridge list display once removed
                        userDoc.update("fridges", fridges)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        syncBothLists();
                                        setupMemberListRealtimeListener();
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
        final ArrayList<Fridge> userFridges = new ArrayList<>();

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    final DocumentReference currentFridge = userData.getDocumentReference("currentFridge");

                    ArrayList<DocumentReference> fridges;

                    if(userData.get("fridges") != null){
                        fridges = (ArrayList)userData.get("fridges");

                        assert fridges != null;
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

    public void setUpRealTimeListener(){
        swipeRefreshLayout.setRefreshing(true);
        if (fridgeDoc == null) {
            if (userDoc == null)
                Toast.makeText(getContext(), R.string.connecting, Toast.LENGTH_SHORT).show();
            else {
                userDoc.get().addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    fridgeDoc = task.getResult().getDocumentReference("currentFridge");
                                    setupMemberListRealtimeListener();
                                } else {
                                    Toast.makeText(getContext(), R.string.connecting, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        }
        else{
            setupMemberListRealtimeListener();
        }
        setupFridgeListRealTimeListener();
    }

    private void setupMemberListRealtimeListener(){
        // detach the old memner list real time listener
        if (memberListRegistration != null)
            memberListRegistration.remove();

        mRecyclerMemberView.scheduleLayoutAnimation();

        memberListRegistration = fridgeDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                final String TAG = "Member list Listener";
                if (e != null) {
                    Log.w(TAG, "failed.", e);
                    return;
                }
//                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    // populate members
                    memberListAdapter.populateAdapter((List<DocumentReference>) snapshot.getData().get("members"));
                    
                    // check next joining request
                    if (!user.isAnonymous()) {
                        final DocumentReference requester = (DocumentReference) snapshot.getData().get("joinRequest");
                        if (requester != null && !requester.equals("null")) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Fridge Mate request")
                                    .setMessage(requester.getId() + " wants to join your fridge.")
                                    .setIcon(R.drawable.ic_ac_unit_black_24dp)
                                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            acceptRequest(requester);
                                        }
                                    })
                                    .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            rejectRequest();
                                        }
                                    }).show();
                        }
                    }

                } else {
//                    Log.d(TAG, source + " data: null");

                }
            }
        });
    }
    private void rejectRequest(){
        fridgeDoc.update("joinRequest",null).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), "Rejected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void acceptRequest(final DocumentReference requester){
        // Add user to fridge's members
        fridgeDoc.get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            List members = (List<DocumentReference>) task.getResult().get("members");
                            members.add(requester);
                            fridgeDoc.update("joinRequest",null,"members",members);
                        }
                    }
                }
        );

        // Add fridge to user's list of fridges
        requester.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot userData = task.getResult();
                List fridges = (List)userData.get("fridges");
                fridges.add(fridgeDoc);
                requester.update("fridges", fridges);
            }
        });

        Toast.makeText(context, getResources().getString(R.string.join_fridge_success), Toast.LENGTH_LONG).show();

    }

    private void setupFridgeListRealTimeListener(){
        mfridgeListView.scheduleLayoutAnimation();
        userDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                swipeRefreshLayout.setRefreshing(true);

                final String TAG = "fridge list Listener";
                if (e != null) {
                    Log.w(TAG, "failed.", e);
                    return;
                }

//                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    // sync the current fridge
                    final DocumentReference currentFridge = (DocumentReference) snapshot.getData().get("currentFridge");
                    MainActivity.fridgeDoc = currentFridge;

                    final List<Fridge> userFridges = new ArrayList<>();
                    for(final DocumentReference ref : (List<DocumentReference>) snapshot.getData().get("fridges")){

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
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                    }
                    memberListAdapter.syncMemberList();

                }
            }
        });
    }

    private void accceptInvitation(final DocumentReference newFridge){
        // add user to the member list on firebase
        newFridge.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> membersList = new ArrayList<>();
                    final DocumentSnapshot fridgeData = task.getResult();
                    if (fridgeData.get("members") != null) {
                        membersList.addAll(((List) Objects.requireNonNull(fridgeData.get("members"))));
                    }
                    membersList.add(userDoc);
                    newFridge.update("members", membersList);
                }
            }
        });

        // add fridge to the member's fridge list on firebase
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                List<DocumentReference> fridges = (List<DocumentReference>) task.getResult().get("fridges");
                assert fridges != null;
                fridges.add(newFridge);
                userDoc.update("fridges", fridges);
                Toast.makeText(getContext(), "Join successful!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkInvitations(){
        // check if there are invitations to user
        userDoc.get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot snapshot = task.getResult();
                            final List<DocumentReference> pendingInvites = (List<DocumentReference>) snapshot.getData().get("pendingInvites");

                            // not asking ones already joined (firebase handles duplicates)
                            final List<DocumentReference> currentFridges = (List<DocumentReference>) snapshot.getData().get("fridges");

                            if (pendingInvites != null && pendingInvites.size() > 0) {
                                for (int i = 0; i<pendingInvites.size(); ++i) {

                                    final DocumentReference newFridge = pendingInvites.get(i);
                                    // ignore the one already joind
                                    if (currentFridges.contains(newFridge)) continue;

                                    newFridge.get().addOnCompleteListener(
                                            new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot fridgeData = task.getResult();
                                                    String fridgeName = String.valueOf(fridgeData.get("fridgeName"));
                                                    DocumentReference owner = (DocumentReference) fridgeData.get("owner");

                                                    new AlertDialog.Builder(context)
                                                            .setTitle("New Fridge Invitation")
                                                            .setMessage(owner.getId() + " invited you to join " + fridgeName + '.')
                                                            .setIcon(R.drawable.ic_ac_unit_black_24dp)
                                                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                                    accceptInvitation(newFridge);
                                                                }
                                                            })
                                                            .setNegativeButton("Reject", null)
                                                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                                @Override
                                                                public void onCancel(DialogInterface dialog) {
                                                                    Toast.makeText(getContext(), "Rejected", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }).show();
                                                }
                                            }
                                    );
                                }
                                pendingInvites.clear();
                                userDoc.update("pendingInvites",pendingInvites);
                            }
                        }
                    }
                }
        );

    }

    private void setupMemberOnClickListener(RecyclerView mRecyclerMemberView, final LinearLayoutManager llm){
        // check someone's profile
        mRecyclerMemberView.addOnItemTouchListener(new RecyclerItemClickListener(
                FridgeFamilyFragment.this, mRecyclerMemberView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                // if it is not the footer "add member" button
                if (position != memberListAdapter.getItemCount() - 1) {

                    Intent intent = new Intent(view.getContext(), MemberProfileActivity.class);
                    if (memberListAdapter.names.get(position) == null)
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

                                                if (user.isAnonymous()){
                                                    Intent i = new Intent(getContext(), RedirectToLogInActivity.class);
                                                    startActivityForResult(i,REQUEST_NEW_ACCOUNT);
                                                    return;
                                                }

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

                                                            // if it is the current fridge
                                                            if (task.getResult().get("currentFridge").equals(selectedFridge)) {
                                                                if (fridges.size() == 0)
                                                                    memberToBeDeleted.update("currentFridge", null);
                                                                else {
                                                                    memberToBeDeleted.update("currentFridge", fridges.get(0));
                                                                }
                                                            }

                                                            memberToBeDeleted.update("fridges", fridges);
                                                            Toast.makeText(getActivity(), R.string.member_removed, Toast.LENGTH_SHORT).show();
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
    }

    private void setupFridgeOnClickListener(RecyclerView mfridgeListView){
        //  on item Click:: change current fridge
        mfridgeListView.addOnItemTouchListener(
                new RecyclerItemClickListener(FridgeFamilyFragment.this, mfridgeListView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (user.isAnonymous()){
                                    Intent i = new Intent(getContext(), RedirectToLogInActivity.class);
                                    startActivityForResult(i,REQUEST_NEW_ACCOUNT);
                                    return;
                                }

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
//                    MainActivity.shopListSync = MainActivity.contentSync = true;

                                    // Update current fridge in database
                                    if (fridgeListAdapter.mFridges != null &&
                                            position < fridgeListAdapter.mFridges.size()) {
                                        final DocumentReference newCurrentFridgeDoc = db.collection("Fridges")
                                                .document(fridgeListAdapter.mFridges.get(position).getFridgeid());

                                        MainActivity.fridgeDoc = newCurrentFridgeDoc;

                                        // (bug) double sync's this actually messes up Glide
                                        //memberListAdapter.syncMemberList();

                                        userDoc.update("currentFridge", newCurrentFridgeDoc);
                                        setUpRealTimeListener();
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

                                            if (user.isAnonymous()){
                                                Intent i = new Intent(getContext(), RedirectToLogInActivity.class);
                                                startActivityForResult(i,REQUEST_NEW_ACCOUNT);
                                                return false;
                                            }
                                            // Get reference to selected fridge
                                            switch (item.getItemId()) {
                                                case R.id.leave_fridge:
                                                    leaveFridge(selectedFridge);
                                                    return true;
                                                case R.id.rename_fridge:
                                                    String oldName = fridgeListAdapter.mFridges.get(position).getFridgeName();
                                                    final String[] newName = new String[1];
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Rename your " + oldName);

                                                    // Set up the input
                                                    final EditText input = new EditText(getContext());
                                                    input.setText(oldName);
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
    }
}
