package org.descartae.android.view.fragments.empty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.descartae.android.AddToWaitlistMutation;
import org.descartae.android.DescartaeApp;
import org.descartae.android.R;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError;
import org.descartae.android.networking.apollo.errors.GeneralError;
import org.descartae.android.presenter.waitlist.WaitListPresenter;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lucasmontano on 09/12/2017.
 */
public class RegionWaitListDialog extends DialogFragment {

    private static final String ARG_LATITUDE = "ARG_LATITUDE";
    private static final String ARG_LONGITUDE = "ARG_LONGITUDE";

    @Inject WaitListPresenter presenter;

    @Inject EventBus eventBus;

    @BindView(R.id.linear_form)
    LinearLayout linearForm;

    @BindView(R.id.loading)
    ProgressBar loading;

    @BindView(R.id.title)
    public TextView mTitle;

    @BindView(R.id.subtitle)
    public TextView mSubTitle;

    @BindView(R.id.email)
    public EditText mEmail;

    @BindView(R.id.action_cancel)
    public View mActionCancel;

    @BindView(R.id.action_send)
    public View mActionSend;

    @BindView(R.id.action_ok)
    public View mActionOk;

    private AlertDialog.Builder mBuilder;

    private double latitude;
    private double longitude;

    public static RegionWaitListDialog newInstance(double latitude, double longitude) {
        RegionWaitListDialog frag = new RegionWaitListDialog();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /**
         * Init Dagger
         */
        DescartaeApp.getInstance(getActivity())
                .getAppComponent()
                .inject(this);

        RelativeLayout viewInflated = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_wait_list, null);

        ButterKnife.bind(this, viewInflated);

        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Light));
        mBuilder.setView(viewInflated);

        latitude = getArguments().getDouble(ARG_LATITUDE);
        longitude = getArguments().getDouble(ARG_LONGITUDE);

        return mBuilder.create();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    @OnClick(R.id.action_send)
    public void onSend() {

        String email = mEmail.getText().toString();

        if (email == null || email.length() <= 0 || ! email.contains("@")) {
            new ApolloApiErrorHandler(getString(R.string.wait_list_no_email_error));
            return;
        }

        ApolloApiErrorHandler.setGenericErrorMessage(getString(R.string.wait_list_error));

        presenter.setLatLng(longitude, latitude);
        presenter.addToWaitList(email);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onError(GeneralError error) {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDuplicatedEmailError(DuplicatedEmailError duplicatedEmailError) {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddWaitList(AddToWaitlistMutation.Data data) {
        if (data.addWaitingUser().success()) onSuccess();
        else dismiss();
    }

    private void onSuccess() {
        mActionCancel.setVisibility(View.GONE);
        mActionSend.setVisibility(View.GONE);
        mActionOk.setVisibility(View.VISIBLE);

        mEmail.setVisibility(View.GONE);

        mTitle.setText(R.string.wait_list_title_success);
        mSubTitle.setText(R.string.wait_list_desc_success);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventShowLoading(EventShowLoading event) {
        linearForm.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventHideLoading(EventHideLoading event) {
        loading.setVisibility(View.GONE);
        linearForm.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.action_cancel, R.id.action_ok})
    public void onClose() {
        dismiss();
    }

}
