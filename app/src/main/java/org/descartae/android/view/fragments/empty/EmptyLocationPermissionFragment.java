package org.descartae.android.view.fragments.empty;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.descartae.android.R;
import org.descartae.android.interfaces.RequestPermissionView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyLocationPermissionFragment extends Fragment {

    private RequestPermissionView mListener;

    public EmptyLocationPermissionFragment() {
    }

    public static EmptyLocationPermissionFragment newInstance() {
        EmptyLocationPermissionFragment fragment = new EmptyLocationPermissionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission_empty, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RequestPermissionView) {
            mListener = (RequestPermissionView) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RequestPermissionView");
        }
    }

    @OnClick(R.id.action_allow)
    public void actionAllow() {
        mListener.onAcceptPermission();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
