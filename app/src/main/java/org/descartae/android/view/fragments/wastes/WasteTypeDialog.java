package org.descartae.android.view.fragments.wastes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.descartae.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lucasmontano on 09/12/2017.
 */

public class WasteTypeDialog extends DialogFragment {

    @BindView(R.id.name)
    public TextView mName;

    @BindView(R.id.desc)
    public TextView mDesc;

    @BindView(R.id.icon)
    public ImageView mIcon;

    public static WasteTypeDialog newInstance(String name, String desc, String icon) {
        WasteTypeDialog frag = new WasteTypeDialog();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("desc", desc);
        args.putString("icon", icon);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String name = getArguments().getString("name");
        String desc = getArguments().getString("desc");
        String icon = getArguments().getString("icon");

        View v = inflater.inflate(R.layout.fragment_waste_type_dialog, container, false);

        ButterKnife.bind(this, v);

        mName.setText(name);
        mDesc.setText(desc);
        Picasso.with(getActivity()).load(icon).placeholder(R.drawable.ic_placeholder).into(mIcon);

        return v;
    }

    @OnClick(R.id.close)
    public void onClose() {
        dismiss();
    }
}
