package org.descartae.android

import android.app.Application
import org.descartae.android.di.components.AppComponent
import org.descartae.android.di.components.DaggerAppComponent
import org.descartae.android.di.modules.AppModule
import org.greenrobot.eventbus.EventBus

class DescartaeApp : Application() {

    var eventBus: EventBus? = null

    val component: AppComponent by lazy {
        DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        eventBus = EventBus.builder().build()
    }
}
