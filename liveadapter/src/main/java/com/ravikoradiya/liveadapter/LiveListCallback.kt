package com.ravikoradiya.liveadapter

import android.os.Looper
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravikoradiya.extendedlivedata.ExtendedObserver
import java.lang.ref.WeakReference

class LiveListCallback(
    adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>,
    diffUtilCallback: DiffUtil.ItemCallback<Any>?
) : ExtendedObserver<List<Any>> {

    private var mDiffer: AsyncListDiffer<Any>? = null
    private val referenceAdapter =
        WeakReference<RecyclerView.Adapter<Holder<ViewDataBinding>>>(adapter)
    private val adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return referenceAdapter.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }

    private val referenceCallback = WeakReference<DiffUtil.ItemCallback<Any>>(diffUtilCallback)
    private val diffUtilCallback: DiffUtil.ItemCallback<Any>?
        get() {
            if (Thread.currentThread() == Looper.getMainLooper().thread) return referenceCallback.get()
            else throw IllegalStateException("You must modify the ObservableList on the main thread")
        }

    init {
        diffUtilCallback?.let {
            mDiffer = AsyncListDiffer<Any>(adapter, it)
        }
    }

    fun getItemCount(): Int {
        return mDiffer?.currentList.orEmpty().size
    }

    override fun onChanged(o: List<Any>?, n: List<Any>?) {
        mDiffer?.submitList(n)
    }

}
