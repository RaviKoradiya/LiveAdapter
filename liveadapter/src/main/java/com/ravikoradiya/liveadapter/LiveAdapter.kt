package com.ravikoradiya.liveadapter

import androidx.lifecycle.LiveData
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class LiveAdapter private constructor(
    private val data: LiveData<out List<Any>>?,
    private val list: List<Any>?,
    private val variable: Int?,
    private val stableIds: Boolean?
) : RecyclerView.Adapter<Holder<ViewDataBinding>>() {

    constructor(list: List<Any>?) : this(null, list, null, false)
    constructor(list: List<Any>?, variable: Int) : this(null, list, variable, false)
    constructor(list: List<Any>?, stableIds: Boolean) : this(null, list, null, stableIds)
    constructor(list: List<Any>?, variable: Int, stableIds: Boolean) : this(null, list, variable, stableIds)

    constructor(data: LiveData<out List<Any>>?) : this(data, null, null, false)
    constructor(data: LiveData<out List<Any>>?, variable: Int) : this(data, null, variable, false)
    constructor(data: LiveData<out List<Any>>?, stableIds: Boolean) : this(data, null, null, stableIds)
    constructor(data: LiveData<out List<Any>>?, variable: Int, stableIds: Boolean) : this(data, null, variable, stableIds)

    private val DATA_INVALIDATION = Any()
    private val liveListCallback = LiveListCallback(this)
    private val observableListCallback = ObservableListCallback(this)
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

    inline fun <reified T : Any> map(layout: Int, variable: Int? = null) = map(T::class.java, layout, variable)

    fun <T : Any> map(clazz: Class<T>, type: AbsType<*>) = apply { map[clazz] = type }

    inline fun <reified T : Any> map(type: AbsType<*>) = map(T::class.java, type)

    inline fun <reified T : Any, B : ViewDataBinding> map(
        layout: Int,
        variable: Int? = null,
        noinline f: (Type<B>.() -> Unit)? = null
    ) = map(T::class.java, Type<B>(layout, variable).apply { f?.invoke(this) })

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

    inline fun type(crossinline f: (Any, Int) -> AbsType<*>?) = handler(object : TypeHandler {
        override fun getItemType(item: Any, position: Int) = f(item, position)
    })

    fun into(recyclerView: RecyclerView) = apply { recyclerView.adapter = this }


    override fun onCreateViewHolder(view: ViewGroup, viewType: Int): Holder<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, view, false)
        val holder = Holder(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding) = recyclerView?.isComputingLayout ?: false
            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView?.isComputingLayout ?: true) {
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
        holder.binding.setVariable(getVariable(type), data?.value?.get(position) ?: list?.get(position))
        holder.binding.executePendingBindings()
        @Suppress("UNCHECKED_CAST")
        if (type is AbsType<*>) {
            if (!holder.created) {
                notifyCreate(holder, type as AbsType<ViewDataBinding>)
            }
            notifyBind(holder, type as AbsType<ViewDataBinding>)
        }
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int, payloads: List<Any>) {
        if (isForDataBinding(payloads)) {
            holder.binding.executePendingBindings()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: Holder<ViewDataBinding>) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION && position < ((data?.value?.size) ?: (list?.size ?: 0))) {
            val type = getType(position)!!
            if (type is AbsType<*>) {
                @Suppress("UNCHECKED_CAST")
                notifyRecycle(holder, type as AbsType<ViewDataBinding>)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if (hasStableIds()) {
            val item = ((data?.value?.get(position)) ?: (list?.get(position) ?: Any()))
            if (item is StableId) {
                return item.stableId
            } else {
                throw IllegalStateException("${item.javaClass.simpleName} must implement StableId interface.")
            }
        } else {
            return super.getItemId(position)
        }
    }

    override fun getItemCount() = ((data?.value?.size) ?: (list?.size ?: 0))

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        if (recyclerView == null) {
            data?.observeForever(liveListCallback)

            if (list is ObservableList?)
                list?.addOnListChangedCallback(observableListCallback)
        }
        recyclerView = rv
        inflater = LayoutInflater.from(rv.context)
    }

    override fun onDetachedFromRecyclerView(rv: RecyclerView) {
        if (recyclerView != null) {
            data?.removeObserver(liveListCallback)

            if (list is ObservableList?)
                list?.removeOnListChangedCallback(observableListCallback)
        }
        recyclerView = null
    }

    override fun getItemViewType(position: Int) =
        layoutHandler?.getItemLayout((data?.value?.get(position) ?: (list?.get(position) ?: Any())), position)
            ?: typeHandler?.getItemType(
                (data?.value?.get(position) ?: (list?.get(position) ?: Any())),
                position
            )?.layout
            ?: getType(position)?.layout
            ?: throw RuntimeException(
                "Invalid object at position $position: ${((data?.value?.get(position) ?: (list?.get(position)
                    ?: Any()))).javaClass}"
            )

    private fun getType(position: Int) =
        typeHandler?.getItemType((data?.value?.get(position) ?: (list?.get(position) ?: Any())), position)
            ?: map[((data?.value?.get(position) ?: (list?.get(position) ?: Any()))).javaClass]

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

    private fun notifyCreate(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> {
                setClickListeners(holder, type)
                type.onCreate?.invoke(holder)
            }
            is ItemType -> type.onCreate(holder)
        }
        holder.created = true
    }

    private fun notifyBind(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> type.onBind?.invoke(holder)
            is ItemType -> type.onBind(holder)
        }
    }

    private fun notifyRecycle(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> type.onRecycle?.invoke(holder)
            is ItemType -> type.onRecycle(holder)
        }
    }

    private fun setClickListeners(holder: Holder<ViewDataBinding>, type: Type<ViewDataBinding>) {
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
