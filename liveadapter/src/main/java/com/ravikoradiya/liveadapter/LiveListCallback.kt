package com.ravikoradiya.liveadapter

import android.os.Looper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.*

class LiveListCallback(adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>) :
    Observer<List<Any>> {

    private val reference = WeakReference<RecyclerView.Adapter<Holder<ViewDataBinding>>>(adapter)
    private val adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return reference.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }
    var oldData = ArrayList<Any>()
    var firstTime = true

    override fun onChanged(t: List<Any>?) {

        if (firstTime) {
            firstTime = false
            val diffCallback = LiveDiffUtils(oldData, t.orEmpty())
            val diffResult = DiffUtil.calculateDiff(diffCallback, true)
            adapter?.let { diffResult.dispatchUpdatesTo(it) }
        } else {
            adapter?.notifyDataSetChanged()
        }

        oldData.clear()
        t?.forEach { oldData.add(it) }

    }

}
