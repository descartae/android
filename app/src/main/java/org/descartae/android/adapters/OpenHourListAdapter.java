package org.descartae.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.descartae.android.FacilityQuery;
import org.descartae.android.R;
import org.descartae.android.view.viewholder.ListTimeViewHolder;

import java.util.Calendar;
import java.util.List;

public class OpenHourListAdapter extends RecyclerView.Adapter<ListTimeViewHolder> {

    private final Calendar todayCalendar;
    private Context mContext;
    private List<FacilityQuery.OpenHour> openHourList;

    public OpenHourListAdapter(Context context) {
        mContext = context;
        todayCalendar = (Calendar) Calendar.getInstance().clone();
    }

    @Override
    public ListTimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_item, parent, false);
        return new ListTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListTimeViewHolder holder, int position) {

        Calendar itemCalendar = (Calendar) todayCalendar.clone();
        itemCalendar.add(Calendar.DAY_OF_WEEK, position + 1);

        String[] daysWeek = mContext.getResources().getStringArray(R.array.day_of_week);

        holder.mDay.setText(daysWeek[itemCalendar.get(Calendar.DAY_OF_WEEK) - 1]);

        String time = mContext.getString(R.string.closed);

        if (openHourList != null) {
            for (FacilityQuery.OpenHour openHour : openHourList) {

                if (openHour.dayOfWeek().ordinal() == itemCalendar.get(Calendar.DAY_OF_WEEK)) {
                    time = mContext.getString(R.string.time_desc, String.valueOf(openHour.startTime()), String.valueOf(openHour.endTime()));
                }
            }
        }

        holder.mTime.setText(time);
    }

    public void setFacilityDays(List<FacilityQuery.OpenHour> openHourList) {
        this.openHourList = openHourList;
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
