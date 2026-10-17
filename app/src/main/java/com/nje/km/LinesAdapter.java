package com.nje.km;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nje.km.model.Line;

public class LinesAdapter extends ListAdapter<Line, LinesAdapter.ViewHolder> {

    public interface OnLineClickListener {
        void onLineClick(Line line);
    }

    private final OnLineClickListener listener;

    public LinesAdapter(OnLineClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Line line = getItem(position);
        holder.number.setText(line.id);
        holder.name.setText(line.name);
        holder.itemView.setOnClickListener(v -> listener.onLineClick(line));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.line_number);
            name = itemView.findViewById(R.id.line_name);
        }
    }

    private static final DiffUtil.ItemCallback<Line> DIFF = new DiffUtil.ItemCallback<Line>() {
        @Override
        public boolean areItemsTheSame(@NonNull Line oldItem, @NonNull Line newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Line oldItem, @NonNull Line newItem) {
            return oldItem.name.equals(newItem.name) && oldItem.circular.equals(newItem.circular);
        }
    };
}
