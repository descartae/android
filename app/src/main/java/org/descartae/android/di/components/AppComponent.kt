package org.descartae.android.di.components

import dagger.Component
import org.descartae.android.di.modules.AppModule
import org.descartae.android.view.activities.BaseActivity
import org.descartae.android.view.activities.FacilityActivity
import org.descartae.android.view.activities.LegendTypeOfWasteActivity
import org.descartae.android.view.fragments.empty.RegionWaitListDialog
import org.descartae.android.view.fragments.facility.FacilitiesFragment
import org.descartae.android.view.fragments.facility.FeedbackDialog
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun inject(baseActivity: BaseActivity)
    fun inject(fragment: FacilitiesFragment)
    fun inject(regionWaitListDialog: RegionWaitListDialog)
    fun inject(legendTypeOfWasteActivity: LegendTypeOfWasteActivity)
    fun inject(facilityActivity: FacilityActivity)
    fun inject(feedbackDialog: FeedbackDialog)
}