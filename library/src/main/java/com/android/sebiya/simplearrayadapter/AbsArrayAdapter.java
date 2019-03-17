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
import android.util.SparseIntArray;
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
        implements SelectMode, SpanSize {

    private static final String TAG = "AbsArrayAdapter";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    protected abstract VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType, View view);

    private ActionMode mActionMode;

    protected final Activity mActivity;

    private final SparseArray<ViewBinder<T>> mBinders;
    private SparseArray<HeaderViewListener> mHeaderBinders = new SparseArray<>();

    @IdRes
    private final int mCheckBoxIdRes;

    private SparseIntArray mHeaderTypes = new SparseIntArray();

    private SparseArray<View> mHeaderViews = new SparseArray<>();

    private SparseIntArray mHeaderViewRes = new SparseIntArray();

    private List<T> mItems = new ArrayList<>();

    @LayoutRes
    private final int mLayoutRes;

    private final OnItemClickListener mOnItemClickListener;

    private final OnItemLongClickListener mOnItemLongClickListener;

    private RecyclerView mRecyclerView;

    private final AbsSelectMode mSelectMode;

    private final SpanSize mSpanSize;

    public AbsArrayAdapter(Builder<T, ? extends Builder> builder) {
        mActivity = builder.activity;
        mOnItemClickListener = builder.itemClickListener;
        mLayoutRes = builder.layoutRes;
        mBinders = builder.binders;
        mSelectMode = builder.selectMode;
        mCheckBoxIdRes = builder.checkBoxId;
        mOnItemLongClickListener = builder.itemLongClickListener;
        mSpanSize = builder.spanSize;
        setHasStableIds(true);
    }

    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @NonNull
    public final VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = mHeaderViews.get(viewType);
        if (DEBUG) {
            Log.d(TAG, "onCreateViewHolder. type - " + viewType);
        }

        if (view == null && mHeaderViewRes.indexOfKey(viewType) >= 0) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(mHeaderViewRes.get(viewType), viewGroup, false);
            if (mHeaderBinders.size() > 0) {
                HeaderViewListener headerBinder = mHeaderBinders.get(viewType);
                if (headerBinder != null) {
                    headerBinder.onCreateHeaderView(view, viewType);
                }
            }
        }
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(mLayoutRes, viewGroup, false);
        }
        return onCreateViewHolder(viewGroup, viewType, view);
    }

    public final void onBindViewHolder(@NonNull VH vh, int position) {
        if (DEBUG) {
            final String message = "onBindViewHolder. pos - " +
                    position +
                    ", type - " + getItemViewType(position);
            Log.d(TAG, message);
        }

        if (isHeader(position)) {
            onBindHeaderViewHolder(vh, mHeaderTypes.get(position));
            if (mHeaderBinders.size() > 0) {
                int type = getItemViewType(position);
                HeaderViewListener headerBinder = mHeaderBinders.get(type);
                if (DEBUG) {
                    Log.d(TAG, "onBindViewHolder. header type - " + type + ", binder - " + headerBinder);
                }
                if (headerBinder != null) {
                    headerBinder.onBindHeaderView(vh.itemView, type);
                }
            }
            return;
        }
        T item = getItemByAdapterPosition(position);
        onBindViewHolder(vh, position, item);
        onBindCheckBoxViewHolder(vh, position, item);
        if (mBinders.size() > 0) {
            for (int index = 0; index < mBinders.size(); index++) {
                int keyAt = mBinders.keyAt(index);
                ViewBinder viewBinder = mBinders.get(keyAt);
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
        View checkBox = null;
        if (mCheckBoxIdRes != 0) {
            checkBox = vh.itemView.findViewById(mCheckBoxIdRes);
        }
        if (checkBox == null) {
            return;
        }
        if (isSelectMode()) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setClickable(false);
            if (checkBox instanceof Checkable) {
                ((Checkable) checkBox).setChecked(mSelectMode.isSelected(getItemId(position)));
                return;
            }
            return;
        }
        checkBox.setVisibility(View.GONE);
        if (checkBox instanceof Checkable) {
            ((Checkable) checkBox).setChecked(false);
        }
    }

    public void addHeaderView(int position, int type, @LayoutRes int viewRes, HeaderViewListener binder) {
        mHeaderTypes.put(position, type);
        mHeaderViewRes.put(type, viewRes);
        mHeaderBinders.put(type, binder);
    }

    public void addHeaderView(int position, int type, @NonNull View view) {
        mHeaderTypes.put(position, type);
        mHeaderViews.put(type, view);
    }

    public void removeHeaderViewByType(int type) {
        mHeaderViews.delete(type);
        mHeaderBinders.delete(type);
        mHeaderViewRes.delete(type);
        int indexOfValue = mHeaderTypes.indexOfValue(type);
        if (indexOfValue >= 0) {
            mHeaderTypes.removeAt(indexOfValue);
        }
        notifyDataSetChanged();
    }

    public long getItemId(int position) {
        if (isHeader(position)) {
            return mHeaderTypes.get(position);
        }
        return (long) getItemByAdapterPosition(position).hashCode();
    }

    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return mHeaderTypes.get(position);
        }
        return super.getItemViewType(position);
    }

    private int convertToRealPos(int i) {
        int newPos = i;
        for (int i2 = 0; i2 < mHeaderTypes.size(); i2++) {
            if (mHeaderTypes.keyAt(i2) <= i) {
                newPos--;
            }
        }
        return newPos;
    }

    public boolean isHeader(int i) {
        return mHeaderTypes.indexOfKey(i) >= 0;
    }

    @Override
    public int getSpanSize(final Adapter adapter, final int position, final int layoutSpanSize) {
        return mSpanSize != null ? mSpanSize.getSpanSize(this, position, layoutSpanSize) : 1;
    }

    private boolean dispatchLongClickEvent(View view, final int i) {
        if (mRecyclerView == null) {
            return false;
        }
        mRecyclerView.startActionMode(new Callback() {
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                mActionMode = actionMode;
                boolean onCreateActionMode = mSelectMode.getActionModeCallback()
                        .onCreateActionMode(actionMode, menu);
                mSelectMode.toggleSelect(getItemId(i));
                notifyDataSetChanged();
                return onCreateActionMode;
            }

            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return mSelectMode.getActionModeCallback().onPrepareActionMode(actionMode, menu);
            }

            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                boolean onActionItemClicked = mSelectMode.getActionModeCallback()
                        .onActionItemClicked(actionMode, menuItem);
                actionMode.finish();
                return onActionItemClicked;
            }

            public void onDestroyActionMode(ActionMode actionMode) {
                mSelectMode.getActionModeCallback().onDestroyActionMode(actionMode);
                mSelectMode.clearItems();
                mActionMode = null;
                notifyDataSetChanged();
            }
        });
        return true;
    }

    public boolean isSelectMode() {
        return mActionMode != null;
    }

    public void swapArray(List<T> list) {
        mItems.clear();
        if (list != null) {
            mItems.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> collection) {
        if (collection != null) {
            mItems.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public void addItem(@NonNull T item) {
        mItems.add(item);
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeItem(T item) {
        if (item == null) {
            return;
        }
        if (mItems.remove(item)) {
            notifyDataSetChanged();
        }
    }

    public void removeItemAtPosition(@IntRange(from = 0) int position) {
        if (position < mItems.size()) {
            mItems.remove(position);
            notifyDataSetChanged();
            // TODO : find real position and notify specific view only for performance
//            notifyItemRemoved(position);
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
                removeCandidate.add(getItemByAdapterPosition(index));
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
        return new ArrayList<>(mItems);
    }

    public T getItemByAdapterPosition(int adapterPosition) {
        return mItems.get(convertToRealPos(adapterPosition));
    }

    public T getItem(int i) {
        if (i < 0 || i >= mItems.size() || mItems.isEmpty()) {
            return null;
        }
        return mItems.get(i);
    }

    public boolean contains(T item) {
        return mItems.contains(item);
    }

    public int getItemCount() {
        int size = mItems.size();
        int i = 0;
        int i2 = 0;
        while (i < mHeaderTypes.size()) {
            if (mHeaderTypes.keyAt(i) <= size) {
                i2++;
                size++;
            }
            i++;
        }
        return mItems.size() + i2;
    }

    /**
     * return item count except header type view.
     */
    public int getRealItemCount() {
        return mItems.size();
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

        private SpanSize spanSize;

        public abstract AbsArrayAdapter build();

        protected abstract B self();

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public B addViewBinder(@IdRes int viewId, ViewBinder<T> viewBinder) {
            binders.put(viewId, viewBinder);
            return self();
        }

        public B withItemClickListener(OnItemClickListener listener) {
            itemClickListener = listener;
            return self();
        }

        public B withItemLongClickListener(OnItemLongClickListener listener) {
            itemLongClickListener = listener;
            return self();
        }

        public B setLayoutResId(@LayoutRes int layoutRes) {
            this.layoutRes = layoutRes;
            return self();
        }

        public B withSelectMode(@IdRes int checkBoxId, AbsSelectMode absSelectMode) {
            selectMode = absSelectMode;
            this.checkBoxId = checkBoxId;
            return self();
        }

        public B withSpanSize(SpanSize spanSize) {
            this.spanSize = spanSize;
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

    public interface HeaderViewListener {
        void onCreateHeaderView(View view, int type);
        void onBindHeaderView(View view, int type);
    }

    public static abstract class AbsViewBinder<T, V extends View> implements ViewBinder<T> {

        protected abstract void bindView(V v, T item);

        public final void bind(View view, T item) {
            if (view != null) {
                bindView((V) view, item);
            }
        }
    }

    public static class ViewHolderInternal extends ViewHolder {
        public ViewHolderInternal(View view, AbsArrayAdapter absArrayAdapter) {
            super(view);
            initClickListener(absArrayAdapter);
        }

        protected void initClickListener(final AbsArrayAdapter absArrayAdapter) {
            if (absArrayAdapter.mOnItemClickListener != null) {
                itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (absArrayAdapter.isHeader(getAdapterPosition())) {
                            return;
                        }
                        if (absArrayAdapter.isSelectMode()) {
                            absArrayAdapter.mSelectMode.toggleSelect(
                                    absArrayAdapter.getItemId(getAdapterPosition()));
                            absArrayAdapter.notifyItemChanged(getAdapterPosition());
                            return;
                        }
                        absArrayAdapter.mOnItemClickListener
                                .onItemClick(view, getAdapterPosition());
                    }
                });
            }
            if (absArrayAdapter.mSelectMode != null) {
                itemView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        if (absArrayAdapter.isHeader(getAdapterPosition())) {
                            return false;
                        }
                        if (absArrayAdapter.isSelectMode()) {
                            return false;
                        }
                        return absArrayAdapter
                                .dispatchLongClickEvent(view, getAdapterPosition());
                    }
                });
            } else {
                if (absArrayAdapter.mOnItemLongClickListener != null) {
                    itemView.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (absArrayAdapter.isHeader(getAdapterPosition())) {
                                return false;
                            }
                            return absArrayAdapter.mOnItemLongClickListener
                                    .onItemLongClick(v, getAdapterPosition());
                        }
                    });
                }
            }
        }
    }
}