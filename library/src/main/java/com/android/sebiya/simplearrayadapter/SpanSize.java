package com.android.sebiya.simplearrayadapter;

import android.support.v7.widget.RecyclerView.Adapter;

public interface SpanSize {
    int getSpanSize(Adapter adapter, int position, int layoutSpanSize);

}
