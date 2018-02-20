package org.descartae.android.di.modules;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.descartae.android.DescartaeApp;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.preferences.DescartaePreferences;
import org.descartae.android.presenter.facility.FacilityListPresenter;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lucasmontano on 19/02/2018.
 */

@Module
public class AppModule {

    private final DescartaeApp app;

    public AppModule(DescartaeApp descartaeApp) {
        app = descartaeApp;
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return EventBus.builder().build();
    }

    @Provides
    FusedLocationProviderClient provideFusedLocationProviderClient() {
        return LocationServices.getFusedLocationProviderClient(app.getApplicationContext());
    }

    @Provides @Singleton
    DescartaePreferences provideDescartaePreferences() {
        return new DescartaePreferences(app.getApplicationContext());
    }

    @Provides
    ApolloApiErrorHandler provideApolloApiErrorHandler() {
        return new ApolloApiErrorHandler(provideEventBus());
    }

    @Provides
    FacilityListPresenter provideFacilityListPresenter(EventBus bus, DescartaePreferences preferences, ApolloApiErrorHandler apiErrorHandler, FusedLocationProviderClient fusedLocationClient) {
        return new FacilityListPresenter(bus, preferences, apiErrorHandler, fusedLocationClient);
    }
}