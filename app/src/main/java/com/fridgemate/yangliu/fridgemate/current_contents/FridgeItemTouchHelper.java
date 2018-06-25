package com.fridgemate.yangliu.fridgemate.current_contents;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by ravi on 29/09/17. Modified by Yang
 */

public class FridgeItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private FridgeItemTouchHelpListener listener;

    FridgeItemTouchHelper(int dragDirs, int swipeDirs, FridgeItemTouchHelpListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ContentListAdapter.ItemViewHolder) viewHolder).viewForeground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ContentListAdapter.ItemViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ContentListAdapter.ItemViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ContentListAdapter.ItemViewHolder) viewHolder).viewForeground;

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);

        // set visibility of the two options (delete and move to shopping list)
        final ImageView d =  ((ContentListAdapter.ItemViewHolder) viewHolder).toDelete;
        final ImageView s =  ((ContentListAdapter.ItemViewHolder) viewHolder).toShoplist;
        if (dX < 0){
            if (s.getVisibility() == View.VISIBLE)
                s.setVisibility(View.GONE);
            if (d.getVisibility() == View.GONE)
                d.setVisibility(View.VISIBLE);
        }else {
            if (d.getVisibility() == View.VISIBLE)
                d.setVisibility(View.GONE);
            if (s.getVisibility() == View.GONE)
                s.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }


    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface FridgeItemTouchHelpListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

}

//https://www.androidhive.info/2017/09/android-recyclerview-swipe-delete-undo-using-itemtouchhelper/