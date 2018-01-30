package org.descartae.android.view.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.descartae.android.R;
import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.adapters.LegendWasteTypeListAdapter;
import org.descartae.android.networking.NetworkingConstants;
import org.descartae.android.view.utils.SimpleDividerItemDecoration;
import org.descartae.android.view.utils.SpaceDividerItemDecoration;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LegendTypeOfWasteActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.loading)
    View mLoading;

    private LegendWasteTypeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_of_waste);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpaceDividerItemDecoration(80));

        adapter = new LegendWasteTypeListAdapter(this);
        recyclerView.setAdapter(adapter);

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(NetworkingConstants.BASE_URL)
                .build();
        TypeOfWasteQuery typeOfWasteQuery = TypeOfWasteQuery.builder().build();

        mLoading.setVisibility(View.VISIBLE);

        apolloClient.query(typeOfWasteQuery).enqueue(new ApolloCall.Callback<TypeOfWasteQuery.Data>() {

            @Override
            public void onResponse(@Nonnull final Response<TypeOfWasteQuery.Data> dataResponse) {

                if (dataResponse == null) return;
                if (dataResponse.data() == null) return;

                runOnUiThread(() -> {
                    adapter.setTypes(dataResponse.data().typesOfWaste());
                    adapter.notifyDataSetChanged();

                    mLoading.setVisibility(View.GONE);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
