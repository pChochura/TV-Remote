package com.pointlessapps.tvremote_server

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*

class MainFragment : BrowseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupUIElements()
        setupEventListeners()
        loadRows()
    }

    private fun setupUIElements() {
        title = getString(R.string.title)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = false

        brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
    }

    private fun loadRows() {
        adapter = ArrayObjectAdapter(ListRowPresenter()).also {
            it.add(ListRow(HeaderItem("Connected devices"), ArrayObjectAdapter(GridItemPresenter()).apply {
                add("Device 1")
                add("Device 2")
                add("Device 3")
            }))
            it.add(ListRow(HeaderItem("Settings"), ArrayObjectAdapter(GridItemPresenter()).apply {
                add("Change port (7654)")
                add("Show IP address")
            }))
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = ItemViewClickedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any, rowViewHolder: RowPresenter.ViewHolder, row: Row) {
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) = Unit
    }

    companion object {
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
    }
}
