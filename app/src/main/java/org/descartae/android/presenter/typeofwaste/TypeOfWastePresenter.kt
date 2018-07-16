package org.descartae.android.presenter.typeofwaste

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.rx2.Rx2Apollo
import org.descartae.android.TypeOfWasteQuery
import org.descartae.android.networking.NetworkingConstants
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val TAG_APOLLO_TYPE_QUERY = "TypeOfWasteQuery"

class TypeOfWastePresenter @Inject constructor(private val eventBus: EventBus,
  private val apiErrorHandler: ApolloApiErrorHandler) {

  private val builder = TypeOfWasteQuery.builder()
  private var typesOfWasteData: List<TypeOfWasteQuery.TypesOfWaste>? = null
  var typesOfWasteTitle: Array<String>? = null
  private var triggerLoadingEvents: Boolean = false

  private val requestCall: ApolloQueryCall<TypeOfWasteQuery.Data>
    get() {
      val apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build()
      return apolloClient.query(builder.build())
    }

  fun isTypesLoaded(): Boolean {
    return typesOfWasteData != null && typesOfWasteData!!.isNotEmpty()
  }

  fun setTriggerLoadingEvents(triggerLoadingEvents: Boolean) {
    this.triggerLoadingEvents = triggerLoadingEvents
  }

  fun requestTypeOfWastes() {

    if (triggerLoadingEvents) eventBus.post(EventShowLoading())

    Rx2Apollo.from<TypeOfWasteQuery.Data>(requestCall).subscribe({ dataResponse ->

      // Check and throw errors
      dataResponse.errors().forEach {
        apiErrorHandler.throwError(it)
      }

      // Check data and forward to event subscribers
      dataResponse.data()?.let {

        typesOfWasteData = it.typesOfWaste()

        typesOfWasteData.let {
          typesOfWasteTitle = Array(it!!.size, { i -> it[i].name() })
        }

        eventBus.post(typesOfWasteData)
      }

      if (triggerLoadingEvents) eventBus.post(EventHideLoading())

    }) { throwable ->

      throwable.message?.let { it ->
        Log.e(TAG_APOLLO_TYPE_QUERY, it)
        if (it == "Failed to execute http call") eventBus.post(ConnectionError())
      }
    }
  }

  fun getTypeId(index: Int): String? {
    return typesOfWasteData?.get(index)?._id()
  }
}
