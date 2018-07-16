package org.descartae.android.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.legend_wate_item.view.desc
import kotlinx.android.synthetic.main.legend_wate_item.view.icon
import kotlinx.android.synthetic.main.legend_wate_item.view.name
import org.descartae.android.R
import org.descartae.android.TypeOfWasteQuery
import org.descartae.android.view.viewholder.LegendTypeWasteViewHolder

class LegendWasteTypeListAdapter : RecyclerView.Adapter<LegendTypeWasteViewHolder>() {

  var types: List<TypeOfWasteQuery.TypesOfWaste>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendTypeWasteViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.legend_wate_item, parent, false)
    return LegendTypeWasteViewHolder(view)
  }

  override fun onBindViewHolder(holder: LegendTypeWasteViewHolder, position: Int) {

    types!![position].let {
      Picasso.get()
          .load(it.icons()
          .androidMediumURL())
          .resize(100, 100)
          .placeholder(R.drawable.ic_placeholder)
          .centerInside()
          .into(holder.itemView.icon)

      holder.itemView.name.text = it.name()
      holder.itemView.name.setTextColor(Color.parseColor("#" + it.color()))
      holder.itemView.desc.text = it.description()
    }
  }

  override fun getItemCount(): Int {
    return if (types == null) 0 else types!!.size
  }
}
