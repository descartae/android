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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.AddToWaitlistMutation;
import org.descartae.android.R;
import org.descartae.android.networking.apollo.ApolloApiErrorHandler;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lucasmontano on 09/12/2017.
 */

public class RegionWaitListDialog extends DialogFragment {

    private static final String ARG_LATITUDE = "ARG_LATITUDE";
    private static final String ARG_LONGITUDE = "ARG_LONGITUDE";

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        RelativeLayout viewInflated = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_wait_list, null);

        ButterKnife.bind(this, viewInflated);

        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Light));
        mBuilder.setView(viewInflated);

        latitude = getArguments().getDouble(ARG_LATITUDE);
        longitude = getArguments().getDouble(ARG_LONGITUDE);

        return mBuilder.create();
    }

    @OnClick(R.id.action_send)
    public void onSend() {

        String email = mEmail.getText().toString();

        if (email == null || email.length() <= 0 || ! email.contains("@")) {
            new ApolloApiErrorHandler(getString(R.string.wait_list_no_email_error));
            return;
        }

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();

        AddToWaitlistMutation.Builder builder = AddToWaitlistMutation.builder();
        builder.email(email);

        if (latitude != 0 && longitude != 0) {
            builder.latitude(latitude);
            builder.longitude(longitude);
        }

        showLoad();

        AddToWaitlistMutation build = builder.build();
        apolloClient.mutate(build).enqueue(new ApolloCall.Callback<AddToWaitlistMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<AddToWaitlistMutation.Data> response) {

                if (getActivity() == null || getActivity().isDestroyed()) return;
                if (response == null) return;

                getActivity().runOnUiThread(() -> {
                    hideLoad();
                });

                if (response.hasErrors()) {

                    // Default message error
                    ApolloApiErrorHandler.setGenericErrorMessage(getString(R.string.wait_list_error));

                    for (Error error : response.errors()) new ApolloApiErrorHandler(error);
                    dismiss();
                } else {

                    getActivity().runOnUiThread(() -> {

                        if (response.data().addWaitingUser().success()) {
                            onSuccess();
                        } else {
                            new ApolloApiErrorHandler(getString(R.string.wait_list_error));
                            dismiss();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                getActivity().runOnUiThread(() -> {
                    hideLoad();
                });

                new ApolloApiErrorHandler(getString(R.string.wait_list_error));

                dismiss();
            }
        });
    }

    private void onSuccess() {
        mActionCancel.setVisibility(View.GONE);
        mActionSend.setVisibility(View.GONE);
        mActionOk.setVisibility(View.VISIBLE);

        mEmail.setVisibility(View.GONE);

        mTitle.setText(R.string.wait_list_title_success);
        mSubTitle.setText(R.string.wait_list_desc_success);
    }

    private void showLoad() {
        linearForm.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoad() {
        loading.setVisibility(View.GONE);
        linearForm.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.action_cancel, R.id.action_ok})
    public void onClose() {
        dismiss();
    }

}
