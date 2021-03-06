# SimpleArrayAdapter

Simple RecyclerView Array Adapter for Android.

[![](https://jitpack.io/v/kshdreams/SimpleArrayAdapter.svg)](https://jitpack.io/#kshdreams/SimpleArrayAdapter)

##### GETTING STARTED
SimpleArrayAdapter releases are available via JitPack.
```ruby
// Project level build.gradle
// ...
repositories {
    maven { url 'https://jitpack.io' }
}
// ...

// Module level build.gradle
dependencies {
    // Replace version with release version, e.g. 1.0.0-alpha, -SNAPSHOT
    implementation "com.github.kshdreams:SimpleArrayAdapter:[VERSION]"
}
```



#### How To Use
##### 1. Define your item class
```java
    public static class Item {
        String name;
        String desc;
        public static Item create(String name, String desc) {
            Item item = new Item();
            item.name = name;
            item.desc = desc;
            return item;
        }
    }
```

##### 2. Create adapter using builder
```java
        SimpleArrayAdapter<Item> adapter = SimpleArrayAdapter.<Item>with(this)
                .setLayoutResId(R.layout.list_item_2_line) // set your layout id
                .addViewBinder(R.id.text1, new AbsViewBinder<Item, TextView>() {
                    @Override
                    protected void bindView(final TextView textView, final Item item) {
                        // implements bindView
                        textView.setText(item.name);
                    }
                })
                .withItemClickListener(new AbsArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // Handle click
                    }
                })
                .build(); // create adapter
```

- set layout Id using ```Adapter.setLayoutId()```
- binding your layout id and data using ```Adapter.addViewBinder()```
- can set click listener when build adapter.


##### 3. Add item
```java
        adapter.addItem(Item.create("name", "description"));
        adapter.addItem(Item.create("name2", "description2"));

         List<Item> items = new ArrayList<>();
         items.add(Item.create("name", "description"));
         items.add(Item.create("name2", "description2"));
         adapter.swapArray(items);
```
add item using ```addItem()``` or ```swapArray()```

##### 4. Select mode
```java
        final SimpleArrayAdapter<Item> adapter = SimpleArrayAdapter.<Item>with(this)
                .setLayoutResId(R.layout.list_item_2_line)
                .withSelectMode(R.id.check_box, new MultipleSelectMode(
                        new SelectModeCallback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                // make context menu here
                                menu.add("Delete");
                                return true;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                                long[] selectedItems = adapter.getSelectedItemIds();
                                adapter.removeItemById(selectedItems);
                                return false;
                            }
                        }))
                .build();
```


##### 5. Using different type of view
```java
        // create view on demand
        mAdapter.addHeaderView(0, HEADER_TYPE_1, R.layout.view_header, new HeaderViewListener() {
            @Override
            public void onCreateHeaderView(View view, int type) {
                // do something on create
            }

            @Override
            public void onBindHeaderView(View view, int type) {
                // do something on bind
            }
        });

        // create view immediately
        View header = View.inflate(this, R.layout.view_header_2, null);
        adapter.addHeaderView(0, HEADER_TYPE_2, header);
```



## License
```
Copyright 2018 kshdreams

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```