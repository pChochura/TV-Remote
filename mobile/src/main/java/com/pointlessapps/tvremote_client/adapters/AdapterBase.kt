package com.pointlessapps.tvremote_client.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.pointlessapps.tvremote_client.utils.InflateMethod

abstract class AdapterBase<ItemType, Binding : ViewBinding>(
	protected val list: LiveData<List<ItemType>>,
	private val inflateMethod: InflateMethod<Binding>
) : RecyclerView.Adapter<AdapterBase<ItemType, Binding>.ViewHolder>() {

	private val observer = { _: List<ItemType> -> notifyDataSetChanged() }

	var onClickListener: ((ItemType) -> Unit)? = null

	init {
		list.observeForever(observer)
	}

	override fun onViewDetachedFromWindow(holder: ViewHolder) {
		list.removeObserver(observer)
		super.onViewDetachedFromWindow(holder)
	}

	abstract fun onBind(root: Binding, item: ItemType)
	open fun onCreate(root: Binding) = Unit

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = inflateMethod.invoke(LayoutInflater.from(parent.context), parent, false)

		return object : ViewHolder(binding) {
			init {
				onCreate(binding)
				binding.root.setOnClickListener {
					onClickListener?.invoke(
						list.value?.getOrNull(adapterPosition) ?: return@setOnClickListener
					)
				}
			}
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		onBind(holder.binding, list.value?.getOrNull(position) ?: return)
	}

	override fun getItemCount() = list.value?.size ?: 0

	override fun getItemId(position: Int) = list.value?.getOrNull(position).hashCode().toLong()

	open inner class ViewHolder(val binding: Binding) : RecyclerView.ViewHolder(binding.root)
}