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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.AddFeedbackMutation;
import org.descartae.android.R;
import org.descartae.android.networking.NetworkingConstants;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lucasmontano on 09/12/2017.
 */

public class FeedbackDialog extends DialogFragment {

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

    @OnClick(R.id.action_send)
    public void onSend() {

        String feedback = mFeedback.getText().toString();

        if (feedback == null || feedback.length() <= 0) {
            Snackbar.make(mFeedback, R.string.feedback_no_message_error, Snackbar.LENGTH_SHORT).show();
            return;
        }

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();

        AddFeedbackMutation.Builder builder = AddFeedbackMutation.builder();
        builder.facilityId(facilityID);
        builder.feedback(feedback);

        AddFeedbackMutation build = builder.build();
        apolloClient.mutate(build).enqueue(new ApolloCall.Callback<AddFeedbackMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<AddFeedbackMutation.Data> response) {

                if (response == null) return;
                if (response.data() == null) return;
                if (getActivity() == null || getActivity().isDestroyed()) return;

                getActivity().runOnUiThread(() -> {

                    if (response.data().addFeedback().success()) {
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
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Snackbar.make(mFeedback, R.string.feedback_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick({R.id.action_cancel, R.id.action_ok})
    public void onClose() {
        dismiss();
    }
}
