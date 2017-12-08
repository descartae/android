package org.descartae.android.view.fragments.facility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import org.descartae.android.adapters.FacilityListAdapter;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;
import org.descartae.android.view.viewholder.FacilityViewHolder;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FacilityFragment extends Fragment implements ConnectionClassManager.ConnectionClassStateChangeListener {

    private OnListFacilitiesListener mListener;
    private FacilityListAdapter facilityListAdapter;

    @BindView(R.id.container)
    public CoordinatorLayout coordinatorLayout;

    @BindView(R.id.list)
    public RecyclerView recyclerView;

    @BindView(R.id.bottom_sheet)
    public View bottomSheetList;

    @BindView(R.id.bottom_sheet_detail)
    public View bottomSheetDetail;

    private FacilityViewHolder facilityViewHolder;

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
        ConnectionClassManager.getInstance().register(this);
        DeviceBandwidthSampler.getInstance().startSampling();
        query();
        DeviceBandwidthSampler.getInstance().stopSampling();
    }

    private void query() {

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl(NetworkingConstants.BASE_URL)
            .build();
        FacilityQuery facilityQuery = FacilityQuery.builder().build();
        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilityQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilityQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                getActivity().runOnUiThread(() -> {
                    facilityListAdapter.setCenters(dataResponse.data().facilities().items());
                    facilityListAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());
                
                if (getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing()) {
                    return;
                }

                ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                if (cq.equals(ConnectionQuality.UNKNOWN)) {
                    mListener.onNoConnection();
                }
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
        facilityListAdapter = new FacilityListAdapter(getActivity(), (FacilityQuery.Item center) -> {

            bottomSheetDetail.setVisibility(View.VISIBLE);
            bottomSheetList.setVisibility(View.GONE);

            facilityViewHolder = new FacilityViewHolder(bottomSheetDetail);
            facilityViewHolder.mItem = center;
            facilityViewHolder.fill();
        });
        recyclerView.setAdapter(facilityListAdapter);

        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheetList);
        behavior.setHideable(true);
        behavior.setPeekHeight(getResources().getDimensionPixelOffset(R.dimen.facilities_peek_height));

        final BottomSheetBehavior behaviorDetail = BottomSheetBehavior.from(bottomSheetDetail);
        behaviorDetail.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetList.setVisibility(View.GONE);
                } else {
                    bottomSheetList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        behaviorDetail.setPeekHeight(getResources().getDimensionPixelOffset(R.dimen.facility_peek_height));

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

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        if (bandwidthState.equals(ConnectionQuality.UNKNOWN)) {
            mListener.onNoConnection();
        }
    }

    public interface OnListFacilitiesListener {
        void onNoConnection();
    }

    public interface OnFacilityListener {
        void onListFacilityInteraction(FacilityQuery.Item facility);
    }
}
