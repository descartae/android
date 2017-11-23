package org.descartae.android.view.facility.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.view.facility.FacilityFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FacilityListAdapter extends RecyclerView.Adapter<FacilityListAdapter.ViewHolder> {

    private List<FacilityQuery.Center> mCenters;
    private final FacilityFragment.OnListFacilitiesListener mListener;

    public FacilityListAdapter(FacilityFragment.OnListFacilitiesListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_facility, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mCenters.get(position);
        holder.mIdView.setText(mCenters.get(position)._id());
        holder.mNameView.setText(mCenters.get(position).name());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFacilityInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mCenters == null) ? 0 : mCenters.size();
    }

    public void setCenters(List<FacilityQuery.Center> centers) {
        mCenters = centers;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        @BindView(R.id.id)
        public TextView mIdView;

        @BindView(R.id.name)
        public TextView mNameView;

        public FacilityQuery.Center mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
