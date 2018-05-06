package com.example.yangliu.fridgemate.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.yangliu.fridgemate.LoginActivity;
import com.example.yangliu.fridgemate.SaveSharedPreference;

import java.util.List;

public class FridgeItemViewModel extends AndroidViewModel {

    private LocalDataManager mManager;

    private LiveData<List<FridgeItem>> mAllItems;

    public FridgeItemViewModel (Application application) {
        super(application);
        mManager = new LocalDataManager(application);
        mAllItems = mManager.getAllItems();
    }

    public LiveData<List<FridgeItem>> getAllItems() { return mAllItems; }

    // TODO:: no notification in each function?
    //notifyItemRemoved(position);
    public void updateItemInfo(FridgeItem item) { mManager.updateItem(item); }
    public void insert(FridgeItem item) { mManager.insert(item); }
    public void removeItem(FridgeItem item) { mManager.removeItem(item); }
    public void restoreItem(FridgeItem item) { mManager.restoreItem(item); }
}