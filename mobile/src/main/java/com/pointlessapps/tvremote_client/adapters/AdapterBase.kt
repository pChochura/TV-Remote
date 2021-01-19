package com.pointlessapps.tvremote_client.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class AdapterBase<T>(protected open val list: MutableList<T>) :
    RecyclerView.Adapter<DataObjectHolder>() {

    var onClickListener: ((T) -> Unit)? = null

    abstract fun getLayoutId(viewType: Int): Int
    abstract fun onBind(root: View, position: Int)

    open fun onCreate(root: View) = Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataObjectHolder(
            LayoutInflater.from(parent.context!!).inflate(getLayoutId(viewType), parent, false),
            this::onCreate
        )

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) =
        onBind(holder.root, position)

    override fun getItemCount() = list.size

    override fun getItemId(position: Int) = list[position].hashCode().toLong()
}

class DataObjectHolder(itemView: View, onCreateCallback: (View) -> Unit) :
    RecyclerView.ViewHolder(itemView) {
    val root = itemView

    init {
        onCreateCallback.invoke(root)
    }
}