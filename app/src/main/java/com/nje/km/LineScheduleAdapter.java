package com.nje.km;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nje.km.model.Departure;
import com.nje.km.model.ScheduleHour;

import java.util.ArrayList;
import java.util.List;

/** Egy vonal indulásai, minden óra előtt egy fejléc sorral. */
public class LineScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HOUR = 0;
    private static final int TYPE_DEPARTURE = 1;

    public interface OnDepartureClickListener {
        void onDepartureClick(Departure departure);
    }

    // Vagy String (óra fejléc), vagy Departure
    private final List<Object> rows = new ArrayList<>();
    private final OnDepartureClickListener listener;

    public LineScheduleAdapter(OnDepartureClickListener listener) {
        this.listener = listener;
    }

    public void setHours(List<ScheduleHour> hours) {
        rows.clear();
        for (ScheduleHour hour : hours) {
            rows.add(hour.hour + ":00");
            rows.addAll(hour.departures);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof String ? TYPE_HOUR : TYPE_DEPARTURE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HOUR) {
            return new HourViewHolder(inflater.inflate(R.layout.item_schedule_hour, parent, false));
        }
        return new DepartureViewHolder(inflater.inflate(R.layout.item_departure, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HourViewHolder) {
            ((HourViewHolder) holder).hour.setText((String) row);
        } else {
            Departure departure = (Departure) row;
            DepartureViewHolder departureHolder = (DepartureViewHolder) holder;
            departureHolder.time.setText(departure.hour + ":" + departure.minute);

            boolean cancelled = departure.isCancelled();
            int flags = departureHolder.time.getPaintFlags();
            departureHolder.time.setPaintFlags(cancelled
                    ? flags | Paint.STRIKE_THRU_TEXT_FLAG
                    : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            departureHolder.status.setVisibility(cancelled ? View.VISIBLE : View.GONE);
            departureHolder.itemView.setOnClickListener(v -> listener.onDepartureClick(departure));
        }
    }

    static class HourViewHolder extends RecyclerView.ViewHolder {
        final TextView hour;

        HourViewHolder(View itemView) {
            super(itemView);
            hour = itemView.findViewById(R.id.hour);
        }
    }

    static class DepartureViewHolder extends RecyclerView.ViewHolder {
        final TextView time;
        final TextView status;

        DepartureViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.departure_time);
            status = itemView.findViewById(R.id.departure_status);
        }
    }
}
