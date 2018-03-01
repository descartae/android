package org.descartae.android.networking.apollo

import com.apollographql.apollo.api.Error
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class ApolloApiErrorHandler @Inject constructor(private val bus: EventBus) {

    companion object {
        var genericErrorMessage: String? = null
    }

    fun throwError(error: Error?) {

        if (error == null || error.message()!!.isEmpty())  bus.post(GeneralError(genericErrorMessage))

        error?.let {
            when (it.message()) {
                "DUPLICATED_EMAIL" -> bus.post(DuplicatedEmailError())
                "REGION_NOT_SUPPORTED" -> bus.post(RegionNotSupportedError())
            }
        }
    }

    fun throwError(error: String?) {
        bus.post(GeneralError(error))
    }
}
