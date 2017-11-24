package org.descartae.android.view.facility;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;

import org.descartae.android.view.facility.adapter.FacilityListAdapter;
import org.descartae.android.view.networking.NetworkingConstants;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;

import javax.annotation.Nonnull;

public class FacilityFragment extends Fragment {

    private OnListFacilitiesListener mListener;
    private FacilityListAdapter facilityListAdapter;

    public FacilityFragment() {
    }

    public static FacilityFragment newInstance() {
        FacilityFragment fragment = new FacilityFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl(NetworkingConstants.BASE_URL)
            .build();
        FacilityQuery facilityQuery = FacilityQuery.builder().build();
        apolloClient.query(facilityQuery).enqueue(new ApolloCall.Callback<FacilityQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<FacilityQuery.Data> dataResponse) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        facilityListAdapter.setCenters(dataResponse.data().centers());
                        facilityListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                if (e != null && e.getMessage() != null)
                    Log.e("ApolloFacilityQuery", e.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            facilityListAdapter = new FacilityListAdapter(getActivity(), mListener);
            recyclerView.setAdapter(facilityListAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFacilitiesListener) {
            mListener = (OnListFacilitiesListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFacilitiesListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFacilitiesListener {
        void onListFacilityInteraction(FacilityQuery.Center center);
    }
}
