package com.nje.km;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.nje.km.model.PathStop;
import com.nje.km.model.VehiclePosition;

import java.util.ArrayList;
import java.util.List;

/** Stops of a trip, marked with the live position of the vehicle when it is running. */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private final List<PathStop> stops = new ArrayList<>();
    @Nullable
    private VehiclePosition vehicle;

    public void setStops(List<PathStop> newStops) {
        stops.clear();
        stops.addAll(newStops);
        notifyDataSetChanged();
    }

    public void setVehiclePosition(@Nullable VehiclePosition position) {
        vehicle = position;
        notifyDataSetChanged();
    }

    /** List index of the stop with the given order, or -1. */
    public int indexOfOrder(String order) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).order.equals(order)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_path_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PathStop stop = stops.get(position);
        int order = parseInt(stop.order);

        holder.number.setText("#" + (position + 1));
        holder.name.setText(stop.name);
        holder.arrival.setText("Érkezés: " + stop.arrival);

        boolean passed = isPassed(order);
        holder.number.setTextColor(MaterialColors.getColor(holder.number, passed
                ? androidx.appcompat.R.attr.colorPrimary
                : android.R.attr.textColorSecondary));

        boolean atThisStop = vehicle != null && vehicle.isAtStop() && order == parseInt(vehicle.lastStopOrder);
        String delay = passed ? null : delayText();
        if (atThisStop) {
            holder.status.setText("Megállóban");
            holder.status.setTextColor(MaterialColors.getColor(holder.status, androidx.appcompat.R.attr.colorPrimary));
            holder.status.setVisibility(View.VISIBLE);
        } else if (delay != null) {
            holder.status.setText(delay);
            holder.status.setTextColor(MaterialColors.getColor(holder.status, parseInt(vehicle.delaySeconds) > 0
                    ? androidx.appcompat.R.attr.colorError
                    : androidx.appcompat.R.attr.colorPrimary));
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.GONE);
        }
    }

    /** A stop is passed once the vehicle reached it and already left. */
    private boolean isPassed(int order) {
        if (vehicle == null) {
            return false;
        }
        int lastOrder = parseInt(vehicle.lastStopOrder);
        return order < lastOrder || (order == lastOrder && !vehicle.isAtStop());
    }

    @Nullable
    private String delayText() {
        if (vehicle == null) {
            return null;
        }
        int minutes = Math.round(parseInt(vehicle.delaySeconds) / 60f);
        if (minutes > 0) {
            return "+" + minutes + " perc";
        } else if (minutes < 0) {
            return minutes + " perc";
        }
        return null;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView name;
        final TextView arrival;
        final TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.stop_number);
            name = itemView.findViewById(R.id.stop_name);
            arrival = itemView.findViewById(R.id.stop_arrival);
            status = itemView.findViewById(R.id.stop_status);
        }
    }
}
