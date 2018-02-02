package org.descartae.android.view.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import org.descartae.android.R;
import org.descartae.android.view.fragments.intro.IntroFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

public class Intro extends BaseActivity implements IntroFragment.IntroListener {

    private ScreenSlidePagerAdapter mPagerAdapter;

    @BindView(R.id.pager)
    ViewPager mPager;

    @BindView(R.id.indicator)
    CircleIndicator indicator;

    private boolean isPermissionGranted;
    private boolean introAlreadyViewed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        init();

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        indicator.setViewPager(mPager);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onStartApp() {

        if (isPermissionGranted) {
            startActivity(new Intent(this, Home.class));
            finish();
            return;
        }

        introAlreadyViewed = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.permission_gps_title)
                .setMessage(R.string.permission_gps_message)
                .setPositiveButton(R.string.action_continue, (DialogInterface dialogInterface, int i) -> {
                    onAcceptPermission();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    startActivity(new Intent(this, Home.class));
                    finish();
                    dialogInterface.dismiss();
                });
        builder.create().show();
    }

    @Override
    void permissionNotGranted() {
        isPermissionGranted = false;
    }

    @Override
    void permissionGranted() {
        isPermissionGranted = true;

        if (introAlreadyViewed) {
            startActivity(new Intent(this, Home.class));
            finish();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return IntroFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
