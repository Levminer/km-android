package com.nje.km;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nje.km.model.StopArrival;

import java.util.Objects;

/** Egy kocsiállásra érkező következő járatok. */
public class StopScheduleAdapter extends ListAdapter<StopArrival, StopScheduleAdapter.ViewHolder> {

    public interface OnArrivalClickListener {
        void onArrivalClick(StopArrival arrival);
    }

    private final OnArrivalClickListener listener;

    public StopScheduleAdapter(OnArrivalClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop_arrival, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StopArrival arrival = getItem(position);
        holder.line.setText(arrival.lineId);
        holder.route.setText(arrival.routeWithoutLine());
        holder.time.setText("Érkezés: " + arrival.arrival);
        holder.left.setText(arrival.isAtStop() ? "Megállóban" : arrival.minutesLeft + " perc múlva");
        holder.itemView.setOnClickListener(v -> listener.onArrivalClick(arrival));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView line;
        final TextView route;
        final TextView time;
        final TextView left;

        ViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.arrival_line);
            route = itemView.findViewById(R.id.arrival_route);
            time = itemView.findViewById(R.id.arrival_time);
            left = itemView.findViewById(R.id.arrival_left);
        }
    }

    private static final DiffUtil.ItemCallback<StopArrival> DIFF = new DiffUtil.ItemCallback<StopArrival>() {
        @Override
        public boolean areItemsTheSame(@NonNull StopArrival oldItem, @NonNull StopArrival newItem) {
            return oldItem.tripId.equals(newItem.tripId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull StopArrival oldItem, @NonNull StopArrival newItem) {
            return Objects.equals(oldItem.arrival, newItem.arrival)
                    && Objects.equals(oldItem.minutesLeft, newItem.minutesLeft)
                    && Objects.equals(oldItem.atStop, newItem.atStop);
        }
    };
}
