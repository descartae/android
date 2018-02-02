package org.descartae.android.view.fragments.facility;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.R;

import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.adapters.FacilityListAdapter;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.view.activities.FacilityActivity;
import org.descartae.android.view.fragments.empty.RegionWaitListDialog;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;
import org.descartae.android.view.viewholder.FacilityViewHolder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacilitiesFragment extends Fragment implements ConnectionClassManager.ConnectionClassStateChangeListener, OnMapReadyCallback, OnSuccessListener<Location>, OnFailureListener {

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

    @BindView(R.id.region_unsupported)
    public View mRegionUnsupported;

    @BindView(R.id.filter_empty)
    public View mFilterEmpty;

    private FacilityViewHolder facilityViewHolder;

    private BottomSheetBehavior<View> behaviorDetail;
    private BottomSheetBehavior<View> behaviorList;
    private FacilitiesQuery.Item mItemSelected;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LocationCallback mLocationCallback;

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

                if (typesOfWasteTitle == null || typesOfWasteTitle.length <= 0) {
                    Log.d("Filter", "No Types");
                    return false;
                }

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.title_filter)
                        .items(typesOfWasteTitle)
                        .itemsCallbackMultiChoice(selectedTypesIndices, (MaterialDialog dialog, Integer[] which, CharSequence[] text) -> {

                            selectedTypesIndices = which;

                            List<String> selected = new ArrayList<>();
                            for (Integer index : which) {
                                selected.add(typesOfWasteData.get(index)._id());
                            }

                            facilityListAdapter.setCenters(null);
                            facilityListAdapter.notifyDataSetChanged();

                            mLoading.setVisibility(View.VISIBLE);

                            query(selected);

                            return true;
                        })
                        .positiveText(R.string.action_filter)
                        .show();

                return true;
        }
        return false;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLoading.setVisibility(View.VISIBLE);

        /**
         * Init Location Service: the facility query is called automatic after got the current location
         */
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this);
        mFusedLocationClient.getLastLocation().addOnFailureListener(this);

        /**
         * Load Type of Waste in order to be fetched before click on filter option
         */
        queryTypeOfWastes();
    }

    @Override
    public void onSuccess(Location location) {
        currentLocation = location;

        if (currentLocation != null) {
            afterGetLocation();
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                /**
                 * If fused location has not return the last location
                 */
                if (currentLocation == null) {
                    currentLocation = locationResult.getLastLocation();
                    afterGetLocation();
                }
            }

            ;
        };
        LocationRequest mRequestingLocationUpdates = new LocationRequest();
        mRequestingLocationUpdates.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mRequestingLocationUpdates.setInterval(60000);

        // Update Location Once
        mRequestingLocationUpdates.setNumUpdates(1);

        mFusedLocationClient.requestLocationUpdates(mRequestingLocationUpdates, mLocationCallback, null /* Looper */);
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

    private void afterGetLocation() {
        // Move Map
        moveMapCamera();

        // Start Test Connection Quality
        ConnectionClassManager.getInstance().register(FacilitiesFragment.this);
        DeviceBandwidthSampler.getInstance().startSampling();

        // Request nearby facilities
        query(null);

        // Stop Test Connection Quality
        DeviceBandwidthSampler.getInstance().stopSampling();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void query(List<String> filterTypesID) {

        Log.d("Facilities", "query");

        if (currentLocation == null) {
            Log.d("Facilities", "location not ready");
            return;
        }

        // Clear Map Pins
        if (mMap != null)
            mMap.clear();

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();

        FacilitiesQuery.Builder builder = FacilitiesQuery.builder();

        // IF location is loaded, fetch by near facilities
        Log.d("Query Facility", "Nearby: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        builder.latitude(currentLocation.getLatitude());
        builder.longitude(currentLocation.getLongitude());

        // IF wast pass type of waste filter
        if (filterTypesID != null) {
            builder.typesOfWasteToFilter(filterTypesID);
        }

        FacilitiesQuery facilityQuery = builder.build();

        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilitiesQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilitiesQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;
                if (getActivity() == null || getActivity().isDestroyed()) return;

                getActivity().runOnUiThread(() -> {
                    facilityListAdapter.setCenters(dataResponse.data().facilities().items());
                    facilityListAdapter.setCurrentLocation(currentLocation);
                    facilityListAdapter.notifyDataSetChanged();

                    if (facilityListAdapter.getItemCount() <= 0 && selectedTypesIndices.length > 0) {

                        /**
                         * If no facilities return with filter
                         */
                        mFilterEmpty.setVisibility(View.VISIBLE);

                    } else if (facilityListAdapter.getItemCount() <= 0 ) {

                        /**
                         * If no facilities return without filter
                         */
                        mRegionUnsupported.setVisibility(View.VISIBLE);
                    }

                    mLoading.setVisibility(View.GONE);

                    mMapFragment.getMapAsync(FacilitiesFragment.this);
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

        // Butterknife sucks for Fragment
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
        behaviorDetail.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        });
        behaviorDetail.setHideable(true);

        return view;
    }

    private void selectFacility(FacilitiesQuery.Item center) {

        // Fill Item Detail
        facilityViewHolder = new FacilityViewHolder(bottomSheetDetail);
        facilityViewHolder.mItem = center;
        facilityViewHolder.setCurrentLocation(currentLocation);
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

    @OnClick(R.id.action_go)
    public void onActionGo() {

        if (mItemSelected == null) return;

        String uri = "geo:" + mItemSelected.location().coordinates().latitude() + ","
                + mItemSelected.location().coordinates().longitude() + "?q=" + mItemSelected.location().coordinates().latitude()
                + "," + mItemSelected.location().coordinates().longitude();
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
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
        for (FacilitiesQuery.Item facility : centers) {
            if (facility.name().equals(name)) {
                mItemSelected = facility;
                selectFacility(facility);
                break;
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

        moveMapCamera();
    }

    private void moveMapCamera() {

        // Move camera
        if (currentLocation != null && mMap != null) {

            Log.d("Move map to: ", "Lat: " + currentLocation.getLatitude() + ", long:" + currentLocation.getLongitude());

            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
        }
    }

    @OnClick(R.id.action_clear_filter)
    public void onClearFilters() {
        selectedTypesIndices = new Integer[]{};

        mFilterEmpty.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);

        query(null);
    }

    @OnClick(R.id.action_notify_me)
    public void onNotifyMe() {
        RegionWaitListDialog.newInstance().show(getActivity().getSupportFragmentManager(), "DIALOG_WAIT_LIST");
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.e("Location Failure", e.getMessage());

        /**
         * If get last location failed, request the current one
         */
        requestLocation();
    }

    public interface OnListFacilitiesListener {
        void onNoConnection();
    }

    public interface OnFacilityListener {
        void onListFacilityInteraction(FacilitiesQuery.Item facility);
    }
}
