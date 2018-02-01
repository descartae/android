package org.descartae.android.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.adapters.OpenHourListAdapter;
import org.descartae.android.adapters.WastesTypeListAdapter;
import org.descartae.android.interfaces.RetryConnectionView;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.type.CustomType;
import org.descartae.android.view.fragments.empty.EmptyOfflineFragment;
import org.descartae.android.view.fragments.facility.FeedbackDialog;
import org.descartae.android.view.fragments.wastes.WasteTypeDialog;

import java.util.Calendar;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacilityActivity extends AppCompatActivity implements OnMapReadyCallback, OnSuccessListener<Location>,WastesTypeListAdapter.TypeOfWasteListner, RetryConnectionView {

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
    public RecyclerView mTypesWasteRecyclerView;

    @BindView(R.id.time_expand)
    public ImageView mTimeExpand;

    @BindView(R.id.more_times)
    public RecyclerView mMoreTimes;

    @BindView(R.id.distance)
    public TextView mDistance;

    @BindView(R.id.no_connection)
    public View mContent;

    @BindView(R.id.loading)
    public View mLoading;

    public MapFragment mMapFragment;

    private String itemID;

    private FacilityQuery.Facility facility;

    private FusedLocationProviderClient mFusedLocationClient;

    private WastesTypeListAdapter mTypesWasteAdapter;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

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

        mLoading.setVisibility(View.VISIBLE);

        // Type of Waste
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mTypesWasteRecyclerView.setLayoutManager(layoutManager);

        mTypesWasteAdapter = new WastesTypeListAdapter(this, this);
        mTypesWasteRecyclerView.setAdapter(mTypesWasteAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_spacing));
        mTypesWasteRecyclerView.addItemDecoration(dividerItemDecoration);

        // Butterknife sucks for Fragment
        mMapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.map, mMapFragment).commitAllowingStateLoss();

        itemID = getIntent().getStringExtra(ARG_ID);
        query(itemID);
    }

    private void query(String id) {

        CustomTypeAdapter<String> customTypeAdapter = new CustomTypeAdapter<String>() {
            @Override
            public String decode(String value) {
               return value;
            }

            @Override
            public String encode(String value) {
                return value;
            }
        };

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .addCustomTypeAdapter(CustomType.TIME, customTypeAdapter)
                .build();
        FacilityQuery facilityQuery = FacilityQuery.builder().id(id).build();
        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilityQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilityQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                runOnUiThread(() -> {

                    facility = dataResponse.data().facility();

                    mLocationView.setText(facility.location().address());
                    mNameView.setText(facility.name());
                    mPhone.setText(facility.telephone());

                    LinearLayoutManager llm = new LinearLayoutManager(FacilityActivity.this);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    mMoreTimes.setLayoutManager(llm);

                    OpenHourListAdapter timeListAdapter = new OpenHourListAdapter(FacilityActivity.this);
                    timeListAdapter.setFacilityDays(facility.openHours());
                    mMoreTimes.setAdapter(timeListAdapter);
                    timeListAdapter.notifyDataSetChanged();

                    String time = null;
                    for (FacilityQuery.OpenHour openHour : facility.openHours()) {
                        if (openHour.dayOfWeek().ordinal() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                            time = getString(R.string.time, "Hoje", getString(R.string.time_desc, String.valueOf(openHour.startTime()), String.valueOf(openHour.endTime())));
                            mTime.setText(time);
                            break;
                        }
                    }

                    if (time == null) {
                        mTime.setText(R.string.no_open_hour_today);
                    }

                    mMapFragment.getMapAsync(FacilityActivity.this);

                    mTypesWasteAdapter.setTypes(facility.typesOfWaste());
                    mTypesWasteAdapter.notifyDataSetChanged();

                    mTimeExpand.setOnClickListener(view -> {

                        if (mMoreTimes.getVisibility() == View.VISIBLE) {
                            Picasso.with(FacilityActivity.this).load(R.drawable.ic_action_expand_more).into(mTimeExpand);
                            mMoreTimes.setVisibility(View.GONE);
                        } else {
                            Picasso.with(FacilityActivity.this).load(R.drawable.ic_action_expand_less).into(mTimeExpand);
                            mMoreTimes.setVisibility(View.VISIBLE);
                        }
                    });

                    mLoading.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());

                runOnUiThread(() -> {
                    mLoading.setVisibility(View.GONE);

                    ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                    if (cq.equals(ConnectionQuality.UNKNOWN)) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.no_connection, EmptyOfflineFragment.newInstance()).commitAllowingStateLoss();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_facility, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {

            if (facility == null) return false;

            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setChooserTitle(R.string.menu_share)
                    .setText(getString(R.string.website_url, facility._id()))
                    .startChooser();

            return true;
        }
        if (id == R.id.action_feedback) {
            FeedbackDialog.newInstance(facility._id()).show(getSupportFragmentManager(), "DIALOG_FEEDBACK");
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap = googleMap;

        if (facility == null) return;

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this);

        LatLng latlng = new LatLng(
            facility.location().coordinates().latitude(),
            facility.location().coordinates().longitude()
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
        mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                        .position(latlng)
                        .title(facility.name()));
    }

    @Override
    public void onSuccess(Location location) {
        if (facility == null || location == null) return;

        Location facilityLocation = new Location("Facility");
        facilityLocation.setLatitude(facility.location().coordinates().latitude());
        facilityLocation.setLongitude(facility.location().coordinates().longitude());

        float distance = location.distanceTo(facilityLocation);
        mDistance.setText(getString(R.string.distance, distance / 1000));
    }

    @Override
    public void onTypeClick(FacilityQuery.TypesOfWaste typesOfWaste) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = WasteTypeDialog.newInstance(typesOfWaste.name(), typesOfWaste.description(), typesOfWaste.icons().androidMediumURL());
        newFragment.show(ft, "dialog");
    }

    @OnClick(R.id.fab)
    public void onNavClick() {

        if (facility == null) return;

        String uri = "geo:" + facility.location().coordinates().latitude() + ","
                + facility.location().coordinates().longitude() + "?q=" + facility.location().coordinates().latitude()
                + "," + facility.location().coordinates().longitude();
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    @Override
    public void onRetryConnection() {
        query(itemID);
    }
}
