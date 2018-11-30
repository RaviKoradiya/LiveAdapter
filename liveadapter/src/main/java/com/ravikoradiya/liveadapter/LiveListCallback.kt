package com.ravikoradiya.liveadapter

import android.arch.lifecycle.Observer
import android.databinding.ViewDataBinding
import android.os.Looper
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import java.lang.ref.WeakReference

class LiveListCallback<E : List<*>>(
    adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>
) : Observer<E?> {

    lateinit var oldData: List<*>

    override fun onChanged(t: E?) {

        if (this::oldData.isInitialized) {
            val diffCallback = LiveDiffUtils(oldData, t.orEmpty())
            val diffResult = DiffUtil.calculateDiff(diffCallback, true)
            diffResult.dispatchUpdatesTo(adapter)
        } else {
            adapter?.notifyDataSetChanged()
        }

        t?.let { oldData = (it as ArrayList<*>).clone() as List<*> }

    }

    private val reference = WeakReference<RecyclerView.Adapter<Holder<ViewDataBinding>>>(adapter)
    private val adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return reference.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }
}
