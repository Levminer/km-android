package com.nje.km;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nje.km.api.ApiClient;
import com.nje.km.model.Line;
import com.nje.km.model.ScheduleHour;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Departures of one line, grouped by hour. */
public class LineScheduleFragment extends Fragment {

    private static final String ARG_LINE_ID = "line_id";
    private static final String ARG_LINE_NAME = "line_name";
    private static final String ARG_CIRCULAR = "circular";
    private static final String STATE_DIRECTION = "direction";

    private final LineScheduleAdapter adapter = new LineScheduleAdapter(departure ->
            ((MainActivity) requireActivity()).openPage(TripFragment.newInstance(this.lineId, this.lineName, departure)));

    private String lineId;
    private String lineName;
    private boolean circular;
    private int direction = 1;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private TextView message;
    private Call<List<ScheduleHour>> call;

    public static LineScheduleFragment newInstance(Line line) {
        Bundle args = new Bundle();
        args.putString(ARG_LINE_ID, line.id);
        args.putString(ARG_LINE_NAME, line.name);
        args.putBoolean(ARG_CIRCULAR, line.isCircular());

        LineScheduleFragment fragment = new LineScheduleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public LineScheduleFragment() {
        super(R.layout.fragment_line_schedule);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        lineId = args.getString(ARG_LINE_ID);
        lineName = args.getString(ARG_LINE_NAME);
        circular = args.getBoolean(ARG_CIRCULAR);
        if (savedInstanceState != null) {
            direction = savedInstanceState.getInt(STATE_DIRECTION, 1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progress = view.findViewById(R.id.progress);
        message = view.findViewById(R.id.message);

        RecyclerView list = view.findViewById(R.id.schedule_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadSchedule(false));

        // Only lines that are not circular can be viewed in the other direction
        if (!circular) {
            requireActivity().addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.line_schedule_menu, menu);
                    menu.findItem(R.id.action_direction).setIcon(direction == 1
                            ? R.drawable.ic_arrow_forward
                            : R.drawable.ic_arrow_back);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_direction) {
                        direction = direction == 1 ? 2 : 1;
                        requireActivity().invalidateOptionsMenu();
                        loadSchedule(true);
                        return true;
                    }
                    return false;
                }
            }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        }

        if (adapter.getItemCount() == 0) {
            loadSchedule(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle(lineId, lineName);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_DIRECTION, direction);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) {
            call.cancel();
        }
    }

    private void loadSchedule(boolean showSpinner) {
        if (call != null) {
            call.cancel();
        }
        message.setVisibility(View.GONE);
        if (showSpinner) {
            adapter.setHours(Collections.emptyList());
            progress.setVisibility(View.VISIBLE);
        }

        call = ApiClient.get().getLineSchedule(ServiceDate.apiFormat(), lineId, direction);
        call.enqueue(new Callback<List<ScheduleHour>>() {
            @Override
            public void onResponse(@NonNull Call<List<ScheduleHour>> call, @NonNull Response<List<ScheduleHour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSchedule(response.body());
                } else {
                    showMessage("Nem sikerült betölteni a menetrendet (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ScheduleHour>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showMessage("Nem sikerült betölteni a menetrendet.\nEllenőrizd az internetkapcsolatot!");
                }
            }
        });
    }

    private void showSchedule(List<ScheduleHour> hours) {
        stopLoading();
        adapter.setHours(sortFromCurrentHour(hours));
        if (hours.isEmpty()) {
            showMessage("Nincs elérhető járat\nNézz vissza holnap!");
        }
    }

    /**
     * For today, start the list at the current hour and move the hours that are
     * already over to the end. Other days are shown in chronological order.
     */
    private static List<ScheduleHour> sortFromCurrentHour(List<ScheduleHour> hours) {
        List<ScheduleHour> sorted = new ArrayList<>(hours);
        int currentHour = ServiceDate.isToday()
                ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                : 0;
        Collections.sort(sorted, Comparator.comparingInt(
                hour -> Math.floorMod(parseHour(hour.hour) - currentHour, 24)));
        return sorted;
    }

    private static int parseHour(String hour) {
        try {
            return Integer.parseInt(hour);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showMessage(String text) {
        stopLoading();
        message.setText(text);
        message.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        progress.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }
}
