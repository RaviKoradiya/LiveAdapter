package com.ravikoradiya.liveadapter

import androidx.databinding.ObservableList
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class ObservableListCallback<H : RecyclerView.ViewHolder>(adapter: RecyclerView.Adapter<H>) :
    ObservableList.OnListChangedCallback<ObservableList<Any>>() {

    private val reference = WeakReference<RecyclerView.Adapter<H>>(adapter)
    private val adapter: RecyclerView.Adapter<H>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return reference.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }

    override fun onChanged(list: ObservableList<Any>) {
        adapter?.notifyDataSetChanged()
    }

    override fun onItemRangeChanged(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeChanged(from, count)
    }

    override fun onItemRangeInserted(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeInserted(from, count)
    }

    override fun onItemRangeRemoved(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeRemoved(from, count)
    }

    override fun onItemRangeMoved(list: ObservableList<Any>, from: Int, to: Int, count: Int) {
        adapter?.let { for (i in 0..count - 1) it.notifyItemMoved(from + i, to + i) }
    }

}
