package com.ravikoradiya.liveadapter

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

open class Holder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {
    internal var created = false
}
