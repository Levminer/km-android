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
import com.nje.km.model.Departure;
import com.nje.km.model.PathStop;
import com.nje.km.model.VehiclePosition;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Egy járat útvonala: a megállói az érkezési időkkel és a jármű élő pozíciójával. */
public class TripFragment extends Fragment {

    private static final String ARG_TRIP_ID = "trip_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    private static final long POSITION_REFRESH_MS = 30_000;

    private final TripAdapter adapter = new TripAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshPosition = new Runnable() {
        @Override
        public void run() {
            loadVehiclePosition();
            handler.postDelayed(this, POSITION_REFRESH_MS);
        }
    };

    private String tripId;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView list;
    private ProgressBar progress;
    private TextView message;
    private Call<List<PathStop>> pathCall;
    private Call<List<VehiclePosition>> positionCall;
    @Nullable
    private VehiclePosition vehicle;
    private boolean scrolledToVehicle;

    public static TripFragment newInstance(String lineId, String lineName, Departure departure) {
        Bundle args = new Bundle();
        args.putString(ARG_TRIP_ID, departure.tripId);
        args.putString(ARG_TITLE, lineId + " – " + departure.hour + ":" + departure.minute);
        args.putString(ARG_SUBTITLE, lineName);

        TripFragment fragment = new TripFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public TripFragment() {
        super(R.layout.fragment_trip);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripId = requireArguments().getString(ARG_TRIP_ID);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progress = view.findViewById(R.id.progress);
        message = view.findViewById(R.id.message);

        list = view.findViewById(R.id.stops_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            loadPath(false);
            loadVehiclePosition();
        });

        if (adapter.getItemCount() == 0) {
            loadPath(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = requireArguments();
        ((MainActivity) requireActivity()).setToolbarTitle(args.getString(ARG_TITLE), args.getString(ARG_SUBTITLE));
        // Amíg az oldal látható, frissen tartja a jármű pozícióját
        handler.post(refreshPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pathCall != null) {
            pathCall.cancel();
        }
        if (positionCall != null) {
            positionCall.cancel();
        }
    }

    private void loadPath(boolean showSpinner) {
        if (pathCall != null) {
            pathCall.cancel();
        }
        message.setVisibility(View.GONE);
        if (showSpinner) {
            adapter.setStops(Collections.emptyList());
            progress.setVisibility(View.VISIBLE);
        }

        pathCall = ApiClient.get().getTripPath(ServiceDate.apiFormat(), tripId);
        pathCall.enqueue(new Callback<List<PathStop>>() {
            @Override
            public void onResponse(@NonNull Call<List<PathStop>> call, @NonNull Response<List<PathStop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showPath(response.body());
                } else {
                    showMessage("Nem sikerült betölteni az útvonalat (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PathStop>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showMessage("Nem sikerült betölteni az útvonalat.\nEllenőrizd az internetkapcsolatot!");
                }
            }
        });
    }

    /** A hibákat figyelmen kívül hagyjuk: az útvonal élő pozíció nélkül is hasznos. */
    private void loadVehiclePosition() {
        if (positionCall != null) {
            positionCall.cancel();
        }
        positionCall = ApiClient.get().getVehiclePosition(ServiceDate.apiFormat(), tripId);
        positionCall.enqueue(new Callback<List<VehiclePosition>>() {
            @Override
            public void onResponse(@NonNull Call<List<VehiclePosition>> call, @NonNull Response<List<VehiclePosition>> response) {
                List<VehiclePosition> positions = response.body();
                VehiclePosition position = response.isSuccessful() && positions != null && !positions.isEmpty()
                        ? positions.get(0)
                        : null;
                vehicle = position;
                adapter.setVehiclePosition(position);
                scrollToVehicle();
            }

            @Override
            public void onFailure(@NonNull Call<List<VehiclePosition>> call, @NonNull Throwable t) {
            }
        });
    }

    private void showPath(List<PathStop> stops) {
        stopLoading();
        adapter.setStops(stops);
        scrollToVehicle();
        if (stops.isEmpty()) {
            showMessage("Nincs elérhető útvonal\nNézz vissza holnap!");
        }
    }

    /** Az oldal megnyitásakor egyszer a jármű által utoljára elért megállóhoz görget. */
    private void scrollToVehicle() {
        if (scrolledToVehicle || vehicle == null || list == null) {
            return;
        }
        // Az útvonal és a pozíció külön töltődik be, ezért mindkettőt meg kell várni
        int index = adapter.indexOfOrder(vehicle.lastStopOrder);
        if (index < 0) {
            return;
        }
        scrolledToVehicle = true;
        ((LinearLayoutManager) list.getLayoutManager()).scrollToPositionWithOffset(Math.max(index - 1, 0), 0);
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
