package org.descartae.android.view.fragments.empty;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.descartae.android.R;
import org.descartae.android.interfaces.RetryConnectionView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyGPSOfflineFragment extends Fragment {

    private RetryConnectionView mListener;

    public EmptyGPSOfflineFragment() {
    }

    public static EmptyGPSOfflineFragment newInstance() {
        EmptyGPSOfflineFragment fragment = new EmptyGPSOfflineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps_offl_empty, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RetryConnectionView) {
            mListener = (RetryConnectionView) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RetryConnectionView");
        }
    }

    @OnClick(R.id.action_try)
    public void actionTry() {
        mListener.onRetryConnection();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
