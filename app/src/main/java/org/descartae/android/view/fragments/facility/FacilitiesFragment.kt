package org.descartae.android.view.fragments.facility

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
import com.google.firebase.appindexing.builders.Indexables
import kotlinx.android.synthetic.main.filter_empty_view.action_clear_filter
import kotlinx.android.synthetic.main.filter_empty_view.filter_empty
import kotlinx.android.synthetic.main.fragment_facility_list.action_detail
import kotlinx.android.synthetic.main.fragment_facility_list.action_go
import kotlinx.android.synthetic.main.fragment_facility_list.bottom_sheet
import kotlinx.android.synthetic.main.fragment_facility_list.bottom_sheet_detail
import kotlinx.android.synthetic.main.fragment_facility_list.list
import kotlinx.android.synthetic.main.fragment_facility_list.loading
import org.descartae.android.DescartaeApp
import org.descartae.android.FacilitiesQuery
import org.descartae.android.R
import org.descartae.android.adapters.FacilityListAdapter
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError
import org.descartae.android.presenter.facility.FacilityListPresenter
import org.descartae.android.presenter.typeofwaste.TypeOfWastePresenter
import org.descartae.android.view.activities.FacilityActivity
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.descartae.android.view.utils.SimpleDividerItemDecoration
import org.descartae.android.view.viewholder.FacilityViewHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList
import javax.inject.Inject

class FacilitiesFragment : Fragment(), OnMapReadyCallback {

  private var facilityListAdapter: FacilityListAdapter? = null

  @Inject
  lateinit var presenter: FacilityListPresenter

  @Inject
  lateinit var presenterTypeWaste: TypeOfWastePresenter

  @Inject
  lateinit var eventBus: EventBus

  private var behaviorDetail: BottomSheetBehavior<View>? = null
  private var behaviorList: BottomSheetBehavior<View>? = null
  private var mItemSelected: FacilitiesQuery.Item? = null

  private var mMapFragment: MapFragment? = null
  private var mMap: GoogleMap? = null

  private var selectedTypesIndices = arrayOf<Int>()

  private val callbackBottomSheet: BottomSheetBehavior.BottomSheetCallback
    get() = object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {

        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
          bottom_sheet.visibility = View.VISIBLE
          behaviorList!!.state = BottomSheetBehavior.STATE_EXPANDED


          mItemSelected?.let {
            FirebaseUserActions.getInstance().end(getFacilityViewAction(it))
          }

          mItemSelected = null
          fillMapMarkers()
        }
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

  val isBottomSheetOpen: Boolean get() = behaviorDetail!!.state == BottomSheetBehavior.STATE_EXPANDED

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {

    when (item!!.itemId) {

      R.id.action_filter -> {

        if (!presenter.haveCurrentLocation()) {
          return false
        }

        if (!presenterTypeWaste.isTypesLoaded()) {
          Log.d("Filter", "No Types")
          return false
        }

        if (activity == null) return false

        MaterialDialog.Builder(activity!!)
            .title(R.string.title_filter)
            .items(*presenterTypeWaste.typesOfWasteTitle!!)
            .itemsCallbackMultiChoice(selectedTypesIndices
            ) { _, which, _ ->
              selectedTypesIndices = which

              val selected = ArrayList<String>()
              for (index in which) {
                presenterTypeWaste.getTypeId(index)?.let { selected.add(it) }
              }
              facilityListAdapter!!.centers = null
              facilityListAdapter!!.notifyDataSetChanged()
              fillMapMarkers()

              presenter.setFilterTypesID(selected)
              presenter.requestFacilities()

              true
            }
            .positiveText(R.string.action_filter)
            .show()

        return true
      }
    }
    return false
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    /*
     * Init Dagger
     */
    (activity!!.applicationContext as DescartaeApp).component.inject(this)

    presenter.requestLocation()
    presenterTypeWaste.requestTypeOfWastes()

    action_go.setOnClickListener {

      mItemSelected?.location()?.coordinates()?.let {
        val uri = ("geo:${it.latitude()},${it.longitude()}?q=${it.latitude()},${it.longitude()}")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
      }
    }

    action_clear_filter.setOnClickListener {

      selectedTypesIndices = arrayOf()

      filter_empty.visibility = View.GONE

      // Clear List
      facilityListAdapter!!.centers = null
      facilityListAdapter!!.notifyDataSetChanged()

      // Refresh MapMarkers
      fillMapMarkers()

      presenter.setFilterTypesID(null)
      presenter.requestFacilities()
    }

    action_detail.setOnClickListener {

      mItemSelected?.let {
        val intent = Intent(activity, FacilityActivity::class.java)
        intent.putExtra(FacilityActivity.ARG_ID, it._id())
        startActivity(intent)
      }
    }

    list.addItemDecoration(SimpleDividerItemDecoration(context!!))
    list.layoutManager = LinearLayoutManager(context)
    facilityListAdapter = FacilityListAdapter { center: FacilitiesQuery.Item ->

      // On Facility Item Clicked
      mItemSelected = center
      selectFacility(center)
    }
    list.adapter = facilityListAdapter

    behaviorList = BottomSheetBehavior.from(bottom_sheet)
    behaviorList!!.isHideable = false
    behaviorList!!.peekHeight = resources.getDimensionPixelOffset(R.dimen.facilities_peek_height)

    behaviorDetail = BottomSheetBehavior.from(bottom_sheet_detail)
    behaviorDetail!!.setBottomSheetCallback(callbackBottomSheet)
    behaviorDetail!!.isHideable = true
  }

