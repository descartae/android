package org.descartae.android.presenter.facility;

import android.location.Location;
import android.util.Log;
import android.view.View;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;

import org.descartae.android.FacilitiesQuery;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.apollo.errors.ConnectionError;
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError;
import org.descartae.android.preferences.DescartaePreferences;
import org.descartae.android.view.fragments.facility.FacilitiesFragment;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Created by lucasmontano on 19/02/2018.
 */
public class FacilityListPresenter {

    private final DescartaePreferences descartaePreferences;
    private final EventBus eventBus;

    private Location currentLocation;
    private List<String> filterTypesID;

    @Inject public FacilityListPresenter(EventBus bus, DescartaePreferences preferences) {
        this.descartaePreferences = preferences;
        this.eventBus = bus;
    }

    public void loadFacilities() {

        if (currentLocation == null) {
            Log.d("FacilitiesQuery", "Location not available");
            return;
        }

        // Save Last Location Queried
        descartaePreferences.setValue(
                DescartaePreferences.PREF_LAST_LOCATION_LAT,
                currentLocation.getLatitude()
        );
        descartaePreferences.setValue(
                DescartaePreferences.PREF_LAST_LOCATION_LNG,
                currentLocation.getLongitude())
        ;

        // Clear Map Pins
        /**
         * @TODO CLEAR MAP
         * */

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

                /** @TODO hide LOAD */

                if (dataResponse.hasErrors()) {
                    for (Error error : dataResponse.errors()) new ApolloApiErrorHandler(error);
                } else {

                    FacilitiesQuery.Facilities facilities = dataResponse.data().facilities();
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());

                /** @TODO hide LOAD */

                ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                if (cq.equals(ConnectionQuality.UNKNOWN)) {
                    eventBus.post(new ConnectionError());
                }
            }
        });
    }
}
