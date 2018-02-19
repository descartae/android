package org.descartae.android.view.fragments.empty;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.descartae.android.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyRegionUnsupportedFragment extends Fragment {

    private static final String ARG_LAT = "ARG_LAT";
    private static final String ARG_LNG = "ARG_LNG";

    private double latitude;
    private double longitude;
    private Listener mListener;

    public EmptyRegionUnsupportedFragment() {
    }

    public static EmptyRegionUnsupportedFragment newInstance(double latitude, double longitude) {
        EmptyRegionUnsupportedFragment fragment = new EmptyRegionUnsupportedFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, latitude);
        args.putDouble(ARG_LNG, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LAT);
            longitude = getArguments().getDouble(ARG_LNG);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.region_unsupported_empty, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            mListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Listener");
        }
    }

    @OnClick(R.id.action_notify_me)
    public void onNotifyMe() {
        mListener.showWaitListDialog(latitude, longitude);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Listener {
        void showWaitListDialog(double latitude, double longitude);
    }
}
