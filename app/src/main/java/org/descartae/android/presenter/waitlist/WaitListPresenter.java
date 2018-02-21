package org.descartae.android.presenter.waitlist;

import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import org.descartae.android.AddToWaitlistMutation;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */
public class WaitListPresenter {

    private static final String TAG_ADD_TO_WAITLIST_MUTATION = "AddToWaitlistMutation";
    private final AddToWaitlistMutation.Builder builder = AddToWaitlistMutation.builder();

    private final EventBus eventBus;
    private final ApolloApiErrorHandler apiErrorHandler;

    @Inject public WaitListPresenter(EventBus bus, ApolloApiErrorHandler apiErrorHandler) {
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;
    }

    public void setLatLng(double longitude, double latitude) {
        builder.longitude(longitude);
        builder.latitude(latitude);
    }

    public void addToWaitList(String email) {
        builder.email(email);

        eventBus.post(new EventShowLoading());

        Rx2Apollo.from(getMutationCall()).subscribe(dataResponse -> {

            eventBus.post(new EventHideLoading());

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling();

            // Check and throw errors
            if (dataResponse.hasErrors())
                for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

            // If no Errors
            else if (dataResponse.data() != null) eventBus.post(dataResponse.data());

        }, throwable -> {

            eventBus.post(new EventHideLoading());

            if (throwable != null && throwable.getMessage() != null) Log.e(TAG_ADD_TO_WAITLIST_MUTATION, throwable.getMessage());
        });
    }

    private ApolloMutationCall<AddToWaitlistMutation.Data> getMutationCall() {
        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();
        return apolloClient.mutate(builder.build());
    }
}
