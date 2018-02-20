package org.descartae.android.presenter.feedback;

import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import org.descartae.android.AddFeedbackMutation;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */
public class FeedbackPresenter {

    private static final String TAG_APOLLO_FEEDBACK_MUTATION = "AddFeedbackMutation";
    private final AddFeedbackMutation.Builder builder = AddFeedbackMutation.builder();

    private final EventBus eventBus;
    private final ApolloApiErrorHandler apiErrorHandler;

    @Inject public FeedbackPresenter(EventBus bus, ApolloApiErrorHandler apiErrorHandler) {
        this.eventBus = bus;
        this.apiErrorHandler = apiErrorHandler;
    }

    public void setFacilityId(String facilityID) {
        builder.facilityId(facilityID);
    }

    public void sendFeedback(String feedback) {
        builder.feedback(feedback);

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

    private ApolloMutationCall<AddFeedbackMutation.Data> getMutationCall() {
        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();
        return apolloClient.mutate(builder.build());
    }

}
