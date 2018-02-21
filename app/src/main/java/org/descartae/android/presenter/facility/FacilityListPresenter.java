package org.descartae.android.presenter.facility;

import android.location.Location;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.apollo.errors.ConnectionError;
import org.descartae.android.preferences.DescartaePreferences;
import org.descartae.android.presenter.BaseLocationPresenter;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

/**
 * Created by lucasmontano on 19/02/2018.
 */
public class FacilityListPresenter extends BaseLocationPresenter implements ConnectionClassManager.ConnectionClassStateChangeListener {

    private static final String TAG_APOLLO_FACILITY_QUERY = "ApolloFacilityQuery";

    private final DescartaePreferences descartaePreferences;
    private final ApolloApiErrorHandler apiErrorHandler;
    private EventBus eventBus;

    private final FacilitiesQuery.Builder builder = FacilitiesQuery.builder();
    private FacilitiesQuery facilityQuery;
    private Disposable disposable;

    @Inject public FacilityListPresenter(EventBus bus, DescartaePreferences preferences, ApolloApiErrorHandler apiErrorHandler, FusedLocationProviderClient fusedLocationClient) {
        super(fusedLocationClient);
        this.descartaePreferences = preferences;
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;

        eventBus.post(new EventShowLoading());

        ConnectionClassManager.getInstance().register(this);
    }

    protected void updateCurrentLocation(Location currentLocation) {

        if (currentLocation == null) {

            // Check Connectivity
            ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
            if (cq.equals(ConnectionQuality.UNKNOWN)) eventBus.post(new ConnectionError());

            return;
        }

        builder.latitude(currentLocation.getLatitude());
        builder.longitude(currentLocation.getLongitude());

        // Save Last Location Queried
        descartaePreferences.setValue(
            DescartaePreferences.PREF_LAST_LOCATION_LAT,
            currentLocation.getLatitude());

        descartaePreferences.setValue(
            DescartaePreferences.PREF_LAST_LOCATION_LNG,
            currentLocation.getLongitude());

        Log.d(TAG_APOLLO_FACILITY_QUERY, "Nearby: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        requestFacilities();
    }

    public void setFilterTypesID(List<String> filterTypesID) {
        builder.hasTypesOfWaste(filterTypesID);
    }

    public void requestFacilities() {

        eventBus.post(new EventShowLoading());

        // Start Test Connection Quality
        DeviceBandwidthSampler.getInstance().startSampling();

        if (disposable == null || disposable.isDisposed()) {

            disposable = Rx2Apollo.from(getFacilitiesCall()).subscribe(dataResponse -> {

                // Stop Test Connection Quality
                DeviceBandwidthSampler.getInstance().stopSampling();

                eventBus.post(new EventHideLoading());

                // Check and throw errors
                if (dataResponse.hasErrors())
                    for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

                    // If no Errors
                else if (dataResponse.data() != null)
                    eventBus.post(dataResponse.data().facilities());

                disposable.dispose();

            }, throwable -> {

                // Stop Test Connection Quality
                DeviceBandwidthSampler.getInstance().stopSampling();
                ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();

                eventBus.post(new EventHideLoading());

                String errorMessage = throwable.getMessage();
                if (throwable != null && errorMessage != null) {
                    Log.e(TAG_APOLLO_FACILITY_QUERY, errorMessage);

                    if (errorMessage.equals("Failed to execute http call")) eventBus.post(new ConnectionError());
                    else if (cq.equals(ConnectionQuality.UNKNOWN)) eventBus.post(new ConnectionError());
                }

                disposable.dispose();
            });
        }
    }

    private ApolloQueryCall<FacilitiesQuery.Data> getFacilitiesCall() {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build();
        facilityQuery = builder.build();
        return apolloClient.query(facilityQuery);
    }

    public boolean haveCurrentLocation() {
        return getCurrentLocation() != null;
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        if (bandwidthState.equals(ConnectionQuality.UNKNOWN)) eventBus.post(new ConnectionError());
    }

    public boolean hasFilterType() {

        if (facilityQuery.variables() != null)
            if (facilityQuery.variables().hasTypesOfWaste() != null)
                if (facilityQuery.variables().hasTypesOfWaste().size() > 0) return true;

        return false;
    }
}
