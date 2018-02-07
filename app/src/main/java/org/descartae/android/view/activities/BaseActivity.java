package org.descartae.android.view.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.descartae.android.interfaces.RequestPermissionView;

/**
 * Created by lucasmontano on 05/12/2017.
 */
public abstract class BaseActivity extends AppCompatActivity implements RequestPermissionView {

    private static final int PERMISSIONS_REQUEST = 0x01;
    private static final int RQ_GPSERVICE = 0x02;

    abstract void permissionNotGranted();
    abstract void permissionGranted();

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
}
