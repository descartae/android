package org.descartae.android.view.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ShareCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import org.descartae.android.BuildConfig
import org.descartae.android.R
import org.descartae.android.interfaces.RetryConnectionView
import org.descartae.android.networking.apollo.errors.ConnectionError
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError
import org.descartae.android.preferences.DescartaePreferences
import org.descartae.android.view.fragments.empty.*
import org.descartae.android.view.fragments.facility.FacilitiesFragment
import org.descartae.android.view.fragments.facility.FeedbackDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.crashlytics.android.Crashlytics



class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, RetryConnectionView, EmptyRegionUnsupportedFragment.Listener {

    private var facilitiesFragment: FacilitiesFragment? = null

    @Override override fun permissionNotGranted() {
        supportFragmentManager.beginTransaction().replace(R.id.content, EmptyLocationPermissionFragment.newInstance()).commitAllowingStateLoss()
    }

    @Override override fun permissionGranted() {

        /**
         * Ok, the permission is granted but we need to check if the location is enabled :)
         */
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (gpsEnabled) {

            /**
             * Do not override the current facilities fragment
             */
            if (facilitiesFragment == null || !facilitiesFragment!!.isAdded) {
                facilitiesFragment = FacilitiesFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.content, facilitiesFragment).commitAllowingStateLoss()
            }
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.content, EmptyGPSOfflineFragment.newInstance()).commitAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {

            drawer_layout.closeDrawer(GravityCompat.START)

        } else if (facilitiesFragment != null && facilitiesFragment!!.isAdded) {

            if (facilitiesFragment!!.isBottomSheetOpen) {
                facilitiesFragment!!.closeBottomSheet()
            } else {
                super.onBackPressed()
            }

        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_info) {
            startActivity(Intent(this, LegendTypeOfWasteActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        drawer_layout.closeDrawer(GravityCompat.START)

        val storeLink = "http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID

        when (id) {
            R.id.nav_rate -> {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_share, BuildConfig.APPLICATION_ID)))
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeLink)))
                }

                return true
            }
            R.id.nav_share -> {

                ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setChooserTitle(R.string.menu_share)
                        .setText(storeLink)
                        .startChooser()

                return true
            }
            R.id.nav_feedback -> {
                FeedbackDialog.newInstance(null).show(supportFragmentManager, "DIALOG_FEEDBACK")
                return true
            }
            R.id.nav_about -> {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url, "")))
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeLink)))
                }

                return true
            }
            else -> return true
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showNoConnectionEmptyState(error: ConnectionError) {
        supportFragmentManager.beginTransaction().replace(
                R.id.content,
                EmptyOfflineFragment.newInstance()
        ).commitAllowingStateLoss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showRegionNotSupported(error: RegionNotSupportedError) {
        supportFragmentManager.beginTransaction().replace(
                R.id.content,
                EmptyRegionUnsupportedFragment.newInstance(
                        preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LAT)!!,
                        preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LNG)!!
                )
        ).commitAllowingStateLoss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDuplicatedEmailError(duplicatedEmailError: DuplicatedEmailError) {
        Snackbar.make(content, R.string.wait_list_double_error, Snackbar.LENGTH_LONG).show()
    }

    override fun showWaitListDialog(latitude: Double, longitude: Double) {
        RegionWaitListDialog.newInstance(latitude, longitude).show(supportFragmentManager, "DIALOG_WAIT_LIST")
    }

    override fun onRetryConnection() {
        permissionGranted()
    }
}