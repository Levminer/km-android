package com.nje.km;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nje.km.api.ApiClient;
import com.nje.km.model.Platform;
import com.nje.km.model.StopArrival;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Egy megálló egy kocsiállására érkező következő járatok. */
public class StopScheduleFragment extends Fragment {

    private static final String ARG_PLATFORM_ID = "platform_id";
    private static final String ARG_PLATFORM_NAME = "platform_name";

    // Az API a forgalmi napot 04:00-tól számolja
    private static final String DAY_START = "04:00";
    // Amíg az oldal nyitva van, frissen tartja az "N perc múlva" visszaszámlálást
    private static final long REFRESH_MS = 30_000;

    private final StopScheduleAdapter adapter = new StopScheduleAdapter(arrival ->
            ((MainActivity) requireActivity()).openPage(
                    TripFragment.newInstance(arrival.tripId, arrival.lineId, arrival.routeWithoutLine())));
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            loadSchedule();
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    private String platformId;
    private String platformName;
    private boolean loaded;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private TextView message;
    private Call<List<StopArrival>> call;

    public static StopScheduleFragment newInstance(Platform platform) {
        Bundle args = new Bundle();
        args.putString(ARG_PLATFORM_ID, platform.id);
        args.putString(ARG_PLATFORM_NAME, platform.name);

        StopScheduleFragment fragment = new StopScheduleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StopScheduleFragment() {
        super(R.layout.fragment_stop_schedule);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        platformId = args.getString(ARG_PLATFORM_ID);
        platformName = args.getString(ARG_PLATFORM_NAME);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progress = view.findViewById(R.id.progress);
        message = view.findViewById(R.id.message);

        RecyclerView list = view.findViewById(R.id.arrivals_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadSchedule());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle(platformName, null);
        if (!loaded) {
            progress.setVisibility(View.VISIBLE);
        }
        handler.post(refresh);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refresh);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) {
            call.cancel();
        }
    }

    private void loadSchedule() {
        if (call != null) {
            call.cancel();
        }

        call = ApiClient.get().getStopSchedule(ServiceDate.apiFormat(), DAY_START, platformId);
        call.enqueue(new Callback<List<StopArrival>>() {
            @Override
            public void onResponse(@NonNull Call<List<StopArrival>> call, @NonNull Response<List<StopArrival>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSchedule(response.body());
                } else if (!loaded) {
                    showMessage("Nem sikerült betölteni a járatokat (" + response.code() + ")");
                } else {
                    stopLoading();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<StopArrival>> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                // Ha csak egy háttérbeli frissítés hiúsult meg, a legutóbbi lista marad látható
                if (!loaded) {
                    showMessage("Nem sikerült betölteni a járatokat.\nEllenőrizd az internetkapcsolatot!");
                } else {
                    stopLoading();
                }
            }
        });
    }

    private void showSchedule(List<StopArrival> arrivals) {
        loaded = true;
        stopLoading();
        adapter.submitList(arrivals);
        if (arrivals.isEmpty()) {
            showMessage("Nincs elérhető járat\nNézz vissza holnap!");
        } else {
            message.setVisibility(View.GONE);
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
