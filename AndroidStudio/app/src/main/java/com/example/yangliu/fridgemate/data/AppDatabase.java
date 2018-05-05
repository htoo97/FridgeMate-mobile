package com.example.yangliu.fridgemate.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {FridgeItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FridgeItemDao fridgeItemDao();

    // Allowing only one database open
    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "item_database")
                            .build();

                    //TODO:: repopulate the database when first login
                    //.addCallback(sRoomDatabaseCallback).build();

                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new LocalDataManager.PopulateDbAsync(INSTANCE).execute();
                }
            };

}

