package com.ravikoradiya.liveadapter

import androidx.databinding.ViewDataBinding

open class BaseType
@JvmOverloads constructor(open val layout: Int, open val variable: Int? = null)

@Suppress("unused")
abstract class AbsType<T, B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : BaseType(layout, variable)

open class ItemType<T, B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : AbsType<T, B>(layout, variable) {
    open fun onCreate(holder: Holder<B>) {}
    open fun onBind(holder: Holder<B>) {}
    open fun onRecycle(holder: Holder<B>) {}
    open fun areItemSame(old: T, new: T): Boolean {
        return old == new
    }
    open fun areContentsTheSame(old: T, new: T): Boolean {
        return old == new
    }
}

open class Type<T, B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : AbsType<T, B>(layout, variable) {
    internal var onCreate: Action<B>? = null; private set
    internal var onBind: Action<B>? = null; private set
    internal var onClick: Action<B>? = null; private set
    internal var onLongClick: Action<B>? = null; private set
    internal var onRecycle: Action<B>? = null; private set
    internal var areContentsTheSame: ActionCompare<T>? = null; private set
    internal var areItemSame: ActionCompare<T>? = null; private set
    fun onCreate(action: Action<B>?) = apply { onCreate = action }
    fun onBind(action: Action<B>?) = apply { onBind = action }
    fun onClick(action: Action<B>?) = apply { onClick = action }
    fun onLongClick(action: Action<B>?) = apply { onLongClick = action }
    fun onRecycle(action: Action<B>?) = apply { onRecycle = action }
    fun areItemSame(action: ActionCompare<T>?) =
        apply { areItemSame = action }
    fun areContentsTheSame(action: ActionCompare<T>?) =
        apply { areContentsTheSame = action }
}

typealias Action<B> = (Holder<B>) -> Unit
typealias ActionCompare<T> = (old: T, new: T) -> Boolean
