package org.descartae.android.view.facility;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import org.descartae.android.view.facility.adapter.FacilityListAdapter;
import org.descartae.android.view.networking.NetworkingConstants;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FacilityFragment extends Fragment {

    private OnListFacilitiesListener mListener;
    private FacilityListAdapter facilityListAdapter;

    @BindView(R.id.container)
    public CoordinatorLayout coordinatorLayout;

    @BindView(R.id.list)
    public RecyclerView recyclerView;

    public FacilityFragment() {
    }

    public static FacilityFragment newInstance() {
        FacilityFragment fragment = new FacilityFragment();
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

        ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl(NetworkingConstants.BASE_URL)
            .build();
        FacilityQuery facilityQuery = FacilityQuery.builder().build();
        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilityQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilityQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        facilityListAdapter.setCenters(dataResponse.data().centers());
                        facilityListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_list, container, false);

        ButterKnife.bind(this, view);

        Context context = view.getContext();
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        facilityListAdapter = new FacilityListAdapter(getActivity(), mListener);
        recyclerView.setAdapter(facilityListAdapter);

        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // @TODO on expanded
                } else {

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        behavior.setHideable(true);
        behavior.setPeekHeight(getResources().getDimensionPixelOffset(R.dimen.facilities_peek_height));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFacilitiesListener) {
            mListener = (OnListFacilitiesListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFacilitiesListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFacilitiesListener {
        void onListFacilityInteraction(FacilityQuery.Center center);
    }
}
