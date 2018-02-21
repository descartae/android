package org.descartae.android.presenter.facility;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.descartae.android.FacilityQuery;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.apollo.errors.ConnectionError;
import org.descartae.android.presenter.BaseLocationPresenter;
import org.descartae.android.type.CustomType;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */
public class FacilityPresenter extends BaseLocationPresenter implements ConnectionClassManager.ConnectionClassStateChangeListener {

    private static final String TAG_APOLLO_FACILITY_QUERY = "FacilityQuery";

    private final EventBus eventBus;
    private final ApolloApiErrorHandler apiErrorHandler;

    private final FacilityQuery.Builder builder = FacilityQuery.builder();

    @Inject
    public FacilityPresenter(EventBus bus, ApolloApiErrorHandler apiErrorHandler, FusedLocationProviderClient fusedLocationClient) {
        super(fusedLocationClient);
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;

        ConnectionClassManager.getInstance().register(this);
    }

    @Override
    protected void updateCurrentLocation(Location currentLocation) {
        eventBus.post(currentLocation);
    }

    public void setFacilityId(String itemId) {
        builder.id(itemId);
    }

    public void requestFacility() {

        eventBus.post(new EventShowLoading());

        // Start Test Connection Quality
        DeviceBandwidthSampler.getInstance().startSampling();

        Rx2Apollo.from(getRequestCall()).subscribe(dataResponse -> {

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling();

            eventBus.post(new EventHideLoading());

            // Check and throw errors
            if (dataResponse.hasErrors())
                for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

            // If no Errors
            else if (dataResponse.data() != null) eventBus.post(dataResponse.data().facility());

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
        });
    }

    private ApolloQueryCall<FacilityQuery.Data> getRequestCall() {

        CustomTypeAdapter<String> customTypeAdapter = new CustomTypeAdapter<String>() {
            @NonNull
            @Override
            public String decode(@NonNull String value) {
                return value;
            }

            @NonNull
            @Override
            public String encode(@NonNull String value) {
                return value;
            }
        };

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .addCustomTypeAdapter(CustomType.TIME, customTypeAdapter)
                .build();

        return apolloClient.query(builder.build());
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        if (bandwidthState.equals(ConnectionQuality.UNKNOWN)) eventBus.post(new ConnectionError());
    }
}
