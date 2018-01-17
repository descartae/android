package org.descartae.android.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucasmontano on 07/12/2017.
 */
public class TypeWasteViewHolder extends RecyclerView.ViewHolder {

    public final View mView;

    @BindView(R.id.waste_name)
    public TextView mName;

    @BindView(R.id.waste_image)
    public ImageView mWasteImage;

    public FacilityQuery.TypesOfWaste mItem;

    public TypeWasteViewHolder(View view) {
        super(view);
        mView = view;
        ButterKnife.bind(this, view);
    }
}