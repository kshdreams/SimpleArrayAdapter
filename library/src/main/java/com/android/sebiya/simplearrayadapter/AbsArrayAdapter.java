package com.android.sebiya.simplearrayadapter;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Checkable;

import com.android.sebiya.simplearrayadapter.selectmode.AbsSelectMode;
import com.android.sebiya.simplearrayadapter.selectmode.SelectMode;
import com.android.sebiya.simplearrayadapter.utils.CollectionsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbsArrayAdapter<T, VH extends AbsArrayAdapter.ViewHolderInternal> extends Adapter<VH>
        implements SelectMode {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    protected abstract VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType, View view);

    private ActionMode mActionMode;

    protected final Activity mActivity;

    private final SparseArray<ViewBinder<T>> mBinders;

    @IdRes
    private final int mCheckBoxIdRes;

    private SparseArray<Integer> mHeaderTypes = new SparseArray<>();

    private SparseArray<View> mHeaderViews = new SparseArray<>();

    private List<T> mItems = new ArrayList<>();

    @LayoutRes
    private final int mLayoutRes;

    private final OnItemClickListener mOnItemClickListener;

    private final OnItemLongClickListener mOnItemLongClickListener;

    private RecyclerView mRecyclerView;

    private final AbsSelectMode mSelectMode;

    AbsArrayAdapter(Builder<T, ? extends Builder> builder) {
        this.mActivity = builder.activity;
        this.mOnItemClickListener = builder.itemClickListener;
        this.mLayoutRes = builder.layoutRes;
        this.mBinders = builder.binders;
        this.mSelectMode = builder.selectMode;
        this.mCheckBoxIdRes = builder.checkBoxId;
        this.mOnItemLongClickListener = builder.itemLongClickListener;
        setHasStableIds(true);
    }

    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.mRecyclerView = null;
    }

    @NonNull
    public final VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = this.mHeaderViews.get(viewType);
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(this.mLayoutRes, viewGroup, false);
        }
        return onCreateViewHolder(viewGroup, viewType, view);
    }

    public final void onBindViewHolder(@NonNull VH vh, int position) {
        if (DEBUG) {
            String simpleName = getClass().getSimpleName();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onBindViewHolder. pos - ");
            stringBuilder.append(position);
            Log.d(simpleName, stringBuilder.toString());
        }

        if (isHeader(position)) {
            onBindHeaderViewHolder(vh, mHeaderTypes.get(position));
            return;
        }
        T item = getItem(position);
        onBindViewHolder(vh, position, item);
        onBindCheckBoxViewHolder(vh, position, item);
        if (this.mBinders.size() > 0) {
            for (int index = 0; index < this.mBinders.size(); index++) {
                int keyAt = this.mBinders.keyAt(index);
                ViewBinder viewBinder = this.mBinders.get(keyAt);
                if (viewBinder != null) {
                    viewBinder.bind(vh.itemView.findViewById(keyAt), item);
                }
            }
        }
    }


    protected void onBindHeaderViewHolder(@NonNull VH vh, int position) {
    }

    protected void onBindViewHolder(@NonNull VH vh, int position, T item) {
    }

    protected void onBindCheckBoxViewHolder(@NonNull VH vh, int position, T item) {
        if (vh.checkBox == null && this.mCheckBoxIdRes != 0) {
            vh.checkBox = vh.itemView.findViewById(this.mCheckBoxIdRes);
        }
        if (vh.checkBox == null) {
            return;
        }
        if (isSelectMode()) {
            vh.checkBox.setVisibility(View.VISIBLE);
            vh.checkBox.setClickable(false);
            if (vh.checkBox instanceof Checkable) {
                ((Checkable) vh.checkBox).setChecked(this.mSelectMode.isSelected(getItemId(position)));
                return;
            }
            return;
        }
        vh.checkBox.setVisibility(View.GONE);
    }


    public void addHeaderView(int position, int type, @NonNull View view) {
        this.mHeaderTypes.put(position, type);
        this.mHeaderViews.put(type, view);
    }

    public long getItemId(int position) {
        if (isHeader(position)) {
            return mHeaderTypes.get(position);
        }
        return (long) getItem(position).hashCode();
    }

    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return mHeaderTypes.get(position);
        }
        return super.getItemViewType(position);
    }

    private int convertToRealPos(int i) {
        for (int i2 = 0; i2 < this.mHeaderTypes.size(); i2++) {
            if (this.mHeaderTypes.keyAt(i2) <= i) {
                i--;
            }
        }
        return i;
    }

    public boolean isHeader(int i) {
        return this.mHeaderTypes.get(i) != null;
    }

    private boolean dispatchLongClickEvent(View view, final int i) {
        if (this.mRecyclerView == null) {
            return false;
        }
        this.mRecyclerView.startActionMode(new Callback() {
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                AbsArrayAdapter.this.mActionMode = actionMode;
                boolean onCreateActionMode = AbsArrayAdapter.this.mSelectMode.getActionModeCallback()
                        .onCreateActionMode(actionMode, menu);
                AbsArrayAdapter.this.mSelectMode.toggleSelect(AbsArrayAdapter.this.getItemId(i));
                AbsArrayAdapter.this.notifyDataSetChanged();
                return onCreateActionMode;
            }

            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return AbsArrayAdapter.this.mSelectMode.getActionModeCallback().onPrepareActionMode(actionMode, menu);
            }

            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                boolean onActionItemClicked = AbsArrayAdapter.this.mSelectMode.getActionModeCallback()
                        .onActionItemClicked(actionMode, menuItem);
                actionMode.finish();
                return onActionItemClicked;
            }

            public void onDestroyActionMode(ActionMode actionMode) {
                AbsArrayAdapter.this.mSelectMode.getActionModeCallback().onDestroyActionMode(actionMode);
                AbsArrayAdapter.this.mSelectMode.clearItems();
                AbsArrayAdapter.this.mActionMode = null;
                AbsArrayAdapter.this.notifyDataSetChanged();
            }
        });
        return true;
    }

    public boolean isSelectMode() {
        return this.mActionMode != null;
    }

    public void swapArray(List<T> list) {
        this.mItems.clear();
        if (list != null) {
            this.mItems.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addItem(@NonNull T t) {
        this.mItems.add(t);
        notifyItemInserted(mItems.size() - 1);
    }

    public void removeItem(@NonNull T t) {
        int indexOf = this.mItems.indexOf(t);
        if (indexOf >= 0) {
            this.mItems.remove(t);
            notifyItemRemoved(indexOf);
        }
    }

    public void removeItemAtPosition(@IntRange(from = 0) int position) {
        if (position < mItems.size()) {
            this.mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeItemById(long[] itemIds) {
        if (itemIds == null) {
            return;
        }
        List<Long> itemIdList = CollectionsUtils.toLongList(itemIds);
        List<T> removeCandidate = new ArrayList<>();
        for (int index = 0; index < getItemCount(); index++) {
            if (itemIdList.contains(getItemId(index))) {
                removeCandidate.add(getItem(index));
            }
        }
        if (!removeCandidate.isEmpty()) {
            removeAll(removeCandidate);
        }
    }

    public void removeAll(Collection<T> items) {
        mItems.removeAll(items);
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return new ArrayList<>(this.mItems);
    }

    public T getItem(int i) {
        return this.mItems.get(convertToRealPos(i));
    }

    public boolean contains(T t) {
        return this.mItems.contains(t);
    }

    public int getItemCount() {
        int size = this.mItems.size();
        int i = 0;
        int i2 = 0;
        while (i < this.mHeaderTypes.size()) {
            if (this.mHeaderTypes.keyAt(i) <= size) {
                i2++;
                size++;
            }
            i++;
        }
        return this.mItems.size() + i2;
    }


    @Override
    public void clearItems() {
        if (mSelectMode == null) {
            return;
        }
        mSelectMode.clearItems();
    }

    @Override
    public long[] getSelectedItemIds() {
        if (mSelectMode == null) {
            return new long[0];
        }
        return mSelectMode.getSelectedItemIds();
    }

    @Override
    public boolean isSelected(long itemId) {
        if (mSelectMode == null) {
            return false;
        }
        return mSelectMode.isSelected(itemId);
    }

    @Override
    public void toggleSelect(long itemId) {
        if (mSelectMode == null) {
            return;
        }
        mSelectMode.toggleSelect(itemId);
    }


    public static abstract class Builder<T, B extends Builder<T, B>> {

        private final Activity activity;

        private SparseArray<ViewBinder<T>> binders = new SparseArray<>();

        @IdRes
        private int checkBoxId;

        @LayoutRes
        private int layoutRes;

        private AbsSelectMode selectMode;

        private OnItemClickListener itemClickListener;

        private OnItemLongClickListener itemLongClickListener;

        public abstract AbsArrayAdapter build();

        protected abstract B self();

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public B addViewBinder(@IdRes int i, ViewBinder<T> viewBinder) {
            this.binders.put(i, viewBinder);
            return self();
        }

        public B withItemClickListener(OnItemClickListener listener) {
            this.itemClickListener = listener;
            return self();
        }

        public B withItemLongClickListener(OnItemLongClickListener listener) {
            this.itemLongClickListener = listener;
            return self();
        }

        public B setLayoutResId(@LayoutRes int i) {
            this.layoutRes = i;
            return self();
        }

        public B withSelectMode(@IdRes int i, AbsSelectMode absSelectMode) {
            this.selectMode = absSelectMode;
            this.checkBoxId = i;
            return self();
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {

        boolean onItemLongClick(View view, int position);
    }

    public interface ViewBinder<T> {

        void bind(View view, T item);
    }

    public static abstract class AbsViewBinder<T, V extends View> implements ViewBinder<T> {

        protected abstract void bindView(V v, T item);

        public final void bind(View view, T item) {
            bindView((V) view, item);
        }
    }

    public static class ViewHolderInternal extends ViewHolder {

        View checkBox;

        public ViewHolderInternal(View view, AbsArrayAdapter absArrayAdapter) {
            super(view);
            this.checkBox = view.findViewById(R.id.checkbox);
            initClickListener(absArrayAdapter);
        }

        protected void initClickListener(final AbsArrayAdapter absArrayAdapter) {
            if (absArrayAdapter.mOnItemClickListener != null) {
                this.itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (absArrayAdapter.isSelectMode()) {
                            absArrayAdapter.mSelectMode.toggleSelect(
                                    absArrayAdapter.getItemId(ViewHolderInternal.this.getAdapterPosition()));
                            absArrayAdapter.notifyItemChanged(ViewHolderInternal.this.getAdapterPosition());
                            return;
                        }
                        absArrayAdapter.mOnItemClickListener
                                .onItemClick(view, ViewHolderInternal.this.getAdapterPosition());
                    }
                });
            }
            if (absArrayAdapter.mSelectMode != null) {
                this.itemView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        return absArrayAdapter
                                .dispatchLongClickEvent(view, ViewHolderInternal.this.getAdapterPosition());
                    }
                });
            } else {
                if (absArrayAdapter.mOnItemLongClickListener != null) {
                    this.itemView.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return absArrayAdapter.mOnItemLongClickListener
                                    .onItemLongClick(v, ViewHolderInternal.this.getAdapterPosition());
                        }
                    });
                }
            }
        }
    }
}