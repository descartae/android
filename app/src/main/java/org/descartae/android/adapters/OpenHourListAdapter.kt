package org.descartae.android.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.descartae.android.FacilityQuery
import org.descartae.android.R
import org.descartae.android.view.viewholder.ListTimeViewHolder

class OpenHourListAdapter(private val mContext: Context) : RecyclerView.Adapter<ListTimeViewHolder>() {

    private var openHourList: List<FacilityQuery.OpenHour>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.time_item, parent, false)
        return ListTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListTimeViewHolder, position: Int) {

        openHourList.let {
            val openHour = it?.get(position)

            openHour.let {
                val daysWeek = mContext.resources.getStringArray(R.array.day_of_week)
                holder.mDay.text = daysWeek[openHour!!.dayOfWeek().ordinal]
                holder.mTime.text = mContext.getString(R.string.time_desc, it?.startTime(), it?.endTime())
            }
        }
    }

    fun setFacilityDays(openHourList: List<FacilityQuery.OpenHour>) {
        this.openHourList = openHourList
    }

    override fun getItemCount(): Int {
        return if (openHourList == null) 0 else openHourList!!.size
    }
}
