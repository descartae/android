package org.descartae.android.view.viewholder;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucasmontano on 07/12/2017.
 */

public class FacilityViewHolder extends RecyclerView.ViewHolder {

    public final View mView;

    @BindView(R.id.location)
    public TextView mLocationView;

    @BindView(R.id.name)
    public TextView mNameView;

    @BindView(R.id.type_waste)
    public LinearLayout mTypes;

    @BindView(R.id.distance)
    public TextView mDistance;

    public FacilitiesQuery.Item mItem;
    private Location currentLocation;

    public FacilityViewHolder(View view) {
        super(view);
        mView = view;
        ButterKnife.bind(this, view);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mNameView.getText() + "'";
    }

    public void fill() {

        mLocationView.setText(mItem.location().address());
        mNameView.setText(mItem.name());

        mTypes.removeAllViews();

        for (FacilitiesQuery.TypesOfWaste typesOfWaste: mItem.typesOfWaste()) {
            ImageView ii = new ImageView(mView.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            if (mTypes.getChildCount() == 0) {
                lp.setMargins(0, 0, 10, 0);
            } else {
                lp.setMargins(10, 0, 10, 0);
            }

            ii.setLayoutParams(lp);

            // More Icons
            if (mTypes.getChildCount() == 4) {
                Picasso.with(mView.getContext()).load(R.drawable.ic_waste_more).into(ii);
                mTypes.addView(ii);
                break;
            } else {
                Picasso.with(mView.getContext()).load(typesOfWaste.icons().androidSmallURL()).placeholder(R.drawable.ic_placeholder).into(ii);
                mTypes.addView(ii);
            }
        }

        // Distance
        if (currentLocation != null) {
            Location facilityLocation = new Location("Facility");
            facilityLocation.setLatitude(mItem.location().coordinates().latitude());
            facilityLocation.setLongitude(mItem.location().coordinates().longitude());

            float distance = currentLocation.distanceTo(facilityLocation);
            mDistance.setText(mView.getContext().getString(R.string.distance, distance / 1000));
        }
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}