package org.descartae.android.presenter.waitlist

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.rx2.Rx2Apollo
import com.facebook.network.connectionclass.DeviceBandwidthSampler
import org.descartae.android.AddToWaitlistMutation
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val TAG_ADD_TO_WAITLIST_MUTATION = "AddToWaitlistMutation"

class WaitListPresenter @Inject constructor(private val eventBus: EventBus, private val apiErrorHandler: ApolloApiErrorHandler) {

    private val builder = AddToWaitlistMutation.builder()

    private val mutationCall: ApolloMutationCall<AddToWaitlistMutation.Data> get() {
        val apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build()
        return apolloClient.mutate(builder.build())
    }

    fun setLatLng(longitude: Double, latitude: Double) {
        builder.longitude(longitude)
        builder.latitude(latitude)
    }

    fun addToWaitList(email: String) {
        builder.email(email)

        eventBus.post(EventShowLoading())

        Rx2Apollo.from<AddToWaitlistMutation.Data>(mutationCall).subscribe({ dataResponse ->

            eventBus.post(EventHideLoading())

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling()

            // Check and throw errors
            dataResponse.errors().forEach {
                apiErrorHandler.throwError(it)
            }

            // Check data and forward to event subscribers
            dataResponse.data()?.let {
                eventBus.post(it)
            }

        }) { throwable ->

            eventBus.post(EventHideLoading())

            throwable.message?.let { it ->
                Log.e(TAG_ADD_TO_WAITLIST_MUTATION, it)
                if (it == "Failed to execute http call") eventBus.post(ConnectionError())
            }
        }
    }
}
