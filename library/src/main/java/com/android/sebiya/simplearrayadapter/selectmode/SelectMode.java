package com.android.sebiya.simplearrayadapter.selectmode;

public interface SelectMode {
    void clearItems();

    long[] getSelectedItemIds();

    boolean isSelected(long itemId);

    void toggleSelect(long itemId);
}
