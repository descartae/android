package org.descartae.android.presenter.facility;

import android.location.Location;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.apollo.errors.ConnectionError;
import org.descartae.android.preferences.DescartaePreferences;
import org.descartae.android.view.events.HideLoading;
import org.descartae.android.view.events.ShowLoading;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

/**
 * Created by lucasmontano on 19/02/2018.
 */
public class FacilityListPresenter {

    private static final String TAG_APOLLO_FACILITY_QUERY = "ApolloFacilityQuery";

    private final DescartaePreferences descartaePreferences;
    private final ApolloApiErrorHandler apiErrorHandler;
    private final EventBus eventBus;

    private final FacilitiesQuery.Builder builder = FacilitiesQuery.builder();

    @Inject public FacilityListPresenter(EventBus bus, DescartaePreferences preferences, ApolloApiErrorHandler apiErrorHandler) {
        this.descartaePreferences = preferences;
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;
    }

    public void setCurrentLocation(Location currentLocation) {
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
    }

    public void setFilterTypesID(List<String> filterTypesID) {
        builder.typesOfWasteToFilter(filterTypesID);
    }

    public void observeFacilities() {

        eventBus.post(new ShowLoading());

        Rx2Apollo.from(getFacilitiesCall()).subscribe(dataResponse -> {

            eventBus.post(new HideLoading());

            // Check and throw errors
            if (dataResponse.hasErrors())
                for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

                // If no Errors
            else if (dataResponse.data() != null) {
                FacilitiesQuery.Facilities facilities = dataResponse.data().facilities();
                eventBus.post(facilities);
            }

        }, throwable -> {

            if (throwable != null && throwable.getMessage() != null) Log.e(TAG_APOLLO_FACILITY_QUERY, throwable.getMessage());

            // Check Connectivity
            ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
            if (cq.equals(ConnectionQuality.UNKNOWN)) eventBus.post(new ConnectionError());
        });
    }

    private ApolloQueryCall<FacilitiesQuery.Data> getFacilitiesCall() {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build();
        FacilitiesQuery facilityQuery = builder.build();
        return apolloClient.query(facilityQuery);
    }
}
