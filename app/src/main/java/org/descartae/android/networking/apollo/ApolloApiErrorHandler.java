package org.descartae.android.networking.apollo;

import com.apollographql.apollo.api.Error;

import org.descartae.android.networking.apollo.errors.DuplicatedEmailError;
import org.descartae.android.networking.apollo.errors.GeneralError;
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lucasmontano on 14/02/2018.
 */
public class ApolloApiErrorHandler {

    private static String genericErrorMessage;

    public ApolloApiErrorHandler(Error error) {

        if (error == null || error.message().isEmpty()) EventBus.getDefault().post(new GeneralError(genericErrorMessage));
        else if (error.message().equals("DUPLICATED_EMAIL")) EventBus.getDefault().post(new DuplicatedEmailError());
        else if (error.message().equals("REGION_NOT_SUPPORTED")) EventBus.getDefault().post(new RegionNotSupportedError());
    }

    public ApolloApiErrorHandler(String message) {
        EventBus.getDefault().post(new GeneralError(message));
    }

    public static void setGenericErrorMessage(String genericErrorMessage) {
        ApolloApiErrorHandler.genericErrorMessage = genericErrorMessage;
    }
}
