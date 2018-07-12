package org.descartae.android.view.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import org.descartae.android.FacilityQuery

class TypeWasteViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
  var mItem: FacilityQuery.TypesOfWaste? = null
}