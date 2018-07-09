package org.descartae.android.view.utils

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpaceDividerItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        parent.adapter?.let {
            if (parent.getChildAdapterPosition(view) != it.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}
