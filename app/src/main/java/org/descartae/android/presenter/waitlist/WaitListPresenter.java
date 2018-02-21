package org.descartae.android.presenter.waitlist;

import android.location.Location;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.descartae.android.AddToWaitlistMutation;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.presenter.BaseLocationPresenter;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */
public class WaitListPresenter extends BaseLocationPresenter {

    private static final String TAG_APOLLO_FEEDBACK_MUTATION = "AddFeedbackMutation";
    private final AddToWaitlistMutation.Builder builder = AddToWaitlistMutation.builder();

    private final EventBus eventBus;
    private final ApolloApiErrorHandler apiErrorHandler;

    @Inject public WaitListPresenter(EventBus bus, ApolloApiErrorHandler apiErrorHandler, FusedLocationProviderClient fusedLocationClient) {
        super(fusedLocationClient);
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;
    }

    public void addToWaitList(String email) {
        builder.email(email);

        Rx2Apollo.from(getMutationCall()).subscribe(dataResponse -> {

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling();

            // Check and throw errors
            if (dataResponse.hasErrors())
                for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

            // If no Errors
            else if (dataResponse.data() != null) eventBus.post(dataResponse.data());

        }, throwable -> {
            if (throwable != null && throwable.getMessage() != null) Log.e(TAG_APOLLO_FEEDBACK_MUTATION, throwable.getMessage());
        });
    }

    private ApolloMutationCall<AddToWaitlistMutation.Data> getMutationCall() {
        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();
        return apolloClient.mutate(builder.build());
    }

    @Override
    protected void updateCurrentLocation(Location currentLocation) {
        builder.longitude(currentLocation.getLongitude());
        builder.latitude(currentLocation.getLatitude());
    }
}
