package com.example.yangliu.fridgemate.fridge_family;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.yangliu.fridgemate.Fridge;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.example.yangliu.fridgemate.current_contents.AddItemManual;
import com.example.yangliu.fridgemate.current_contents.RecyclerItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FridgeFamilyFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout constraintLayout;
    private View mProgressView;
    private RecyclerView mfridgeListView;

    private MemberListAdapter memberListAdapter;
    private FridgeListAdapter fridgeListAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userDoc;

    private int shortAnimTime;

    public FridgeFamilyFragment() {  }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fridge_family, container, false);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        final String email = user.getEmail();
        shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Create fridge if user has no fridges
        userDoc = db.collection("Users").document(email);
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

                        db.collection("Fridges")
                                .add(fridgeData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    public void onSuccess(DocumentReference documentReference) {
                                        List<DocumentReference> fridges = new ArrayList<DocumentReference>();
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
                                                        syncList();
                                                    }
                                                });
                                    }
                                });
                    }
                }
            }
        });


        mProgressView = view.findViewById(R.id.load_fridge_progress);
        constraintLayout = view.findViewById(R.id.cl);

        // fridge list set up
        mfridgeListView = (RecyclerView) view.findViewById(R.id.fridgeList);
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
                SaveSharedPreference.setCurrentFridge(getContext(),position);

                // Update current fridge in database
                if(position < fridgeListAdapter.mFridges.size()){
                    DocumentReference newCurrentFridge = db.collection("Fridges")
                            .document(fridgeListAdapter.mFridges.get(position).getFridgeid());
                    userDoc.update("currentFridge", newCurrentFridge);
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if (position != fridgeListAdapter.getItemCount() - 1) {
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    popup.getMenuInflater().inflate(R.menu.menu_fridge, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        DocumentReference selectedFridge = db.collection("Fridges")
                                .document(fridgeListAdapter.mFridges.get(position).getFridgeid());
                        public boolean onMenuItemClick(MenuItem item) {
                            // Get reference to selected fridge
                            switch (item.getItemId()) {
                                case R.id.leave_fridge:
                                    if(fridgeListAdapter.getItemCount() == 2){
                                        Toast.makeText(getContext(), "@string/one_fridge_error", Toast.LENGTH_SHORT).show();
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
                                            syncList();
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
        memberListAdapter = new MemberListAdapter(view.getContext());
        mRecyclerMemberView.setAdapter(memberListAdapter);
        mRecyclerMemberView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mRecyclerMemberView.addOnItemTouchListener(new RecyclerItemClickListener(FridgeFamilyFragment.this, mRecyclerMemberView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO:: check someone's profile
                Intent intent = new Intent(view.getContext(), MemberProfileActivity.class);
//                intent.putExtra();
                startActivity(intent);
//                Bundle extras = new Bundle();
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
                syncList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void leaveFridge(final DocumentReference fridge){
        fridge.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot fridgeData = task.getResult();

                    // Delete fridge if user is only member
                    final List<DocumentReference> members = (List)fridgeData.get("members");
                    if(members == null || members.isEmpty() || members.size() <= 1){
                        new AlertDialog.Builder(getContext())
                                .setTitle("Leave Fridge")
                                .setMessage(R.string.delete_fridge_warning)
                                .setIcon(R.drawable.ic_dialog_alert_material)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        removeFromFridgeList(fridge);
                                        fridge.delete();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    else{
                        new AlertDialog.Builder(getContext())
                                .setTitle("Leave Fridge")
                                .setMessage(R.string.leave_fridge_warning)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
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

    // Remove fridge from user's lists of fridges
    private void removeFromFridgeList(final DocumentReference fridge){
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    final DocumentReference currentFridge = userData.getDocumentReference("currentFridge");

                    List<DocumentReference> fridges;
                    if(userData.get("fridges") != null){
                        fridges = (List)userData.get("fridges");

                        fridges.remove(fridge);
                        // Set new current fridge if deleting the current one
                        if(currentFridge.equals(fridge) && !fridges.isEmpty()){
                            userDoc.update("currentFridge", fridges.get(0));
                        }
                        else if(currentFridge.equals(fridge) && fridges.isEmpty()){
                            userDoc.update("currentFridge", null);
                        }

                        // Update fridge list display once removed
                        userDoc.update("fridges", fridges)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        syncList();
                                    }
                                });
                    }
                }
            }
        });
    }

    public void syncList(){
        showProgress(true);
        // TODO:: DATABASE: populate fridge member list
        int currentFridge = SaveSharedPreference.getCurrentFridge(getContext());
        final List<Fridge> userFridges = new ArrayList<>();

        DocumentReference userDoc = db.collection("Users").document(user.getEmail());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                                    DocumentSnapshot fridgeData = task.getResult();

                                    userFridges.add(new Fridge(fridgeData.getId(),
                                            fridgeData.getString("fridgeName"),
                                            "test"));

                                    fridgeListAdapter.setItems(userFridges);

                                    // Highlight current fridge
                                    if(currentFridge.equals(ref)){
                                        fridgeListAdapter.selectedItemPos = userFridges.size() - 1;
                                    }

                                    showProgress(false);
                                }
                            });

                        }
                    }
                }
            }
        });

    }

    // Refresh every time activity comes into focus
    @Override
    public void onResume(){
        super.onResume();
        syncList();
    }

    // Cancel active tasks when fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        showProgress(false);
    }

    /**
     * Hide fridge interface while loading fridges.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            mfridgeListView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
            mfridgeListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mfridgeListView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mfridgeListView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
