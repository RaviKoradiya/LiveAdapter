package com.ravikoradiya.liveadapter

import androidx.recyclerview.widget.DiffUtil

class LiveDiffUtils(private val oldList: List<Any>, private val newList: List<Any>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
