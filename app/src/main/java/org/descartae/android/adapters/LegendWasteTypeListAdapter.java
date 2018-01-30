package org.descartae.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.view.viewholder.LegendTypeWasteViewHolder;
import org.descartae.android.view.viewholder.TypeWasteViewHolder;

import java.util.List;

public class LegendWasteTypeListAdapter extends RecyclerView.Adapter<LegendTypeWasteViewHolder> {

    private Context mContext;
    private List<TypeOfWasteQuery.TypesOfWaste> types;

    public LegendWasteTypeListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public LegendTypeWasteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.legend_wate_item, parent, false);
        return new LegendTypeWasteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LegendTypeWasteViewHolder holder, int position) {
        holder.mItem = types.get(position);

        Picasso.with(mContext).load(holder.mItem.icons().androidMediumURL()).resize(100, 100).placeholder(R.drawable.ic_placeholder).centerInside().into(holder.mWasteImage);

        holder.mName.setText(holder.mItem.name());
        holder.mName.setTextColor(Color.parseColor("#" + holder.mItem.color()));
        holder.mDesc.setText(holder.mItem.description());
    }

    @Override
    public int getItemCount() {
        return (types == null) ? 0 : types.size();
    }

    public void setTypes(List<TypeOfWasteQuery.TypesOfWaste> types) {
        this.types = types;
    }
}
