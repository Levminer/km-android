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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nje.km.api.ApiClient;
import com.nje.km.model.Line;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LinesFragment extends Fragment {

    private final LinesAdapter adapter = new LinesAdapter();

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private TextView message;
    private Call<List<Line>> call;

    public LinesFragment() {
        super(R.layout.fragment_lines);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progress = view.findViewById(R.id.progress);
        message = view.findViewById(R.id.message);

        RecyclerView list = view.findViewById(R.id.lines_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadLines(false));

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.lines_menu, menu);
                menu.findItem(R.id.action_date).setTitle(ServiceDate.displayFormat());
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_date) {
                    showDatePicker();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        if (adapter.getItemCount() == 0) {
            loadLines(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) {
            call.cancel();
        }
    }

    private void loadLines(boolean showSpinner) {
        if (call != null) {
            call.cancel();
        }
        message.setVisibility(View.GONE);
        if (showSpinner) {
            adapter.submitList(null);
            progress.setVisibility(View.VISIBLE);
        }

        call = ApiClient.get().getLines(ServiceDate.apiFormat());
        call.enqueue(new Callback<List<Line>>() {
            @Override
            public void onResponse(@NonNull Call<List<Line>> call, @NonNull Response<List<Line>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showLines(response.body());
                } else {
                    showMessage("Nem sikerült betölteni a vonalakat (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Line>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showMessage("Nem sikerült betölteni a vonalakat.\nEllenőrizd az internetkapcsolatot!");
                }
            }
        });
    }

    private void showLines(List<Line> lines) {
        stopLoading();
        adapter.submitList(lines);
        if (lines.isEmpty()) {
            showMessage("Nincs elérhető vonal ezen a napon");
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

    private void showDatePicker() {
        // MaterialDatePicker works with UTC midnight timestamps
        Calendar current = ServiceDate.get();
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));

        Calendar todayUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar today = Calendar.getInstance();
        todayUtc.clear();
        todayUtc.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Nap")
                .setSelection(utc.getTimeInMillis())
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setStart(todayUtc.getTimeInMillis())
                        .setValidator(DateValidatorPointForward.from(todayUtc.getTimeInMillis()))
                        .build())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar picked = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            picked.setTimeInMillis(selection);
            ServiceDate.set(picked.get(Calendar.YEAR), picked.get(Calendar.MONTH), picked.get(Calendar.DAY_OF_MONTH));
            requireActivity().invalidateOptionsMenu();
            loadLines(true);
        });
        picker.show(getChildFragmentManager(), "date_picker");
    }
}
