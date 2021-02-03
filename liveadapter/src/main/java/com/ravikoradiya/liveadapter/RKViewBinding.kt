package com.ravikoradiya.liveadapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.viewbinding.ViewBinding

class RKViewBinding<T : Any>(private val root: View) : ViewBinding {

    private val childMap = HashMap<Int, View>()
    var data: T? = null

    fun putData(data: Any?) {
        this.data = data as T
    }

    init {
        mapChildren(root)
    }

    fun <T : View> getViewById(@IdRes resId: Int): T? {
        return childMap[resId] as T?
    }

    private fun mapChildren(view: View) {

        if (view.id != View.NO_ID)
            childMap[view.id] = view


        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                mapChildren(view.getChildAt(index))
            }
        }
    }

    override fun getRoot(): View {
        return root
    }
}