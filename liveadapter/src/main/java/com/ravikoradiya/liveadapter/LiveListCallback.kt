package com.ravikoradiya.liveadapter

import androidx.lifecycle.Observer
import androidx.databinding.ViewDataBinding
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class LiveListCallback<E : List<*>>(
    adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>
) : Observer<E?> {

    lateinit var oldData: List<*>

    override fun onChanged(t: E?) {

        if (this::oldData.isInitialized) {
            val diffCallback = LiveDiffUtils(oldData, t.orEmpty())
            val diffResult = DiffUtil.calculateDiff(diffCallback, true)
            adapter?.let { diffResult.dispatchUpdatesTo(it) }
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
