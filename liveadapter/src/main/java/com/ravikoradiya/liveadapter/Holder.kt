package com.ravikoradiya.liveadapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class Holder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {
    internal var created = false
}