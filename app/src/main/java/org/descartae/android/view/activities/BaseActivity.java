package org.descartae.android.view.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.descartae.android.DescartaeApp;
import org.descartae.android.interfaces.RequestPermissionView;
import org.descartae.android.networking.apollo.errors.GeneralError;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 05/12/2017.
 */
public abstract class BaseActivity extends AppCompatActivity implements RequestPermissionView {

    private static final int PERMISSIONS_REQUEST = 0x01;
    private static final int RQ_GPSERVICE = 0x02;

    @Inject EventBus eventBus;

    abstract void permissionNotGranted();
    abstract void permissionGranted();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Init Dagger
         */
        DescartaeApp.getInstance(this)
                .getAppComponent()
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    public void onResume() {
        super.onResume();

        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int googlePlayServicesAvailable = instance.isGooglePlayServicesAvailable(this);
        if (googlePlayServicesAvailable != ConnectionResult.SUCCESS) {
            instance.getErrorDialog(this, googlePlayServicesAvailable, RQ_GPSERVICE);
        }

        init();
    }

    protected void init() {
        int permFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permFineLocation == PackageManager.PERMISSION_GRANTED && permCoarseLocation == PackageManager.PERMISSION_GRANTED) {
            permissionGranted();
        } else {
            permissionNotGranted();
        }
    }

    @Override
    public void onAcceptPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted();
                } else {
                    permissionNotGranted();
                }
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onError(GeneralError error) {
        if (error.getMessage() != null)
            Snackbar.make(getCurrentFocus(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
    }
}
