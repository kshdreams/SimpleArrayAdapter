package com.android.sebiya.simplearrayadapter.selectmode;

import android.support.annotation.NonNull;
import android.view.ActionMode.Callback;

public abstract class AbsSelectMode implements SelectMode {

    private final Callback mCallback;

    public AbsSelectMode(SelectModeCallback callback) {
        this.mCallback = callback;
    }

    public Callback getActionModeCallback() {
        return this.mCallback;
    }

    public int getSelectedItemCount() {
        long[] selectedItems = getSelectedItemIds();
        if (selectedItems == null) {
            return 0;
        }
        return selectedItems.length;
    }
}