package com.android.sebiya.simplearrayadapter.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class CollectionsUtils {

    public interface Condition<T> {

        boolean checkCondition(T t);
    }

    private CollectionsUtils() {
    }

    public static long[] toLongArray(@NonNull Collection<Long> collection) {
        long[] jArr = new long[collection.size()];
        Iterator<Long> it = collection.iterator();
        for (int i = 0; i < jArr.length; i++) {
            jArr[i] = it.next();
        }
        return jArr;
    }

    public static List<Long> toLongList(long[] longs) {
        List<Long> list = new ArrayList<>();
        if (longs != null) {
            for (long item : longs) {
                list.add(item);
            }
        }
        return list;
    }
}
