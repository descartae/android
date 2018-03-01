package org.descartae.android.presenter.facility

import android.location.Location
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.rx2.Rx2Apollo
import com.facebook.network.connectionclass.ConnectionClassManager
import com.facebook.network.connectionclass.ConnectionQuality
import com.facebook.network.connectionclass.DeviceBandwidthSampler
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.disposables.Disposable
import org.descartae.android.FacilitiesQuery
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.descartae.android.preferences.DescartaePreferences
import org.descartae.android.presenter.BaseLocationPresenter
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val TAG_APOLLO_FACILITY_QUERY = "ApolloFacilityQuery"

class FacilityListPresenter @Inject constructor(private val eventBus: EventBus, private val descartaePreferences: DescartaePreferences, private val apiErrorHandler: ApolloApiErrorHandler, fusedLocationClient: FusedLocationProviderClient) : BaseLocationPresenter(fusedLocationClient), ConnectionClassManager.ConnectionClassStateChangeListener {

    private val builder = FacilitiesQuery.builder()
    private var facilityQuery: FacilitiesQuery? = null
    private var disposable: Disposable? = null

    private val facilitiesCall: ApolloQueryCall<FacilitiesQuery.Data> get() {
        val apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build()
        facilityQuery = builder.build()
        return apolloClient.query(facilityQuery!!)
    }

    init {
        eventBus.post(EventShowLoading())
        ConnectionClassManager.getInstance().register(this)
    }

    override fun updateCurrentLocation(currentLocation: Location) {
        builder.latitude(currentLocation.latitude)
        builder.longitude(currentLocation.longitude)

        // Save Last Location Queried
        descartaePreferences.setValue(
                DescartaePreferences.PREF_LAST_LOCATION_LAT,
                currentLocation.latitude)

        descartaePreferences.setValue(
                DescartaePreferences.PREF_LAST_LOCATION_LNG,
                currentLocation.longitude)

        Log.d(TAG_APOLLO_FACILITY_QUERY, "Nearby: " + currentLocation.latitude + ", " + currentLocation.longitude)

        requestFacilities()
    }

    fun setFilterTypesID(filterTypesID: List<String>) {
        builder.hasTypesOfWaste(filterTypesID)
    }

    fun requestFacilities() {

        if (disposable == null || disposable!!.isDisposed) {

            eventBus.post(EventShowLoading())

            // Start Test Connection Quality
            DeviceBandwidthSampler.getInstance().startSampling()

            disposable = Rx2Apollo.from<FacilitiesQuery.Data>(facilitiesCall).subscribe({ dataResponse ->

                // Stop Test Connection Quality
                DeviceBandwidthSampler.getInstance().stopSampling()

                eventBus.post(EventHideLoading())

                // Check and throw errors
                dataResponse.errors().forEach {
                    apiErrorHandler.throwError(it)
                }

                // Check data and send facilities to be render
                dataResponse.data()?.let {
                    eventBus.post(it.facilities())
                }

                disposable!!.dispose()

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

                disposable!!.dispose()
            }
        }
    }

    fun haveCurrentLocation(): Boolean {
        return currentLocation != null
    }

    override fun onBandwidthStateChange(bandwidthState: ConnectionQuality) {
        if (bandwidthState == ConnectionQuality.UNKNOWN) eventBus.post(ConnectionError())
    }

    fun hasFilterType(): Boolean {

        if (facilityQuery!!.variables() != null)
            if (facilityQuery!!.variables().hasTypesOfWaste() != null)
                if (facilityQuery!!.variables().hasTypesOfWaste()!!.size > 0) return true

        return false
    }
}
