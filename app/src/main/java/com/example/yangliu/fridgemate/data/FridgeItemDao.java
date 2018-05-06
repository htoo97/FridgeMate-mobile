package com.example.yangliu.fridgemate.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FridgeItemDao {

    @Insert
    void insert(FridgeItem item);

    @Delete
    void delete(FridgeItem item);

    @Query("DELETE FROM fridge")
    void deleteAll();

    @Query("UPDATE fridge SET exp_date= :date, image= :image WHERE item_name = :name")
    public abstract int setItem(String name, String date, byte[] image);

    // TODO:: fridge contents order
    // LiveData: data observation tool
    @Query("SELECT * from fridge ORDER BY item_id ASC")
    LiveData<List<FridgeItem>> getAllItems();
}
