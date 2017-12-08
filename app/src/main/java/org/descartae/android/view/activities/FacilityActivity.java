package org.descartae.android.view.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.view.fragments.empty.EmptyOfflineFragment;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacilityActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String ARG_ID = "ITEM";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.time)
    public TextView mTime;

    @BindView(R.id.phone)
    public TextView mPhone;

    @BindView(R.id.location)
    public TextView mLocationView;

    @BindView(R.id.name)
    public TextView mNameView;

    @BindView(R.id.type_waste)
    public LinearLayout mTypes;

    public MapFragment mMapFragment;

    private String itemID;

    private FacilityQuery.Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility);

        if (getIntent() == null) {
            finish();
            return;
        }

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Butterknife sucks for Fragment
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        itemID = getIntent().getStringExtra(ARG_ID);
        query(itemID);
    }

    private void query(String id) {

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();
        FacilityQuery facilityQuery = FacilityQuery.builder().id(id).build();
        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilityQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilityQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                runOnUiThread(() -> {

                    facility = dataResponse.data().facility();

                    mLocationView.setText(facility.location().municipality());
                    mNameView.setText(facility.name());
                    mPhone.setText(facility.telephone());

                    mMapFragment.getMapAsync(FacilityActivity.this);
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());

                ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                if (cq.equals(ConnectionQuality.UNKNOWN)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, EmptyOfflineFragment.newInstance()).commitAllowingStateLoss();
                }
            }
        });
    }

    @OnClick(R.id.fab)
    public void onFab() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (facility == null) return;

        googleMap.addMarker(
            new MarkerOptions()
                .position(new LatLng(30.000, 20.000))
                .title(facility.name()));
    }
}
