package com.example.yangliu.fridgemate.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.yangliu.fridgemate.SaveSharedPreference;

import java.util.List;

public class LocalDataManager {

    private FridgeItemDao mItemDao;
    private LiveData<List<FridgeItem>> mAllItems;

    LocalDataManager(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mItemDao = db.fridgeItemDao();
        mAllItems = mItemDao.getAllItems();

    }

    LiveData<List<FridgeItem>> getAllItems() {
        return mAllItems;
    }


    public void insert (FridgeItem item) {
        new insertAsyncTask(mItemDao).execute(item);
    }

    public void removeItem(FridgeItem item) {
        new deleteAsyncTask(mItemDao).execute(item);
    }

    public void updateItem(FridgeItem item) { new updateAsyncTask(mItemDao).execute(item); }

    public void restoreItem(FridgeItem item) {
        new insertAsyncTask(mItemDao).execute(item);
    }

    private static class updateAsyncTask extends AsyncTask<FridgeItem, Void, Void> {

        private FridgeItemDao mAsyncTaskDao;

        updateAsyncTask(FridgeItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(FridgeItem... fridgeItems) {
            FridgeItem item = fridgeItems[0];
            mAsyncTaskDao.setItem(item.getItemName(),item.getExpDate(),item.getImage());
            return null;
        }
    }

    private static class insertAsyncTask extends AsyncTask<FridgeItem, Void, Void> {

        private FridgeItemDao mAsyncTaskDao;

        insertAsyncTask(FridgeItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FridgeItem... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<FridgeItem, Void, Void> {

        private FridgeItemDao mAsyncTaskDao;

        deleteAsyncTask(FridgeItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FridgeItem... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    // repopulate classes
    static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        // a class for repopulating the database
        private final FridgeItemDao  mDao;

        PopulateDbAsync(AppDatabase db) {
            mDao = db.fridgeItemDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            FridgeItem item= new FridgeItem ("Apple (Sample)","08/31/96");
            mDao.insert(item);
            item = new FridgeItem ("Banana (Sample)");
            mDao.insert(item);
            return null;
        }
    }
}