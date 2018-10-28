package com.android.sebiya.simplearrayadapter;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class SimpleArrayAdapter<T> extends AbsArrayAdapter<T, AbsArrayAdapter.ViewHolderInternal> {

    private SimpleArrayAdapter(AbsArrayAdapter.Builder<T, ? extends AbsArrayAdapter.Builder> builder) {
        super(builder);
    }

    @Override
    protected ViewHolderInternal onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType, View view) {
        return new ViewHolderInternal(view, this);
    }

    public static <T> Builder<T> with(Activity activity) {
        return new Builder<>(activity);
    }

    public static class Builder<T> extends AbsArrayAdapter.Builder<T, Builder<T>> {

        protected Builder<T> self() {
            return this;
        }

        Builder(Activity activity) {
            super(activity);
        }

        public SimpleArrayAdapter<T> build() {
            return new SimpleArrayAdapter<>(this);
        }
    }

}
