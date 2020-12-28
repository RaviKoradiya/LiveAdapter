package com.ravikoradiya.liveadapter

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView

class LiveListCallback(
    adapter: RecyclerView.Adapter<Holder<ViewDataBinding>>,
    diffCallback: DiffCallback,
    private var noDataCallback: ((isDataEmpty: Boolean) -> Unit)?
) : Observer<List<Any>> {

    private var mDiffer: AsyncListDiffer<Any>? = null

    init {
        mDiffer = AsyncListDiffer<Any>(adapter, object : ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if (oldItem.javaClass != newItem.javaClass) {
                    false
                } else {
                    diffCallback.areDataSame(oldItem, newItem)
                }
            }
        })
    }

    fun setNoDataCallback(noDataCallback: ((isDataEmpty: Boolean) -> Unit)?) {
        this.noDataCallback = noDataCallback
    }

    fun getItemCount(): Int {
        return mDiffer?.currentList.orEmpty().size
    }

    fun getItem(position: Int): Any? {
        return mDiffer?.currentList?.get(position)
    }

    override fun onChanged(t: List<Any>) {
        mDiffer?.submitList(t.map { it })
        noDataCallback?.invoke(t.isEmpty())
    }
}
