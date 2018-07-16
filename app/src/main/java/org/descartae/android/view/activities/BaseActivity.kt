package org.descartae.android.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.descartae.android.DescartaeApp
import org.descartae.android.interfaces.RequestPermissionView
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.preferences.DescartaePreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), RequestPermissionView {

  @Inject
  lateinit var preferences: DescartaePreferences
  @Inject
  lateinit var eventBus: EventBus

  abstract fun permissionNotGranted()
  abstract fun permissionGranted()

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (applicationContext as DescartaeApp).component.inject(this)
  }

  public override fun onStart() {
    super.onStart()
    eventBus.register(this)
  }

  public override fun onStop() {
    super.onStop()
    eventBus.unregister(this)
  }

  public override fun onResume() {
    super.onResume()

    val instance = GoogleApiAvailability.getInstance()
    val googlePlayServicesAvailable = instance.isGooglePlayServicesAvailable(this)
    if (googlePlayServicesAvailable != ConnectionResult.SUCCESS) {
      instance.getErrorDialog(this, googlePlayServicesAvailable, RQ_GPSERVICE)
    }

    init()
  }

  protected fun init() {
    val permFineLocation = ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION)
    val permCoarseLocation = ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    if (permFineLocation == PackageManager.PERMISSION_GRANTED && permCoarseLocation == PackageManager.PERMISSION_GRANTED) {
      permissionGranted()
    } else {
      permissionNotGranted()
    }
  }

  override fun onAcceptPermission() {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
    grantResults: IntArray) {
    when (requestCode) {
      PERMISSIONS_REQUEST -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          permissionGranted()
        } else {
          permissionNotGranted()
        }
        return
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onError(error: GeneralError) {
    error.message?.let {
      Snackbar.make(currentFocus, it, Snackbar.LENGTH_SHORT).show()
    }
  }

  companion object {
    private val PERMISSIONS_REQUEST = 0x01
    private val RQ_GPSERVICE = 0x02
  }
}
