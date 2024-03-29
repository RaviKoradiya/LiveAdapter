package com.ravikoradiya.liveadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView


class LiveAdapter private constructor(
    private val data: LiveData<out List<Any>>?,
    private val lifecycleOwner: LifecycleOwner?,
    private val list: List<Any>?,
    private val variable: Int?,
    private val stableIds: Boolean?
) : RecyclerView.Adapter<Holder<ViewDataBinding>>() {

    constructor(list: List<Any>?) : this(null, null, list, null, false)
    constructor(list: List<Any>?, variable: Int) : this(null, null, list, variable, false)
    constructor(list: List<Any>?, stableIds: Boolean) : this(
        null,
        null,
        list,
        null,
        stableIds
    )

    constructor(list: List<Any>?, variable: Int, stableIds: Boolean) : this(
        null,
        null,
        list,
        variable,
        stableIds
    )

    constructor(
        data: LiveData<out List<Any>>,
        lifecycleOwner: LifecycleOwner
    ) : this(
        data,
        lifecycleOwner,
        null,
        null,
        false
    )

    constructor(
        data: LiveData<out List<Any>>,
        lifecycleOwner: LifecycleOwner,
        variable: Int
    ) : this(
        data,
        lifecycleOwner,
        null,
        variable,
        false
    )

    constructor(
        data: LiveData<out List<Any>>,
        lifecycleOwner: LifecycleOwner,
        stableIds: Boolean
    ) : this(
        data,
        lifecycleOwner,
        null,
        null,
        stableIds
    )

    constructor(
        data: LiveData<out List<Any>>,
        lifecycleOwner: LifecycleOwner,
        variable: Int,
        stableIds: Boolean
    ) : this(
        data,
        lifecycleOwner,
        null,
        variable,
        stableIds
    )

    private val diffCallback = object : DiffCallback {
        override fun areDataSame(old: Any, new: Any): Boolean {
            val typeOld = map[old.javaClass]
            val typeNew = map[new.javaClass]

            return if (typeOld == typeNew)
                when (typeOld) {
                    is Type<*, *> -> typeOld.areContentsTheSame?.invoke(old, new)
                        ?: (old == new)
                    is ItemType<*, *> -> typeOld.areContentsTheSame(old, new) ?: (old == new)
                    else -> false
                }
            else false
        }

        override fun areItemSame(old: Any, new: Any): Boolean {
            val typeOld = map[old.javaClass]
            val typeNew = map[new.javaClass]

            return if (typeOld == typeNew)
                when (typeOld) {
                    is Type<*, *> -> typeOld.areItemSame?.invoke(old, new)
                        ?: (old == new)
                    is ItemType<*, *> -> typeOld.areItemSame(old, new) ?: (old == new)
                    else -> false
                }
            else false
        }
    }

    private val DATA_INVALIDATION = Any()
    private var noDataCallback: ((isDataEmpty: Boolean) -> Unit)? = null
    private val liveListCallback: LiveListCallback =
        LiveListCallback(this, diffCallback, noDataCallback)
    private val observableListCallback = ObservableListCallback(this, noDataCallback)
    private var recyclerView: RecyclerView? = null

    private lateinit var inflater: LayoutInflater
    private val map = mutableMapOf<Class<*>, BaseType>()
    private var layoutHandler: LayoutHandler? = null
    private var typeHandler: TypeHandler? = null

    init {
        setHasStableIds(stableIds ?: false)
    }

    @JvmOverloads
    fun <T : Any> map(clazz: Class<T>, layout: Int, variable: Int? = null) =
        apply { map[clazz] = BaseType(layout, variable) }

    @JvmOverloads
    inline fun <reified T : Any> map(layout: Int, variable: Int? = null) =
        map(T::class.java, layout, variable)

    @JvmOverloads
    fun <T : Any> map(clazz: Class<T>, type: AbsType<*, *>) = apply { map[clazz] = type }

    @JvmOverloads
    inline fun <reified T : Any> map(type: AbsType<*, *>) = map(T::class.java, type)


    inline fun <reified T : Any, B : ViewDataBinding> map(
        layout: Int,
        variable: Int? = null,
        noinline f: (Type<T, B>.() -> Unit)? = null
    ) = map(T::class.java, Type<T, B>(layout, variable).apply { f?.invoke(this) })

    fun onNoData(
        f: ((isDataEmpty: Boolean) -> Unit)? = null
    ) = apply {
        noDataCallback = f
        liveListCallback.setNoDataCallback(noDataCallback)
        observableListCallback.setNoDataCallback(noDataCallback)
    }


    fun handler(handler: Handler) = apply {
        when (handler) {
            is LayoutHandler -> {
                if (variable == null) {
                    throw IllegalStateException("No variable specified in LiveAdapter constructor")
                }
                layoutHandler = handler
            }
            is TypeHandler -> typeHandler = handler
        }
    }

    inline fun layout(crossinline f: (Any, Int) -> Int) = handler(object : LayoutHandler {
        override fun getItemLayout(item: Any, position: Int) = f(item, position)
    })

    inline fun type(crossinline f: (Any, Int) -> AbsType<*, *>?) = handler(object : TypeHandler {
        override fun getItemType(item: Any, position: Int) = f(item, position)
    })

    fun into(recyclerView: RecyclerView) = apply { recyclerView.adapter = this }

    override fun onCreateViewHolder(view: ViewGroup, viewType: Int): Holder<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, view, false)
        val holder = Holder(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding) =
                recyclerView?.isComputingLayout ?: false

            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView?.isComputingLayout != false) {
                    return
                }
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION)
                }
            }
        })
        return holder
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        val type = getType(position)!!

        val value = if (data != null && lifecycleOwner != null) {
            liveListCallback?.getItem(position)
        } else {
            list?.get(position)
        }

        holder.binding.setVariable(
            getVariable(type),
            value
        )
        holder.binding.executePendingBindings()
        @Suppress("UNCHECKED_CAST")
        if (type is AbsType<*, *>) {
            if (!holder.created) {
                notifyCreate(holder, type as AbsType<*, ViewDataBinding>)
            }
            notifyBind(holder, type as AbsType<*, ViewDataBinding>)
        }
    }

    override fun onBindViewHolder(
        holder: Holder<ViewDataBinding>,
        position: Int,
        payloads: List<Any>
    ) {
        if (isForDataBinding(payloads)) {
            holder.binding.executePendingBindings()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: Holder<ViewDataBinding>) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION && position < itemCount
        ) {
            val type = getType(position)!!
            if (type is AbsType<*, *>) {
                @Suppress("UNCHECKED_CAST")
                notifyRecycle(holder, type as AbsType<*, ViewDataBinding>)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if (hasStableIds()) {

            val item = if (data != null && lifecycleOwner != null) {
                liveListCallback?.getItem(position)
            } else {
                list?.get(position)
            } ?: Any()

            if (item is StableId) {
                return item.stableId
            } else {
                throw IllegalStateException("${item.javaClass.simpleName} must implement StableId interface.")
            }
        } else {
            return super.getItemId(position)
        }
    }

    override fun getItemCount(): Int {
        return if (data != null && lifecycleOwner != null) {
            liveListCallback?.getItemCount() ?: 0
        } else {
            list?.size ?: 0
        }
    }

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        if (recyclerView == null) {
            if (data != null && lifecycleOwner != null) {
                liveListCallback?.let {
                    data.observe(lifecycleOwner, it)
                }
            } else if (list is ObservableList?)
                list?.addOnListChangedCallback(observableListCallback)
        }
        recyclerView = rv
        inflater = LayoutInflater.from(rv.context)
    }

    override fun onDetachedFromRecyclerView(rv: RecyclerView) {
        if (recyclerView != null) {
            if (data != null && lifecycleOwner != null) {
                liveListCallback?.let {
                    data.removeObserver(it)
                }
            } else if (list is ObservableList?) {
                list?.removeOnListChangedCallback(observableListCallback)
            }
        }
        recyclerView = null
    }

    override fun getItemViewType(position: Int): Int {

        val value = if (data != null && lifecycleOwner != null) {
            liveListCallback?.getItem(position)
        } else {
            list?.get(position)
        } ?: Any()

        return layoutHandler?.getItemLayout(
            value,
            position
        )
            ?: typeHandler?.getItemType(
                value,
                position
            )?.layout
            ?: getType(position)?.layout
            ?: throw RuntimeException(
                "Invalid object at position $position: ${value.javaClass}"
            )
    }

    private fun getType(position: Int): BaseType? {
        val value = if (data != null && lifecycleOwner != null) {
            liveListCallback?.getItem(position)
        } else {
            list?.get(position)
        } ?: Any()
        return typeHandler?.getItemType(value, position) ?: map[value.javaClass]
    }

    private fun getVariable(type: BaseType) = type.variable
        ?: variable
        ?: throw IllegalStateException("No variable specified for type ${type.javaClass.simpleName}")

    private fun isForDataBinding(payloads: List<Any>): Boolean {
        if (payloads.isEmpty()) {
            return false
        }
        payloads.forEach {
            if (it != DATA_INVALIDATION) {
                return false
            }
        }
        return true
    }

    private fun notifyCreate(holder: Holder<ViewDataBinding>, type: AbsType<*, ViewDataBinding>) {
        when (type) {
            is Type -> {
                setClickListeners(holder, type)
                type.onCreate?.invoke(holder)
            }
            is ItemType -> type.onCreate(holder)
        }
        holder.created = true
    }

    private fun notifyBind(holder: Holder<ViewDataBinding>, type: AbsType<*, ViewDataBinding>) {
        when (type) {
            is Type -> type.onBind?.invoke(holder)
            is ItemType -> type.onBind(holder)
        }
    }

    private fun notifyRecycle(holder: Holder<ViewDataBinding>, type: AbsType<*, ViewDataBinding>) {
        when (type) {
            is Type -> type.onRecycle?.invoke(holder)
            is ItemType -> type.onRecycle(holder)
        }
    }

    private fun setClickListeners(holder: Holder<ViewDataBinding>, type: Type<*, ViewDataBinding>) {
        val onClick = type.onClick
        if (onClick != null) {
            holder.itemView.setOnClickListener {
                onClick(holder)
            }
        }
        val onLongClick = type.onLongClick
        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick(holder)
                true
            }
        }
    }
}

private fun <P1, R1> (((P1, P1) -> R1)?).invoke(old: Any, new: Any): R1? {
    return this?.invoke(old as P1, new as P1)
}

private fun <T> ItemType<T, *>.areContentsTheSame(old: Any, new: Any): Boolean {
    return areContentsTheSame(old as T, new as T)
}

private fun <T> ItemType<T, *>.areItemSame(old: Any, new: Any): Boolean {
    return areItemSame(old as T, new as T)
}
