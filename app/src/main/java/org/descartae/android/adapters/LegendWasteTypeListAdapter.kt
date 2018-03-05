package org.descartae.android.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.legend_wate_item.view.*
import org.descartae.android.R
import org.descartae.android.TypeOfWasteQuery
import org.descartae.android.view.viewholder.LegendTypeWasteViewHolder

class LegendWasteTypeListAdapter(private val mContext: Context) : RecyclerView.Adapter<LegendTypeWasteViewHolder>() {

    var types: List<TypeOfWasteQuery.TypesOfWaste>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendTypeWasteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.legend_wate_item, parent, false)
        return LegendTypeWasteViewHolder(view)
    }

    override fun onBindViewHolder(holder: LegendTypeWasteViewHolder, position: Int) {
        holder.mItem = types!![position]

        Picasso.with(mContext).load(holder.mItem.icons().androidMediumURL()).resize(100, 100).placeholder(R.drawable.ic_placeholder).centerInside().into(holder.itemView.icon)

        holder.itemView.name.text = holder.mItem.name()
        holder.itemView.name.setTextColor(Color.parseColor("#" + holder.mItem.color()))
        holder.itemView.desc.text = holder.mItem.description()
    }

    override fun getItemCount(): Int {
        return if (types == null) 0 else types!!.size
    }
}
