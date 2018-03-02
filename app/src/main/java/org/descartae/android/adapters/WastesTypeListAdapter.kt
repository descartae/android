package org.descartae.android.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import org.descartae.android.FacilityQuery
import org.descartae.android.R
import org.descartae.android.view.viewholder.TypeWasteViewHolder

class WastesTypeListAdapter(private val mContext: Context, internal var mListener: TypeOfWasteListner?) : RecyclerView.Adapter<TypeWasteViewHolder>() {

    var types: List<FacilityQuery.TypesOfWaste>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeWasteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.waste_type_item, parent, false)
        return TypeWasteViewHolder(view)
    }

    override fun onBindViewHolder(holder: TypeWasteViewHolder, position: Int) {
        holder.mItem = types!![position]

        Picasso.with(mContext).load(holder.mItem.icons().androidMediumURL())
            .resize(100, 100)
            .placeholder(R.drawable.ic_placeholder)
            .centerInside().into(holder.mWasteImage)

        holder.mName.text = holder.mItem.name()

        holder.mView.setOnClickListener { mListener.let { it?.onTypeClick(holder.mItem) } }
    }

    override fun getItemCount(): Int {
        return if (types == null) 0 else types!!.size
    }

    interface TypeOfWasteListner {
        fun onTypeClick(typesOfWaste: FacilityQuery.TypesOfWaste)
    }
}
