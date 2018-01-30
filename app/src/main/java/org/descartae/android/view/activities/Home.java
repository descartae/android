package org.descartae.android.view.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.descartae.android.BuildConfig;
import org.descartae.android.R;
import org.descartae.android.interfaces.RetryConnectionView;
import org.descartae.android.view.fragments.empty.EmptyLocationPermissionFragment;
import org.descartae.android.view.fragments.empty.EmptyOfflineFragment;
import org.descartae.android.view.fragments.facility.FacilitiesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, FacilitiesFragment.OnListFacilitiesListener, RetryConnectionView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    void permissionNotGranted() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, EmptyLocationPermissionFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    void permissionGranted() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, FacilitiesFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        init();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        if (id == R.id.action_filter) {
            return true;
        } else if (id == R.id.action_info) {
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
        }

        return true;
    }

    @Override
    public void onNoConnection() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, EmptyOfflineFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    public void onRetryConnection() {
        permissionGranted();
    }
}
