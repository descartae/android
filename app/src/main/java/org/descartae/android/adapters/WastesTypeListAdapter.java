package org.descartae.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.view.viewholder.TypeWasteViewHolder;

import java.util.List;

public class WastesTypeListAdapter extends RecyclerView.Adapter<TypeWasteViewHolder> {

    private List<FacilityQuery.TypesOfWaste> mTypes;
    private Context mContext;

    TypeOfWasteListner mListener;

    public WastesTypeListAdapter(Context context, TypeOfWasteListner listner) {
        mContext = context;
        mListener = listner;
    }

    @Override
    public TypeWasteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waste_type_item, parent, false);
        return new TypeWasteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TypeWasteViewHolder holder, int position) {
        holder.mItem = mTypes.get(position);

        Picasso.with(mContext).load(holder.mItem.icons().androidMediumURL()).resize(100, 100).placeholder(R.drawable.ic_waste_more).centerInside().into(holder.mWasteImage);

        holder.mName.setText(holder.mItem.name());

        holder.mView.setOnClickListener((View v) -> {
            if (null != mListener) {
                mListener.onTypeClick(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mTypes == null) ? 0 : mTypes.size();
    }

    public void setTypes(List<FacilityQuery.TypesOfWaste> typesOfWastes) {
        mTypes = typesOfWastes;
    }

    public interface TypeOfWasteListner {
        void onTypeClick(FacilityQuery.TypesOfWaste typesOfWaste);
    }
}
