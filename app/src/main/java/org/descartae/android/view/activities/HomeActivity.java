package org.descartae.android.view.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.descartae.android.BuildConfig;
import org.descartae.android.R;
import org.descartae.android.interfaces.RetryConnectionView;
import org.descartae.android.networking.apollo.errors.ConnectionError;
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError;
import org.descartae.android.networking.apollo.errors.RegionNotSupportedError;
import org.descartae.android.preferences.DescartaePreferences;
import org.descartae.android.view.fragments.empty.EmptyGPSOfflineFragment;
import org.descartae.android.view.fragments.empty.EmptyLocationPermissionFragment;
import org.descartae.android.view.fragments.empty.EmptyOfflineFragment;
import org.descartae.android.view.fragments.empty.EmptyRegionUnsupportedFragment;
import org.descartae.android.view.fragments.empty.RegionWaitListDialog;
import org.descartae.android.view.fragments.facility.FacilitiesFragment;
import org.descartae.android.view.fragments.facility.FeedbackDialog;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, RetryConnectionView, EmptyRegionUnsupportedFragment.Listener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.content)
    FrameLayout content;

    private FacilitiesFragment facilitiesFragment;

    @Override
    void permissionNotGranted() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, EmptyLocationPermissionFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    void permissionGranted() {

        /**
         * Ok, the permission is granted but we need to check if the location is enabled :)
         */
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        if (gps_enabled) {

            /**
             * Do not override the current facilities fragment
             */
            if (facilitiesFragment == null || ! facilitiesFragment.isAdded()) {
                facilitiesFragment = FacilitiesFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.content, facilitiesFragment).commitAllowingStateLoss();
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, EmptyGPSOfflineFragment.newInstance()).commitAllowingStateLoss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);

        } else if (facilitiesFragment != null && facilitiesFragment.isAdded()) {

            if (facilitiesFragment.isBottomSheetOpen()) {
                facilitiesFragment.closeBottomSheet();
            } else {
                super.onBackPressed();
            }

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_info) {
            startActivity(new Intent(this, LegendTypeOfWasteActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        drawer.closeDrawer(GravityCompat.START);

        String storeLink = "http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;

        if (id == R.id.nav_rate) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_share, BuildConfig.APPLICATION_ID)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storeLink)));
            }

            return true;
        } else if (id == R.id.nav_share) {

            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setChooserTitle(R.string.menu_share)
                    .setText(storeLink)
                    .startChooser();

            return true;
        } else if (id == R.id.nav_feedback) {
            FeedbackDialog.newInstance(null).show(getSupportFragmentManager(), "DIALOG_FEEDBACK");
            return true;
        } else if (id == R.id.nav_about) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url, "")));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storeLink)));
            }

            return true;
        }

        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showNoConnectionEmptyState(ConnectionError error) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.content,
                EmptyOfflineFragment.newInstance()
        ).commitAllowingStateLoss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showRegionNotSupported(RegionNotSupportedError error) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.content,
                EmptyRegionUnsupportedFragment.newInstance(
                        preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LAT),
                        preferences.getDoubleValue(DescartaePreferences.PREF_LAST_LOCATION_LNG)
                )
        ).commitAllowingStateLoss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDuplicatedEmailError(DuplicatedEmailError duplicatedEmailError) {
        Snackbar.make(content, R.string.wait_list_double_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showWaitListDialog(double latitude, double longitude) {
        RegionWaitListDialog.newInstance(latitude, longitude).show(getSupportFragmentManager(), "DIALOG_WAIT_LIST");
    }

    @Override
    public void onRetryConnection() {
        permissionGranted();
    }

}
