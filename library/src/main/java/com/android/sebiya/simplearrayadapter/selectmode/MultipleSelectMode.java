package com.android.sebiya.simplearrayadapter.selectmode;

import com.android.sebiya.simplearrayadapter.utils.CollectionsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultipleSelectMode extends AbsSelectMode {

    private HashMap<Long, Boolean> mSelectMap = new HashMap<>();

    public MultipleSelectMode(SelectModeCallback callback) {
        super(callback);
    }

    public boolean isSelected(long id) {
        return mSelectMap.containsKey(id) && mSelectMap.get(id);
    }

    public void toggleSelect(long id) {
        this.mSelectMap.put(id, !isSelected(id));
    }

    public void clearItems() {
        this.mSelectMap.clear();
    }

    @Override
    public long[] getSelectedItemIds() {
        List<Long> arrayList = new ArrayList<>();
        Set<Long> keySet = mSelectMap.keySet();
        for (Long key : keySet) {
            if (mSelectMap.get(key)) {
                arrayList.add(key);
            }
        }
        return CollectionsUtils.toLongArray(arrayList);
    }
}