package com.pointlessapps.tvremote_client.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class AdapterBase<T, Binding : ViewBinding>(
    protected open val list: MutableList<T>,
    private val bindingClass: Class<Binding>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: Binding
    var onClickListener: ((T) -> Unit)? = null

    init {
        @Suppress("LeakingThis")
        setHasStableIds(true)
    }

    abstract fun onBind(root: Binding, position: Int)
    open fun onCreate(root: Binding) = Unit

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, LayoutInflater.from(parent.context), parent, false) as Binding

        return object : RecyclerView.ViewHolder(binding.root) {
            init {
                onCreate(binding)
                binding.root.setOnClickListener {
                    onClickListener?.invoke(list[adapterPosition])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        onBind(binding, position)

    override fun getItemCount() = list.size

    override fun getItemId(position: Int) = list[position].hashCode().toLong()
}