package org.descartae.android.view.fragments.facility;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import org.descartae.android.adapters.FacilityListAdapter;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.view.activities.FacilityActivity;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;
import org.descartae.android.view.viewholder.FacilityViewHolder;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacilitiesFragment extends Fragment implements ConnectionClassManager.ConnectionClassStateChangeListener {

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

    @BindView(R.id.loading)
    public View mLoading;

    private FacilityViewHolder facilityViewHolder;

    private BottomSheetBehavior<View> behaviorDetail;
    private BottomSheetBehavior<View> behaviorList;
    private FacilitiesQuery.Item mItemSelected;

    public FacilitiesFragment() {
    }

    public static FacilitiesFragment newInstance() {
        FacilitiesFragment fragment = new FacilitiesFragment();
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
        FacilitiesQuery facilityQuery = FacilitiesQuery.builder().build();

        mLoading.setVisibility(View.VISIBLE);

        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilitiesQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilitiesQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;
                if (getActivity() == null || getActivity().isDestroyed()) return;

                getActivity().runOnUiThread(() -> {
                    facilityListAdapter.setCenters(dataResponse.data().facilities().items());
                    facilityListAdapter.notifyDataSetChanged();
                    mLoading.setVisibility(View.GONE);
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

                    getActivity().runOnUiThread(() -> {
                        mListener.onNoConnection();
                        mLoading.setVisibility(View.GONE);
                    });
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
        facilityListAdapter = new FacilityListAdapter(getActivity(), (FacilitiesQuery.Item center) -> {

            // On Facility Item Clicked
            mItemSelected = center;

            // Fill Item Detail
            facilityViewHolder = new FacilityViewHolder(bottomSheetDetail);
            facilityViewHolder.mItem = center;
            facilityViewHolder.fill();

            // Show BottomSheetDetail
            bottomSheetDetail.setVisibility(View.VISIBLE);
            behaviorDetail.setState(BottomSheetBehavior.STATE_EXPANDED);

            // List Collapse and GONE
            behaviorList.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetList.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(facilityListAdapter);

        behaviorList = BottomSheetBehavior.from(bottomSheetList);
        behaviorList.setHideable(false);
        behaviorList.setPeekHeight(getResources().getDimensionPixelOffset(R.dimen.facilities_peek_height));

        behaviorDetail = BottomSheetBehavior.from(bottomSheetDetail);
        behaviorDetail.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                    // List VISIBLE and Expand
                    bottomSheetList.setVisibility(View.VISIBLE);
                    behaviorList.setState(BottomSheetBehavior.STATE_EXPANDED);

                    mItemSelected = null;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });
        behaviorDetail.setHideable(true);

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

    @OnClick(R.id.action_detail)
    public void onActionDetail() {

        if (mItemSelected == null) return;

        Intent intent = new Intent(getActivity(), FacilityActivity.class);
        intent.putExtra(FacilityActivity.ARG_ID, mItemSelected._id());
        startActivity(intent);
    }

    public interface OnListFacilitiesListener {
        void onNoConnection();
    }

    public interface OnFacilityListener {
        void onListFacilityInteraction(FacilitiesQuery.Item facility);
    }
}
