package org.descartae.android.presenter.typeofwaste;

import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.rx2.Rx2Apollo;

import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */

public class TypeOfWastePresenter {

    private static final String TAG_APOLLO_TYPE_QUERY = "TypeOfWasteQuery";

    private final EventBus eventBus;

    private final TypeOfWasteQuery.Builder builder = TypeOfWasteQuery.builder();
    private final ApolloApiErrorHandler apiErrorHandler;
    private List<TypeOfWasteQuery.TypesOfWaste> typesOfWasteData;
    private String[] typesOfWasteTitle;

    @Inject public TypeOfWastePresenter(EventBus eventBus, ApolloApiErrorHandler apiErrorHandler) {
        this.eventBus = eventBus;
        this.apiErrorHandler = apiErrorHandler;
    }

    public void requestTypeOfWastes() {

        Rx2Apollo.from(getRequestCall()).subscribe(dataResponse -> {

            // Check and throw errors
            if (dataResponse.hasErrors()) for (Error error : dataResponse.errors()) apiErrorHandler.throwError(error);

            // If no Errors
            else if (dataResponse.data() != null) {

                this.typesOfWasteData = dataResponse.data().typesOfWaste();
                this.typesOfWasteTitle = new String[typesOfWasteData.size()];

                int i = 0;
                for (TypeOfWasteQuery.TypesOfWaste type : typesOfWasteData) {
                    typesOfWasteTitle[i] = type.name();
                    i++;
                }
            }

        }, throwable -> {
            if (throwable != null && throwable.getMessage() != null) Log.e(TAG_APOLLO_TYPE_QUERY, throwable.getMessage());
        });
    }

    private ApolloQueryCall<TypeOfWasteQuery.Data> getRequestCall() {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(NetworkingConstants.BASE_URL).build();
        return apolloClient.query(builder.build());
    }

    public boolean isTypesLoaded() {
        return typesOfWasteData != null && typesOfWasteData.size() > 0;
    }

    public String[] getTypesOfWasteTitle() {
        return typesOfWasteTitle;
    }

    public String getTypeId(Integer index) {
        if ( ! isTypesLoaded()) return null;
        else return typesOfWasteData.get(index)._id();
    }
}
