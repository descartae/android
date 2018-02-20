package org.descartae.android.view.fragments.facility;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.DescartaeApp;
import org.descartae.android.FacilitiesQuery;
import org.descartae.android.R;

import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.adapters.FacilityListAdapter;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError;
import org.descartae.android.presenter.facility.FacilityListPresenter;
import org.descartae.android.view.activities.FacilityActivity;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;
import org.descartae.android.view.viewholder.FacilityViewHolder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacilitiesFragment extends Fragment implements OnMapReadyCallback {

    private FacilityListAdapter facilityListAdapter;

    @Inject FacilityListPresenter presenter;

    @Inject EventBus eventBus;

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

    @BindView(R.id.filter_empty)
    public View mFilterEmpty;

    private FacilityViewHolder facilityViewHolder;

    private BottomSheetBehavior<View> behaviorDetail;
    private BottomSheetBehavior<View> behaviorList;
    private FacilitiesQuery.Item mItemSelected;

    private MapFragment mMapFragment;
    private GoogleMap mMap;

    private List<TypeOfWasteQuery.TypesOfWaste> typesOfWasteData;
    private String[] typesOfWasteTitle;
    private Integer[] selectedTypesIndices = {};

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
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_filter:

                if ( ! presenter.haveCurrentLocation()) {
                    return false;
                }

                if (typesOfWasteTitle == null || typesOfWasteTitle.length <= 0) {
                    Log.d("Filter", "No Types");
                    return false;
                }

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.title_filter)
                        .items(typesOfWasteTitle)
                        .itemsCallbackMultiChoice(selectedTypesIndices, getCallbackMultiChoiceFilter())
                        .positiveText(R.string.action_filter)
                        .show();

                return true;
        }
        return false;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * Init Dagger
         */
        DescartaeApp.getInstance(getActivity())
                .getAppComponent()
                .inject(this);

        /**
         * Load Type of Waste in order to be fetched before click on filter option
         */
        queryTypeOfWastes();
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    private void queryTypeOfWastes() {

        ApolloClient apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build();
        TypeOfWasteQuery typeOfWasteQuery = TypeOfWasteQuery.builder().build();
        apolloClient.query(typeOfWasteQuery).enqueue(new ApolloCall.Callback<TypeOfWasteQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<TypeOfWasteQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                typesOfWasteData = dataResponse.data().typesOfWaste();
                typesOfWasteTitle = new String[typesOfWasteData.size()];

                int i = 0;
                for (TypeOfWasteQuery.TypesOfWaste type : typesOfWasteData) {
                    typesOfWasteTitle[i] = type.name();
                    i++;
                }
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

        mMapFragment = MapFragment.newInstance();
        getActivity().getFragmentManager().beginTransaction().replace(R.id.map, mMapFragment).commitAllowingStateLoss();

        Context context = view.getContext();
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        facilityListAdapter = new FacilityListAdapter(getActivity(), (FacilitiesQuery.Item center) -> {

            // On Facility Item Clicked
            mItemSelected = center;
            selectFacility(center);
        });
        recyclerView.setAdapter(facilityListAdapter);

        behaviorList = BottomSheetBehavior.from(bottomSheetList);
        behaviorList.setHideable(false);
        behaviorList.setPeekHeight(getResources().getDimensionPixelOffset(R.dimen.facilities_peek_height));

        behaviorDetail = BottomSheetBehavior.from(bottomSheetDetail);
        behaviorDetail.setBottomSheetCallback(getCallbackBottomSheet());
        behaviorDetail.setHideable(true);

        return view;
    }

    private void selectFacility(FacilitiesQuery.Item center) {

        // Fill Item Detail
        facilityViewHolder = new FacilityViewHolder(bottomSheetDetail);
        facilityViewHolder.mItem = center;
        facilityViewHolder.setCurrentLocation(presenter.getCurrentLocation());
        facilityViewHolder.fill();

        // Show BottomSheetDetail
        bottomSheetDetail.setVisibility(View.VISIBLE);
        behaviorDetail.setState(BottomSheetBehavior.STATE_EXPANDED);

        // List Collapse and GONE
        behaviorList.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetList.setVisibility(View.GONE);

        // Move Map
        if (mMap != null) {

            mMap.clear();

            LatLng latlng = new LatLng(
                    mItemSelected.location().coordinates().latitude(),
                    mItemSelected.location().coordinates().longitude()
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                            .position(latlng)
                            .title(mItemSelected.name()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        fillMapMarkers();

        mMap.setOnMarkerClickListener((Marker marker) -> {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));
            selectFacility(marker.getTitle());
            return false;
        });
    }

    private void selectFacility(String name) {
        List<FacilitiesQuery.Item> centers = facilityListAdapter.getCenters();
        if (centers != null && centers.size() > 0) {
            for (FacilitiesQuery.Item facility : centers) {
                if (facility.name().equals(name)) {
                    mItemSelected = facility;
                    selectFacility(facility);
                    break;
                }
            }
        }
    }

    private void fillMapMarkers() {

        mMap.clear();

        // Add pins to map
        List<FacilitiesQuery.Item> facilities = facilityListAdapter.getCenters();
        if (facilities != null && facilities.size() > 0) {

            for (FacilitiesQuery.Item facility : facilities) {

                LatLng latlng = new LatLng(facility.location().coordinates().latitude(), facility.location().coordinates().longitude());
                mMap.addMarker(
                        new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_places_map))
                                .position(latlng)
                                .snippet(facility.location().address())
                                .title(facility.name()));
            }
        }

        // Move camera
        Location currentLocation = presenter.getCurrentLocation();

        if (currentLocation != null && mMap != null) {
            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
        }
    }

    @NonNull
    private MaterialDialog.ListCallbackMultiChoice getCallbackMultiChoiceFilter() {
        return (MaterialDialog dialog, Integer[] which, CharSequence[] text) -> {

            selectedTypesIndices = which;

            List<String> selected = new ArrayList<>();
            for (Integer index : which) {
                selected.add(typesOfWasteData.get(index)._id());
            }

            // Clear List
            facilityListAdapter.setCenters(null);
            facilityListAdapter.notifyDataSetChanged();

            // Refresh MapMarkers
            fillMapMarkers();

            presenter.setFilterTypesID(selected);
            presenter.requestFacilities();

            return true;
        };
    }

    @NonNull
    private BottomSheetBehavior.BottomSheetCallback getCallbackBottomSheet() {
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                    // List VISIBLE and Expand
                    bottomSheetList.setVisibility(View.VISIBLE);
                    behaviorList.setState(BottomSheetBehavior.STATE_EXPANDED);

                    mItemSelected = null;

                    // Reset Map Pins
                    fillMapMarkers();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        };
    }

    @OnClick(R.id.action_detail)
    public void onActionDetail() {

        if (mItemSelected == null) return;

        Intent intent = new Intent(getActivity(), FacilityActivity.class);
        intent.putExtra(FacilityActivity.ARG_ID, mItemSelected._id());
        startActivity(intent);
    }

    @OnClick(R.id.action_go)
    public void onActionGo() {

        if (mItemSelected == null) return;

        String uri = "geo:" + mItemSelected.location().coordinates().latitude() + ","
                + mItemSelected.location().coordinates().longitude() + "?q=" + mItemSelected.location().coordinates().latitude()
                + "," + mItemSelected.location().coordinates().longitude();
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    @OnClick(R.id.action_clear_filter)
    public void onClearFilters() {
        selectedTypesIndices = new Integer[]{};

        mFilterEmpty.setVisibility(View.GONE);

        // Clear List
        facilityListAdapter.setCenters(null);
        facilityListAdapter.notifyDataSetChanged();

        // Refresh MapMarkers
        fillMapMarkers();

        presenter.setFilterTypesID(null);
        presenter.requestFacilities();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventHideLoading(EventHideLoading event) {
        mLoading.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventShowLoading(EventShowLoading event) {
        mLoading.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renderFacilities(FacilitiesQuery.Facilities facilities) {

        if (facilities == null && presenter.hasFilterType()) {

            /**
             * If no facilities return with filter
             */
            mFilterEmpty.setVisibility(View.VISIBLE);

        } else if (facilities != null) {

            /**
             * If have facilities
             */
            facilityListAdapter.setCenters(facilities.items());
            facilityListAdapter.setCurrentLocation(presenter.getCurrentLocation());
            facilityListAdapter.notifyDataSetChanged();

            mMapFragment.getMapAsync(FacilitiesFragment.this);
        } else {

            /**
             * If no facilities and no filter
             * @deprecated the server is already checkin this situation as Error
             */
            eventBus.post(new RegionNotSupportedError());
        }
    }

    public boolean isBottomSheetOpen() {
        return behaviorDetail.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public void closeBottomSheet() {
        behaviorDetail.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

}
