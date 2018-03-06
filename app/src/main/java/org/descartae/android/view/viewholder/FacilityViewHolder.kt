package org.descartae.android.view.viewholder

import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_facility.view.*
import org.descartae.android.FacilitiesQuery
import org.descartae.android.R

class FacilityViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

    var mItem: FacilitiesQuery.Item? = null
    private var currentLocation: Location? = null

    fun fill() {

        mItem?.let {
            itemView.location.text = it.location().address()
            itemView.name.text = it.name()
            itemView.type_waste.removeAllViews()

            for (typesOfWaste in it.typesOfWaste()) {
                val ii = ImageView(mView.context)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)

                if (itemView.type_waste.childCount == 0) {
                    lp.setMargins(0, 0, 10, 0)
                } else {
                    lp.setMargins(10, 0, 10, 0)
                }

                ii.layoutParams = lp

                // More Icons
                if (itemView.type_waste.childCount == 4) {
                    Picasso.with(mView.context).load(R.drawable.ic_waste_more).resizeDimen(R.dimen.type_waste_height, R.dimen.type_waste_height).into(ii)
                    itemView.type_waste.addView(ii)
                    break
                } else {
                    Picasso.with(mView.context)
                            .load(typesOfWaste.icons().androidMediumURL())
                            .resizeDimen(R.dimen.type_waste_height, R.dimen.type_waste_height)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(ii)
                    itemView.type_waste.addView(ii)
                }
            }

            // Distance
            if (currentLocation != null) {
                val facilityLocation = Location("Facility")
                facilityLocation.latitude = it.location().coordinates().latitude()
                facilityLocation.longitude = it.location().coordinates().longitude()

                val distance = currentLocation!!.distanceTo(facilityLocation)
                itemView.distance.text = mView.context.getString(R.string.distance, distance / 1000)
            }
        }
    }

    fun setCurrentLocation(currentLocation: Location?) {
        this.currentLocation = currentLocation
    }
}