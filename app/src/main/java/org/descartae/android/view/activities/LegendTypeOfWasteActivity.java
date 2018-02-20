package org.descartae.android.view.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.descartae.android.DescartaeApp;
import org.descartae.android.R;
import org.descartae.android.TypeOfWasteQuery;
import org.descartae.android.adapters.LegendWasteTypeListAdapter;
import org.descartae.android.presenter.typeofwaste.TypeOfWastePresenter;
import org.descartae.android.view.events.EventHideLoading;
import org.descartae.android.view.events.EventShowLoading;
import org.descartae.android.view.utils.SpaceDividerItemDecoration;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LegendTypeOfWasteActivity extends AppCompatActivity {

    @Inject TypeOfWastePresenter presenter;

    @Inject EventBus eventBus;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.loading)
    View mLoading;

    private LegendWasteTypeListAdapter adapter;

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_of_waste);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*
         * Init Dagger
         */
        DescartaeApp.getInstance(this)
                .getAppComponent()
                .inject(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpaceDividerItemDecoration(80));

        adapter = new LegendWasteTypeListAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setTriggerLoadingEvents(true);
        presenter.requestTypeOfWastes();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renderTypes(List<TypeOfWasteQuery.TypesOfWaste> typesOfWasteList) {
        adapter.setTypes(typesOfWasteList);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventHideLoading(EventHideLoading event) {
        mLoading.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventShowLoading(EventShowLoading event) {
        mLoading.setVisibility(View.VISIBLE);
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
