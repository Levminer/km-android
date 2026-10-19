package com.nje.km;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nje.km.model.Stop;

public class StopsAdapter extends ListAdapter<Stop, StopsAdapter.ViewHolder> {

    public interface OnStopClickListener {
        void onStopClick(Stop stop);
    }

    private final OnStopClickListener listener;

    public StopsAdapter(OnStopClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stop stop = getItem(position);
        holder.name.setText(stop.name);
        holder.itemView.setOnClickListener(v -> listener.onStopClick(stop));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.stop_name);
        }
    }

    private static final DiffUtil.ItemCallback<Stop> DIFF = new DiffUtil.ItemCallback<Stop>() {
        @Override
        public boolean areItemsTheSame(@NonNull Stop oldItem, @NonNull Stop newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Stop oldItem, @NonNull Stop newItem) {
            return oldItem.name.equals(newItem.name);
        }
    };
}
