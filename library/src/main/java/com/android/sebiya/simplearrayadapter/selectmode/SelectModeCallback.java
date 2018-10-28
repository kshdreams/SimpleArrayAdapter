package com.android.sebiya.simplearrayadapter.selectmode;

import android.view.ActionMode;
import android.view.Menu;


public abstract class SelectModeCallback implements ActionMode.Callback {

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
