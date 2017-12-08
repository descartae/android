package org.descartae.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.view.fragments.facility.FacilityFragment;
import org.descartae.android.view.viewholder.FacilityViewHolder;

import java.util.List;

public class FacilityListAdapter extends RecyclerView.Adapter<FacilityViewHolder> {

    private List<FacilityQuery.Item> mFacilities;
    private final FacilityFragment.OnFacilityListener mListener;
    private Context mContext;

    public FacilityListAdapter(Context context, FacilityFragment.OnFacilityListener listener) {
        mListener = listener;
        mContext = context;
    }

    @Override
    public FacilityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FacilityViewHolder holder, int position) {
        holder.mItem = mFacilities.get(position);
        holder.fill();

        holder.mView.setOnClickListener((View v) -> {
            if (null != mListener) {
                mListener.onListFacilityInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mFacilities == null) ? 0 : mFacilities.size();
    }

    public void setCenters(List<FacilityQuery.Item> facilities) {
        mFacilities = facilities;
    }
}
