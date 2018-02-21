package org.descartae.android.networking.apollo;

import com.apollographql.apollo.api.Error;

import org.descartae.android.networking.apollo.errors.DuplicatedEmailError;
import org.descartae.android.networking.apollo.errors.GeneralError;
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 14/02/2018.
 */
public final class ApolloApiErrorHandler {

    private EventBus bus;

    private static String genericErrorMessage;

    @Inject public ApolloApiErrorHandler(EventBus bus) {
        this.bus = bus;
    }

    public void throwError(Error error) {

        if (error == null || error.message().isEmpty()) bus.post(new GeneralError(genericErrorMessage));
        else if (error.message().equals("DUPLICATED_EMAIL")) bus.post(new DuplicatedEmailError());
        else if (error.message().equals("REGION_NOT_SUPPORTED")) bus.post(new RegionNotSupportedError());
    }

    public ApolloApiErrorHandler(String message) {
        bus.post(new GeneralError(message));
    }

    public static void setGenericErrorMessage(String genericErrorMessage) {
        ApolloApiErrorHandler.genericErrorMessage = genericErrorMessage;
    }
}
