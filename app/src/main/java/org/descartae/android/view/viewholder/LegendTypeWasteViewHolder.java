package org.descartae.android.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.TypeOfWasteQuery;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucasmontano on 07/12/2017.
 */
public class LegendTypeWasteViewHolder extends RecyclerView.ViewHolder {

    public final View mView;

    @BindView(R.id.name)
    public TextView mName;

    @BindView(R.id.desc)
    public TextView mDesc;

    @BindView(R.id.icon)
    public ImageView mWasteImage;

    public TypeOfWasteQuery.TypesOfWaste mItem;

    public LegendTypeWasteViewHolder(View view) {
        super(view);
        mView = view;
        ButterKnife.bind(this, view);
    }
}