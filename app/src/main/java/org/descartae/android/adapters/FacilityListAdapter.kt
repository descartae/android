package org.descartae.android.adapters

import android.location.Location
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import org.descartae.android.FacilitiesQuery
import org.descartae.android.R
import org.descartae.android.view.viewholder.FacilityViewHolder

class FacilityListAdapter(private val mListener: (FacilitiesQuery.Item) -> Unit) : RecyclerView.Adapter<FacilityViewHolder>() {

    var centers: List<FacilitiesQuery.Item>? = null
    var currentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_facility, parent, false)
        return FacilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        holder.mItem = centers!![position]
        holder.setCurrentLocation(currentLocation)
        holder.fill()

        holder.itemView.setOnClickListener {
            mListener.invoke(holder.mItem!!)
        }
    }

    override fun getItemCount(): Int {
        return if (centers == null) 0 else centers!!.size
    }
}