[![JitPack](https://jitpack.io/v/RaviKoradiya/LiveAdapter.svg)](https://jitpack.io/#RaviKoradiya/LiveAdapter) 
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LiveAdapter-blue.svg?style=flat)](https://android-arsenal.com/details/1/8210)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](https://opensource.org/licenses/Apache-2.0)



# LiveAdapter

**Don't write a RecyclerView adapter again. Not even a ViewHolder!**

* Based on [**Android Data Binding**](https://developer.android.com/topic/libraries/data-binding/index.html)
* Written in [**Kotlin**](http://kotlinlang.org)
* Supports [**LiveData**](https://developer.android.com/topic/libraries/architecture/livedata)
* No need to write the adapter
* No need to write the ViewHolders
* No need to modify your model classes
* No need to notify the adapter when data set changes
* Supports multiple item view types
* Optional Callbacks/Listeners
* Very fast — no reflection
* Super easy API
* Minimum Android SDK: **19**


## Setup

### Gradle

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency to module build.gradle

```gradle
// apply plugin: 'kotlin-kapt' // this line only for Kotlin projects

android {
    ...
    dataBinding.enabled true 
}

dependencies {
    implementation 'com.github.RaviKoradiya:LiveAdapter:1.3.2-1608532016'
    // kapt 'com.android.databinding:compiler:GRADLE_PLUGIN_VERSION' // this line only for Kotlin projects
}
```


## Usage

Create your item layouts with `<layout>` as root:

```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android">

    <data>
        <variable name="item" type="com.github.RaviKoradiya:LiveAdapter.item.Header"/>
    </data>
    
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@{item.text}"/>
        
</layout>
```

**It is important for all the item types to have the same variable name**, in this case "item". 
This name is passed to the adapter builder as BR.variableName, in this case BR.item:

##### ObservableList or simple List

```java
// Java
new LiveAdapter(listOfItems, BR.item)
           .map(Header.class, R.layout.item_header)
           .map(Point.class, R.layout.item_point)
           .into(recyclerView);
```
```kotlin     
// Kotlin
LiveAdapter(listOfItems, BR.item)
           .map<Header>(R.layout.item_header)
           .map<Point>(R.layout.item_point)
           .into(recyclerView)
```

The list of items can be an `ObservableList` or `LiveData<List>` if you want to get the adapter **automatically updated** when its content changes, or a simple `List` if you don't need to use this feature.

##### LiveData<List<*>>

```kotlin
LiveAdapter(
            data = liveListOfItems,
            lifecycleOwner = this@MainActivity,
            variable = BR.item )
            .map<Header, ItemHeaderBinding>(R.layout.item_header) {
                areContentsTheSame { old: Header, new: Header ->
                    return@areContentsTheSame old.text == new.text
                }
            }
            .map<Point, ItemPointBinding>(R.layout.item_point) {
                areContentsTheSame { old: Point, new: Point ->
                    return@areContentsTheSame old.id == new.id
                }
            }
            .into(recyclerview)
```

I suggest to implement `DiffUtil ItemCallback` while using `LiveData`.


### LayoutHandler

The LayoutHandler interface allows you to use different layouts based on more complex criteria. Its one single method receives the item and the position and returns the layout resource id.

```java
// Java sample
new LiveAdapter(listOfItems, BR.item)
           .handler(handler)
           .into(recyclerView);

private LayoutHandler handler = new LayoutHandler() {
    @Override public int getItemLayout(@NotNull Object item, int position) {
        if (item instanceof Header) {
            return (position == 0) ? R.layout.item_header_first : R.layout.item_header;
        } else {
            return R.layout.item_point;
        }
    }
};
```
```kotlin
// Kotlin sample
LiveAdapter(listOfItems, BR.item).layout { item, position ->
    when (item) {
        is Header -> if (position == 0) R.layout.item_header_first else R.layout.item_header
        else -> R.layout.item_point 
    }
}.into(recyclerView)
```


## Acknowledgments

Thanks to **Miguel Ángel Moreno** for [this library](https://github.com/nitrico/LastAdapter).


## Author

#### Ravi Koradiya

I'm open to new job positions - Contact me!

|[Email](mailto:koradiyaravi@gmail.com)|[Facebook](https://www.facebook.com/ravikoradiya)|[Linked.in](https://www.linkedin.com/in/ravikoradiya)|[Twitter](https://twitter.com/Ravi_Koradiya/)
|---|---|---|---|


## License

```txt
Copyright 2020 Ravi Koradiya

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
