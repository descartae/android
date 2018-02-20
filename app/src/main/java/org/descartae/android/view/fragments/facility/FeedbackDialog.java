package org.descartae.android.view.fragments.facility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.descartae.android.AddFeedbackMutation;
import org.descartae.android.DescartaeApp;
import org.descartae.android.R;
import org.descartae.android.presenter.feedback.FeedbackPresenter;
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
public class FeedbackDialog extends DialogFragment {

    @Inject FeedbackPresenter presenter;

    @Inject EventBus eventBus;

    @BindView(R.id.title)
    public TextView mTitle;

    @BindView(R.id.subtitle)
    public TextView mSubTitle;

    @BindView(R.id.message)
    public EditText mFeedback;

    @BindView(R.id.action_cancel)
    public View mActionCancel;

    @BindView(R.id.action_send)
    public View mActionSend;

    @BindView(R.id.action_ok)
    public View mActionOk;

    private String facilityID;
    private AlertDialog.Builder mBuilder;

    public static FeedbackDialog newInstance(String facilityID) {
        FeedbackDialog frag = new FeedbackDialog();
        Bundle args = new Bundle();
        args.putString("facilityID", facilityID);
        frag.setArguments(args);
        return frag;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Init Dagger
         */
        DescartaeApp.getInstance(getActivity())
                .getAppComponent()
                .inject(this);

        presenter.setFacilityId(facilityID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LinearLayout viewInflated = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);

        ButterKnife.bind(this, viewInflated);

        facilityID = getArguments().getString("facilityID");

        if (facilityID == null) {
            mTitle.setText(R.string.feedback_title);
            mSubTitle.setText(R.string.feedback_desc);
        } else {
            mTitle.setText(R.string.feedback_facility_title);
            mSubTitle.setText(R.string.feedback_facility_desc);
        }

        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Light));
        mBuilder.setView(viewInflated);

        return mBuilder.create();
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

    @OnClick(R.id.action_send)
    public void onSend() {

        String feedback = mFeedback.getText().toString();

        if (feedback == null || feedback.length() <= 0) {
            Snackbar.make(mFeedback, R.string.feedback_no_message_error, Snackbar.LENGTH_SHORT).show();
            return;
        }

        presenter.sendFeedback(feedback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedbackResult(AddFeedbackMutation.Data data) {
        if (data.addFeedback().success()) {
            mActionCancel.setVisibility(View.GONE);
            mActionSend.setVisibility(View.GONE);
            mActionOk.setVisibility(View.VISIBLE);

            mFeedback.setVisibility(View.GONE);

            if (facilityID == null) {
                mTitle.setText(R.string.feedback_title_success);
                mSubTitle.setText(R.string.feedback_desc_success);
            } else {
                mTitle.setText(R.string.feedback_facility_title_success);
                mSubTitle.setText(R.string.feedback_facility_desc_success);
            }
        } else {
            Snackbar.make(mFeedback, R.string.feedback_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.action_cancel, R.id.action_ok})
    public void onClose() {
        dismiss();
    }
}
