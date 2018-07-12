package org.descartae.android.di.modules

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import org.descartae.android.DescartaeApp
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.preferences.DescartaePreferences
import org.descartae.android.presenter.facility.FacilityListPresenter
import org.descartae.android.presenter.feedback.FeedbackPresenter
import org.descartae.android.presenter.typeofwaste.TypeOfWastePresenter
import org.descartae.android.presenter.waitlist.WaitListPresenter
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
class AppModule(private val app: DescartaeApp) {

  @Provides
  @Singleton
  fun provideApplication() = app

  @Provides
  @Singleton
  fun provideEventBus(): EventBus {
    return app.eventBus!!
  }

  @Provides
  fun provideFusedLocationProviderClient(): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(app.applicationContext)
  }

  @Provides
  @Singleton
  fun provideDescartaePreferences(): DescartaePreferences {
    return DescartaePreferences(app.applicationContext)
  }

  @Provides
  fun provideApolloApiErrorHandler(): ApolloApiErrorHandler {
    return ApolloApiErrorHandler(provideEventBus())
  }

  @Provides
  fun provideFacilityListPresenter(bus: EventBus, preferences: DescartaePreferences,
    apiErrorHandler: ApolloApiErrorHandler,
    fusedLocationClient: FusedLocationProviderClient): FacilityListPresenter {
    return FacilityListPresenter(bus, preferences, apiErrorHandler, fusedLocationClient)
  }

  @Provides
  fun provideTypeOfWastePresenter(bus: EventBus,
    apiErrorHandler: ApolloApiErrorHandler): TypeOfWastePresenter {
    return TypeOfWastePresenter(bus, apiErrorHandler)
  }

  @Provides
  fun provideFeedbackPresenter(bus: EventBus,
    apiErrorHandler: ApolloApiErrorHandler): FeedbackPresenter {
    return FeedbackPresenter(bus, apiErrorHandler)
  }

  @Provides
  fun provideRegionUnsupportedPresenter(bus: EventBus,
    apiErrorHandler: ApolloApiErrorHandler): WaitListPresenter {
    return WaitListPresenter(bus, apiErrorHandler)
  }
}