package org.descartae.android.indexing

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.rx2.Rx2Apollo
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Indexables
import org.descartae.android.FacilitiesQuery
import org.descartae.android.R
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.preferences.DescartaePreferences

private const val TAG_APOLLO_FACILITY_QUERY = "ApolloFacilityQuery"

class AppIndexingUpdateService : JobIntentService() {

  private val builder = FacilitiesQuery.builder()
  private var facilityQuery: FacilitiesQuery? = null
  private val facilitiesCall: ApolloQueryCall<FacilitiesQuery.Data>
    get() {
      val apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build()
      facilityQuery = builder.build()

      facilityQuery?.let { Log.d(TAG_APOLLO_FACILITY_QUERY, it.variables().valueMap().toString()) }

      return apolloClient.query(facilityQuery!!)
    }

  companion object {
    private lateinit var preferences: DescartaePreferences

    fun enqueueWork(context : Context) {
      preferences = DescartaePreferences(context)
      enqueueWork(context, AppIndexingUpdateService::class.java, 42, Intent())
    }
  }

  override fun onHandleWork(intent: Intent) {

    val latitude = preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LAT)
    val longitude = preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LNG)

    latitude?.let {
      builder.latitude(it)
    }
    longitude?.let {
      builder.longitude(it)
    }

    Rx2Apollo.from<FacilitiesQuery.Data>(facilitiesCall).subscribe { dataResponse ->

      val indexableFacility = ArrayList<Any>()

      dataResponse.data()?.facilities()?.items()?.let {
        for (facility in it) {

          val types = StringBuilder()
          for (type in facility.typesOfWaste()) {
            if (types.isNotEmpty()) {
              types.append(" / ")
            }
            types.append(type)
          }

          val facilityToIndex = Indexables.placeBuilder()
              .setName(facility.name())
              .setDescription(getString(R.string.indexing_description, types))
              .setUrl(getString(R.string.deeplink_uri, facility._id()))
              .build()

          indexableFacility.add(facilityToIndex)
        }
      }

      if (indexableFacility.size > 0) {
        var facilityArr = arrayOfNulls<Indexable>(indexableFacility.size)
        facilityArr = indexableFacility.toArray(facilityArr)
        val task = FirebaseAppIndex.getInstance().update(*facilityArr)
        task.addOnSuccessListener {
          Log.d(
              "App Indexing",
              "App Indexing API: Successfully added facilities to index."
          )
        }
        task.addOnFailureListener {
          Log.e(
              "App Indexing",
              "App Indexing API: Failed to add facilities to index. ${it.message}"
          )
        }
      }
    }
  }
}