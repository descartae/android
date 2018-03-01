package org.descartae.android.presenter.facility

import android.location.Location
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.CustomTypeAdapter
import com.apollographql.apollo.rx2.Rx2Apollo
import com.facebook.network.connectionclass.ConnectionClassManager
import com.facebook.network.connectionclass.ConnectionQuality
import com.facebook.network.connectionclass.DeviceBandwidthSampler
import com.google.android.gms.location.FusedLocationProviderClient
import org.descartae.android.FacilityQuery
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.descartae.android.presenter.BaseLocationPresenter
import org.descartae.android.type.CustomType
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val TAG_APOLLO_FACILITY_QUERY = "FacilityQuery"

class FacilityPresenter @Inject constructor(
        private val eventBus: EventBus,
        private val apiErrorHandler: ApolloApiErrorHandler, fusedLocationClient: FusedLocationProviderClient
    ) : BaseLocationPresenter(fusedLocationClient), ConnectionClassManager.ConnectionClassStateChangeListener {

    private val builder = FacilityQuery.builder()

    private val requestCall: ApolloQueryCall<FacilityQuery.Data>
        get() {

            val customTypeAdapter = object : CustomTypeAdapter<String> {
                override fun decode(value: String): String {
                    return value
                }

                override fun encode(value: String): String {
                    return value
                }
            }

            val apolloClient = ApolloClient.builder()
                    .serverUrl(NetworkingConstants.BASE_URL)
                    .addCustomTypeAdapter(CustomType.TIME, customTypeAdapter)
                    .build()

            return apolloClient.query(builder.build())
        }

    init {
        ConnectionClassManager.getInstance().register(this)
    }

    override fun updateCurrentLocation(currentLocation: Location) {
        eventBus.post(currentLocation)
    }

    fun setFacilityId(itemId: String) {
        builder.id(itemId)
    }

    fun requestFacility() {

        eventBus.post(EventShowLoading())

        // Start Test Connection Quality
        DeviceBandwidthSampler.getInstance().startSampling()

        Rx2Apollo.from<FacilityQuery.Data>(requestCall).subscribe({ dataResponse ->

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling()

            eventBus.post(EventHideLoading())

            // Check and throw errors
            dataResponse.errors().forEach {
                apiErrorHandler.throwError(it)
            }

            // Check data and send facility to be render in FacilityActivity
            dataResponse.data()?.let {
                eventBus.post(it.facility())
            }

        }) { throwable ->

            // Stop Test Connection Quality
            DeviceBandwidthSampler.getInstance().stopSampling()
            val cq = ConnectionClassManager.getInstance().currentBandwidthQuality

            eventBus.post(EventHideLoading())

            throwable.message?.let { it ->
                Log.e(TAG_APOLLO_FACILITY_QUERY, it)
                if (it == "Failed to execute http call") eventBus.post(ConnectionError())
                else if (cq == ConnectionQuality.UNKNOWN) eventBus.post(ConnectionError())
            }
        }
    }

    override fun onBandwidthStateChange(bandwidthState: ConnectionQuality) {
        if (bandwidthState == ConnectionQuality.UNKNOWN) eventBus.post(ConnectionError())
    }
}
