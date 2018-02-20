package org.descartae.android.di.components;

import org.descartae.android.di.modules.AppModule;
import org.descartae.android.view.activities.BaseActivity;
import org.descartae.android.view.fragments.empty.RegionWaitListDialog;
import org.descartae.android.view.fragments.facility.FacilitiesFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by lucasmontano on 19/02/2018.
 */

@Singleton
@Component(modules = {
        AppModule.class
})
public interface AppComponent {

    void inject(BaseActivity baseActivity);
    void inject(FacilitiesFragment fragment);
    void inject(RegionWaitListDialog regionWaitListDialog);
}