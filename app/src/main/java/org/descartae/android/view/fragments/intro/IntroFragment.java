package org.descartae.android.view.fragments.intro;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.descartae.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroFragment extends Fragment {

    private static final String ARG_PAGE = "page";

    private int page;

    @BindView(R.id.intro_image)
    ImageView mImage;

    @BindView(R.id.intro_title)
    TextView mTitle;

    @BindView(R.id.intro_subtitle)
    TextView mSubTitle;

    @BindView(R.id.action_start)
    Button mStart;

    private IntroListener mListener;

    public interface IntroListener {
        void onStartApp();
    }

    public IntroFragment() {
    }

    public static IntroFragment newInstance(int page) {
        IntroFragment fragment = new IntroFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt(ARG_PAGE, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        ButterKnife.bind(this, view);

        switch (page) {
            case 0:

                mTitle.setText(R.string.onboard_title_1);
                mSubTitle.setText(R.string.onboard_subtitle_1);
                Picasso.with(getActivity()).load(R.drawable.onboarding_1).into(mImage);
                mStart.setVisibility(View.GONE);

                return view;
            case 1:

                mTitle.setText(R.string.onboard_title_2);
                mSubTitle.setText(R.string.onboard_subtitle_2);
                Picasso.with(getActivity()).load(R.drawable.onboarding_2).into(mImage);
                mStart.setVisibility(View.GONE);

                return view;
            case 2:

                mTitle.setText(R.string.onboard_title_3);
                mSubTitle.setText(R.string.onboard_subtitle_3);
                Picasso.with(getActivity()).load(R.drawable.onboarding_3).into(mImage);
                mStart.setVisibility(View.GONE);

                return view;
            case 3:

                mTitle.setText(R.string.onboard_title_4);
                mSubTitle.setText(R.string.onboard_subtitle_4);
                Picasso.with(getActivity()).load(R.drawable.onboarding_4).into(mImage);
                mStart.setVisibility(View.VISIBLE);

                return view;

            default: return view;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IntroListener) {
            mListener = (IntroListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IntroListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.action_start)
    public void onActionStart() {
        mListener.onStartApp();
    }
}