  override fun onResume() {
    super.onResume()
    eventBus.register(this)
  }

  override fun onPause() {
    super.onPause()
    eventBus.unregister(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_facility_list, container, false)
    mMapFragment = MapFragment.newInstance()
    activity!!.fragmentManager.beginTransaction().replace(R.id.map,
        mMapFragment).commitAllowingStateLoss()
    return view
  }

  private fun selectFacility(center: FacilitiesQuery.Item) {

    // Fill Item Detail
    val facilityViewHolder = FacilityViewHolder(bottom_sheet_detail)
    facilityViewHolder.mItem = center
    facilityViewHolder.setCurrentLocation(presenter.currentLocation)
    facilityViewHolder.fill()

    // Show BottomSheetDetail
    indexingFacility(center)
    FirebaseUserActions.getInstance().start(getFacilityViewAction(center))

    bottom_sheet_detail.visibility = View.VISIBLE
    behaviorDetail!!.state = BottomSheetBehavior.STATE_EXPANDED

    // List Collapse and GONE
    behaviorList!!.state = BottomSheetBehavior.STATE_COLLAPSED
    bottom_sheet.visibility = View.GONE

    // Move Map
    if (mMap != null) {

      mMap!!.clear()

      val latlng = LatLng(
          mItemSelected!!.location().coordinates().latitude(),
          mItemSelected!!.location().coordinates().longitude()
      )

      mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latlng))
      mMap!!.addMarker(
          MarkerOptions()
              .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
              .position(latlng)
              .title(mItemSelected!!.name()))
    }
  }

  override fun onMapReady(googleMap: GoogleMap) {

    mMap = googleMap

    fillMapMarkers()

    mMap!!.setOnMarkerClickListener { marker: Marker ->
      marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
      selectFacility(marker.title)
      false
    }
  }

  private fun selectFacility(name: String) {
    val centers = facilityListAdapter!!.centers
    if (centers != null && centers.size > 0) {
      for (facility in centers) {
        if (facility.name() == name) {
          mItemSelected = facility
          selectFacility(facility)
          break
        }
      }
    }
  }

  private fun fillMapMarkers() {

    mMap!!.clear()

    // Add pins to map
    val facilities = facilityListAdapter!!.centers
    if (facilities != null && facilities.isNotEmpty()) {

      facilities.forEach { facility ->
        val latlng = LatLng(facility.location().coordinates().latitude(),
            facility.location().coordinates().longitude())
        mMap!!.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_places_map))
                .position(latlng)
                .snippet(facility.location().address())
                .title(facility.name()))
      }
    }

    // Move camera
    val currentLocation = presenter.currentLocation

    if (currentLocation != null && mMap != null) {
      val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
      mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
    }
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
  fun renderFacilities(facilities: FacilitiesQuery.Facilities?) {

    val hasFacilities = facilities?.items() != null && facilities.items()!!.size > 0

    if (!hasFacilities && presenter.hasFilterType()) {

      /*
       * If no facilities return with filter
       */
      filter_empty.visibility = View.VISIBLE

    } else if (hasFacilities) {

      /*
       * If have facilities
       */
      facilityListAdapter!!.centers = facilities!!.items()
      facilityListAdapter!!.currentLocation = presenter.currentLocation
      facilityListAdapter!!.notifyDataSetChanged()

      mMapFragment!!.getMapAsync(this@FacilitiesFragment)
    } else {

      /*
        If no facilities and no filter
        @deprecated the server is already checkin this situation as Error
       */
      eventBus.post(RegionNotSupportedError())
    }
  }

  fun closeBottomSheet() {
    behaviorDetail!!.state = BottomSheetBehavior.STATE_HIDDEN
  }

  private fun indexingFacility(facility: FacilitiesQuery.Item) {
    val facilityToUpdate = Indexables.placeBuilder()
        .setName(facility.name())
        .setUrl(getString(R.string.deeplink_uri, facility._id()))
        .setDescription(facility.location().address())
        .build()

    val task = FirebaseAppIndex.getInstance().update(facilityToUpdate)
    task.addOnSuccessListener {
      Log.d(
          "App Indexing",
          "App Indexing API: Successfully added ${facility.name()} to index"
      )
    }
    task.addOnFailureListener {
      Log.e(
          "App Indexing",
          "App Indexing API: Failed to add ${facility.name()} to index. ${it.message}"
      )
    }
  }

  private fun getFacilityViewAction(facility: FacilitiesQuery.Item): Action? {
    return Actions.newView(facility.name(), getString(R.string.deeplink_uri, facility._id()))
  }

  companion object {

    fun newInstance(): FacilitiesFragment {
      val fragment = FacilitiesFragment()
      val args = Bundle()
      fragment.arguments = args
      return fragment
    }
  }
}