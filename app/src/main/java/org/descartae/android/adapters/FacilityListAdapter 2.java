package org.descartae.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.view.fragments.facility.FacilityFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FacilityListAdapter extends RecyclerView.Adapter<FacilityListAdapter.ViewHolder> {

    private List<FacilityQuery.Center> mCenters;
    private final FacilityFragment.OnListFacilitiesListener mListener;
    private Context mContext;

    public FacilityListAdapter(Context context, FacilityFragment.OnListFacilitiesListener listener) {
        mListener = listener;
        mContext = context;
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
        holder.mLocationView.setText(mCenters.get(position).location().municipality());
        holder.mNameView.setText(mCenters.get(position).name());

        holder.mTypes.removeAllViews();


        for (FacilityQuery.TypesOfWaste typesOfWaste: mCenters.get(position).typesOfWaste()) {
            ImageView ii = new ImageView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(20, 0, 20, 0);
            ii.setLayoutParams(lp);
            Picasso.with(mContext).load(typesOfWaste.icon()).resize(100, 100).centerInside().into(ii);
            holder.mTypes.addView(ii);
        }

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

        @BindView(R.id.location)
        public TextView mLocationView;

        @BindView(R.id.name)
        public TextView mNameView;

        @BindView(R.id.type_waste)
        public LinearLayout mTypes;

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
