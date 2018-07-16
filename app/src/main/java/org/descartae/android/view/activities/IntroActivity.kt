package org.descartae.android.view.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_intro.indicator
import kotlinx.android.synthetic.main.activity_intro.pager
import org.descartae.android.BuildConfig
import org.descartae.android.R
import org.descartae.android.preferences.DescartaePreferences
import org.descartae.android.view.fragments.intro.IntroFragment

class IntroActivity : BaseActivity(), IntroFragment.IntroListener {

  private var mPagerAdapter: ScreenSlidePagerAdapter? = null
  private var isPermissionGranted = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val fabric = Fabric.Builder(this)
        .kits(Crashlytics(), Answers())
        .debuggable(BuildConfig.DEBUG)
        .build()
    Fabric.with(fabric)
    setContentView(R.layout.activity_intro)

    if (preferences.getBooleanValue(DescartaePreferences.INTRO_OK)) {
      startActivity(Intent(this, HomeActivity::class.java))
      finish()
      return
    }

    init()

    mPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
    pager.adapter = mPagerAdapter
    indicator.setViewPager(pager)
  }

  override fun onBackPressed() {
    if (pager.currentItem == 0) {
      super.onBackPressed()
    } else {
      pager.currentItem = pager.currentItem - 1
    }
  }

  override fun onStartApp() {

    preferences.setBooleanValue(DescartaePreferences.INTRO_OK, true)

    if (isPermissionGranted) {
      startActivity(Intent(this, HomeActivity::class.java))
      finish()
      return
    }

    val builder = AlertDialog.Builder(this)
        .setTitle(R.string.permission_gps_title)
        .setMessage(R.string.permission_gps_message)
        .setPositiveButton(R.string.action_continue) { dialogInterface: DialogInterface, i: Int ->
          onAcceptPermission()
          dialogInterface.dismiss()

          // Do not block user If denies permission after agree
          isPermissionGranted = true
        }
        .setNegativeButton(R.string.cancel) { dialogInterface, i ->
          startActivity(Intent(this, HomeActivity::class.java))
          finish()
          dialogInterface.dismiss()
        }
    builder.create().show()
  }

  override fun permissionNotGranted() {

    // Do not block user If denies permission after agree
    if (isPermissionGranted) {
      permissionGranted()
    }
  }

  override fun permissionGranted() {
    isPermissionGranted = true

    if (preferences.getBooleanValue(DescartaePreferences.INTRO_OK)) {
      startActivity(Intent(this, HomeActivity::class.java))
      finish()
    }
  }

  private inner class ScreenSlidePagerAdapter internal constructor(
    fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      return IntroFragment.newInstance(position)
    }

    override fun getCount(): Int {
      return 4
    }
  }
}
