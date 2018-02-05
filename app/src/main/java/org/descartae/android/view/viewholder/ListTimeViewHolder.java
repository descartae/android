package org.descartae.android.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.descartae.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucasmontano on 11/01/2018.
 */
public class ListTimeViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.day)
    public TextView mDay;

    @BindView(R.id.time)
    public TextView mTime;

    public ListTimeViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}
