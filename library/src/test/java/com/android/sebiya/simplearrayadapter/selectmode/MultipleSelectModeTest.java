package com.android.sebiya.simplearrayadapter.selectmode;


import junit.framework.Assert;

import org.junit.Test;

public class MultipleSelectModeTest {
    private MultipleSelectMode selectMode = new MultipleSelectMode(null);

    @Test
    public void test_100_toggle_select() {
        selectMode.toggleSelect(10);
        Assert.assertTrue(selectMode.isSelected(10));
        Assert.assertFalse(selectMode.isSelected(100));
    }

    @Test
    public void test_200_get_selected_item_count() {
        selectMode.toggleSelect(1);
        selectMode.toggleSelect(2);
        selectMode.toggleSelect(3);
        selectMode.toggleSelect(4);

        Assert.assertEquals(4, selectMode.getSelectedItemCount());

        selectMode.toggleSelect(1);
        Assert.assertEquals(3, selectMode.getSelectedItemCount());
    }
}
