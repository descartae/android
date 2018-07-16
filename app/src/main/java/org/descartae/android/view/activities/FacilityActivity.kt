package org.descartae.android.view.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_facility.fab
import kotlinx.android.synthetic.main.activity_facility.loading
import kotlinx.android.synthetic.main.activity_facility.toolbar
import kotlinx.android.synthetic.main.content_facility.location
import kotlinx.android.synthetic.main.content_facility.name
import kotlinx.android.synthetic.main.content_facility.phone
import kotlinx.android.synthetic.main.content_facility.recyclerView_more_times
import kotlinx.android.synthetic.main.content_facility.textView_distance
import kotlinx.android.synthetic.main.content_facility.text_time
import kotlinx.android.synthetic.main.content_facility.time_expand
import kotlinx.android.synthetic.main.content_facility.type_waste
import org.descartae.android.DescartaeApp
import org.descartae.android.FacilityQuery
import org.descartae.android.R
import org.descartae.android.adapters.OpenHourListAdapter
import org.descartae.android.adapters.WastesTypeListAdapter
import org.descartae.android.interfaces.RetryConnectionView
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.presenter.facility.FacilityPresenter
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.descartae.android.view.fragments.facility.FeedbackDialog
import org.descartae.android.view.fragments.wastes.WasteTypeDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Calendar
import javax.inject.Inject

class FacilityActivity : AppCompatActivity(), OnMapReadyCallback, RetryConnectionView {

  @Inject
  lateinit var eventBus: EventBus

  @Inject
  lateinit var presenter: FacilityPresenter

  private var facility: FacilityQuery.Facility? = null

  private var mTypesWasteAdapter: WastesTypeListAdapter? = null

  private var mMapFragment: MapFragment? = null

  companion object {
    const val ARG_ID = "id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.activity_facility)

    if (intent == null) {
      finish()
      return
    }

    setSupportActionBar(toolbar)

    supportActionBar?.let {
      it.setDisplayShowTitleEnabled(false)
      it.setDisplayHomeAsUpEnabled(true)
      it.setDisplayShowHomeEnabled(true)
    }

    /*
     * Init Dagger
     */
    (applicationContext as DescartaeApp).component.inject(this)

    // Type of Waste
    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    type_waste.layoutManager = layoutManager

    mTypesWasteAdapter = WastesTypeListAdapter {

      val ft = supportFragmentManager.beginTransaction()
      val prev = supportFragmentManager.findFragmentByTag("dialog")
      if (prev != null) {
        ft.remove(prev)
      }
      ft.addToBackStack(null)

      // Create and show the dialog.
      val newFragment = WasteTypeDialog.newInstance(
          it.name(),
          it.description(),
          it.icons().androidMediumURL()
      )
      newFragment.show(ft, "dialog")
    }
    type_waste.adapter = mTypesWasteAdapter

    val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
    val drawableDividerSpacing = ContextCompat.getDrawable(this, R.drawable.divider_spacing)
    if (drawableDividerSpacing != null) dividerItemDecoration.setDrawable(drawableDividerSpacing)
    type_waste.addItemDecoration(dividerItemDecoration)

    // Butterknife sucks for Fragment
    mMapFragment = MapFragment.newInstance()
    fragmentManager.beginTransaction().replace(R.id.map, mMapFragment).commitAllowingStateLoss()

    fab.setOnClickListener {

      facility?.location()?.coordinates()?.let {
        val uri = ("geo:${it.latitude()},${it.longitude()}?q=${it.latitude()},${it.longitude()}")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
      }
    }

    handleIntent()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent()
  }

  public override fun onStart() {
    super.onStart()
    eventBus.register(this)
  }

  public override fun onStop() {
    super.onStop()
    eventBus.unregister(this)
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onError(error: GeneralError) {
    finish()
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onGetCurrentLocation(location: Location?) {
    if (facility == null || location == null) return

    val facilityLocation = Location("Facility")
    facilityLocation.latitude = facility!!.location().coordinates().latitude()
    facilityLocation.longitude = facility!!.location().coordinates().longitude()

    val distance = location.distanceTo(facilityLocation)
    textView_distance.text = getString(R.string.distance, distance / 1000)
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
  fun renderFacility(facility: FacilityQuery.Facility) {

    this.facility = facility

    location.text = facility.location().address()
    name.text = facility.name()
    phone.text = facility.telephone()

    val llm = LinearLayoutManager(this@FacilityActivity)
    llm.orientation = LinearLayoutManager.VERTICAL
    recyclerView_more_times.layoutManager = llm

    val timeListAdapter = OpenHourListAdapter(this@FacilityActivity)
    timeListAdapter.setFacilityDays(facility.openHours())
    recyclerView_more_times.adapter = timeListAdapter
    timeListAdapter.notifyDataSetChanged()

    var time: String? = null
    for (openHour in facility.openHours()) {
      if (openHour.dayOfWeek().ordinal + 1 == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        time = getString(R.string.time, "Hoje",
            getString(R.string.time_desc, openHour.startTime(), openHour.endTime()))
        text_time.text = time
        break
      }
    }

    if (time == null) {
      text_time.text = getString(R.string.no_open_hour_today)
    }

    mMapFragment!!.getMapAsync(this@FacilityActivity)

    mTypesWasteAdapter!!.types = facility.typesOfWaste()
    mTypesWasteAdapter!!.notifyDataSetChanged()

    time_expand.setOnClickListener {

      if (recyclerView_more_times.visibility == View.VISIBLE) {
        Picasso.get().load(R.drawable.ic_action_expand_more).into(time_expand)
        recyclerView_more_times.visibility = View.GONE
      } else {
        Picasso.get().load(R.drawable.ic_action_expand_less).into(time_expand)
        recyclerView_more_times.visibility = View.VISIBLE
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_facility, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    if (id == R.id.action_share) {

      if (facility == null) return false

      ShareCompat.IntentBuilder.from(this)
          .setType("text/plain")
          .setChooserTitle(R.string.menu_share)
          .setText(getString(R.string.share_url, facility!!._id()))
          .startChooser()

      return true
    }
    if (id == R.id.action_feedback) {
      FeedbackDialog.newInstance(facility!!._id()).show(supportFragmentManager, "DIALOG_FEEDBACK")
      return true
    }
    if (id == android.R.id.home) {
      finish()
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onMapReady(googleMap: GoogleMap) {

    if (facility == null) return

    val permissionCheck = ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION)
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      return
    }

    val latlng = LatLng(
        facility!!.location().coordinates().latitude(),
        facility!!.location().coordinates().longitude()
    )

    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13f))
    googleMap.addMarker(
        MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
            .position(latlng)
            .title(facility!!.name()))
  }

  override fun onRetryConnection() {
    presenter.requestFacility()
  }

  private fun handleIntent() {
    // ID coming from deeplink or not
    val facilityId = intent.data?.getQueryParameter(ARG_ID) ?:intent.getStringExtra(ARG_ID)

    presenter.setFacilityId(facilityId)
    presenter.requestFacility()
  }

}