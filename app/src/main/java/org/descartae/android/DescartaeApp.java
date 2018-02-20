package org.descartae.android;

import android.app.Application;
import android.content.Context;

import org.descartae.android.di.components.AppComponent;
import org.descartae.android.di.components.DaggerAppComponent;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lucasmontano on 19/02/2018.
 */
public class DescartaeApp extends Application {

    private EventBus bus;
    private AppComponent component;

    protected AppComponent createComponent() {
        return DaggerAppComponent.builder().build();
    }

    public AppComponent getAppComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static DescartaeApp getInstance(Context context) {
        return (DescartaeApp) context.getApplicationContext();
    }
}
