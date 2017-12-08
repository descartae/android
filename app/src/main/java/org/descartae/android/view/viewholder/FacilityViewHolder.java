package org.descartae.android.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

    public FacilityQuery.Item mItem;

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

        mLocationView.setText(mItem.location().municipality());
        mNameView.setText(mItem.name());

        mTypes.removeAllViews();

        for (FacilityQuery.TypesOfWaste typesOfWaste: mItem.typesOfWaste()) {
            ImageView ii = new ImageView(mView.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(20, 0, 20, 0);
            ii.setLayoutParams(lp);
            Picasso.with(mView.getContext()).load(typesOfWaste.icons().androidSmallURL()).resize(100, 100).centerInside().into(ii);
            mTypes.addView(ii);
        }
    }
}