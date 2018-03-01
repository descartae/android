package org.descartae.android.presenter.feedback

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.rx2.Rx2Apollo
import org.descartae.android.AddFeedbackMutation
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val TAG_APOLLO_FEEDBACK_MUTATION = "TAG_FEEDBACK_MUTATION"

class FeedbackPresenter @Inject constructor(private var eventBus: EventBus, private var apiErrorHandler: ApolloApiErrorHandler) {

    private val builder = AddFeedbackMutation.builder()

    fun setFacilityId(facilityID: String?) {
        builder.facilityId(facilityID)
    }

    fun sendFeedback(feedback: String) {
        builder.feedback(feedback)

        Rx2Apollo.from<AddFeedbackMutation.Data>(getMutationCall()).subscribe({ dataResponse ->

            // Check and throw errors
            dataResponse.errors().forEach {
                apiErrorHandler.throwError(it)
            }

            // Check data and forward to event subscribers
            dataResponse.data().let {
                eventBus.post(it)
            }

        }) { throwable ->

            throwable.message?.let { it ->
                Log.e(TAG_APOLLO_FEEDBACK_MUTATION, it)
                if (it == "Failed to execute http call") eventBus.post(ConnectionError())
            }
        }
    }

    private fun getMutationCall(): ApolloMutationCall<AddFeedbackMutation.Data> {
        val apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build()
        return apolloClient.mutate(builder.build())
    }
}