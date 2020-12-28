package com.ravikoradiya.liveadapter

import android.os.Looper
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class ObservableListCallback<H : RecyclerView.ViewHolder>(
    adapter: RecyclerView.Adapter<H>,
    private var noDataCallback: ((isDataEmpty: Boolean) -> Unit)?
) : ObservableList.OnListChangedCallback<ObservableList<Any>>() {

    private val reference = WeakReference(adapter)
    private val adapter: RecyclerView.Adapter<H>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return reference.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }

    fun setNoDataCallback(noDataCallback: ((isDataEmpty: Boolean) -> Unit)?) {
        this.noDataCallback = noDataCallback
    }

    override fun onChanged(list: ObservableList<Any>) {
        adapter?.notifyDataSetChanged()
        noDataCallback?.invoke(list.isEmpty())
    }

    override fun onItemRangeChanged(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeChanged(from, count)
        noDataCallback?.invoke(list.isEmpty())
    }

    override fun onItemRangeInserted(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeInserted(from, count)
        noDataCallback?.invoke(list.isEmpty())
    }

    override fun onItemRangeRemoved(list: ObservableList<Any>, from: Int, count: Int) {
        adapter?.notifyItemRangeRemoved(from, count)
        noDataCallback?.invoke(list.isEmpty())
    }

    override fun onItemRangeMoved(list: ObservableList<Any>, from: Int, to: Int, count: Int) {
        adapter?.let { for (i in 0 until count) it.notifyItemMoved(from + i, to + i) }
        noDataCallback?.invoke(list.isEmpty())
    }
}
