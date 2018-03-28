package org.descartae.android.view.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_type_of_waste.*
import org.descartae.android.DescartaeApp
import org.descartae.android.R
import org.descartae.android.TypeOfWasteQuery
import org.descartae.android.adapters.LegendWasteTypeListAdapter
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.presenter.typeofwaste.TypeOfWastePresenter
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.descartae.android.view.utils.SpaceDividerItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class LegendTypeOfWasteActivity : AppCompatActivity() {

    @Inject lateinit var presenter: TypeOfWastePresenter
    @Inject lateinit var eventBus: EventBus

    private var adapter: LegendWasteTypeListAdapter? = null

    public override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_of_waste)

        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        /*
         * Init Dagger
         */
        (applicationContext as DescartaeApp).component.inject(this)

        list.layoutManager = LinearLayoutManager(this)
        list.addItemDecoration(SpaceDividerItemDecoration(80))

        adapter = LegendWasteTypeListAdapter(this)
        list.adapter = adapter
    }

    public override fun onResume() {
        super.onResume()
        presenter.setTriggerLoadingEvents(true)
        presenter.requestTypeOfWastes()
    }

    public override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun renderTypes(typesOfWasteList: List<TypeOfWasteQuery.TypesOfWaste>) {
        adapter?.types = typesOfWasteList
        adapter?.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventHideLoading(event: EventHideLoading) {
        loading.visibility = View.GONE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventShowLoading(event: EventShowLoading) {
        loading.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(error: GeneralError) {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}