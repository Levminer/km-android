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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nje.km.api.ApiClient;
import com.nje.km.model.Stop;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** All stops of the network, with search and A → Z / Z → A sorting. */
public class StopsFragment extends Fragment {

    private static final Locale HUNGARIAN = Locale.forLanguageTag("hu-HU");

    private final StopsAdapter adapter = new StopsAdapter(stop -> {
        // TODO: open the platforms of the stop
    });

    private final List<Stop> allStops = new ArrayList<>();
    private String query = "";
    private boolean ascending = true;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView list;
    private ProgressBar progress;
    private TextView message;
    private Call<List<Stop>> call;

    public StopsFragment() {
        super(R.layout.fragment_stops);
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

        swipeRefresh.setOnRefreshListener(() -> loadStops(false));

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.stops_menu, menu);
                menu.findItem(ascending ? R.id.action_sort_asc : R.id.action_sort_desc).setChecked(true);
                setUpSearch(menu.findItem(R.id.action_search));
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_sort_asc || id == R.id.action_sort_desc) {
                    ascending = id == R.id.action_sort_asc;
                    menuItem.setChecked(true);
                    showStops(true);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        if (allStops.isEmpty()) {
            loadStops(true);
        } else {
            showStops(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle("Megállók", null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) {
            call.cancel();
        }
    }

    private void setUpSearch(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Keresés megállók között");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Keep the previous search when coming back to this page
        String previousQuery = query;
        if (!previousQuery.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(previousQuery, false);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                query = text;
                showStops(true);
                return true;
            }
        });
    }

    private void loadStops(boolean showSpinner) {
        if (call != null) {
            call.cancel();
        }
        message.setVisibility(View.GONE);
        if (showSpinner) {
            adapter.submitList(null);
            progress.setVisibility(View.VISIBLE);
        }

        call = ApiClient.get().getStops(ServiceDate.apiFormat());
        call.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(@NonNull Call<List<Stop>> call, @NonNull Response<List<Stop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stopLoading();
                    allStops.clear();
                    allStops.addAll(response.body());
                    showStops(false);
                } else {
                    showMessage("Nem sikerült betölteni a megállókat (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Stop>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showMessage("Nem sikerült betölteni a megállókat.\nEllenőrizd az internetkapcsolatot!");
                }
            }
        });
    }

    /** Filters and sorts the downloaded stops by the current search and sort order. */
    private void showStops(boolean scrollToTop) {
        if (message == null || (allStops.isEmpty() && progress.getVisibility() == View.VISIBLE)) {
            return;
        }

        String search = normalize(query.trim());
        List<Stop> shown = new ArrayList<>();
        for (Stop stop : allStops) {
            if (search.isEmpty() || normalize(stop.name).contains(search)) {
                shown.add(stop);
            }
        }

        Collator collator = Collator.getInstance(HUNGARIAN);
        Comparator<Stop> byName = (a, b) -> collator.compare(a.name, b.name);
        Collections.sort(shown, ascending ? byName : byName.reversed());

        adapter.submitList(shown, () -> {
            if (scrollToTop) {
                list.scrollToPosition(0);
            }
        });

        if (allStops.isEmpty()) {
            showMessage("Nincs elérhető megálló");
        } else if (shown.isEmpty()) {
            showMessage("Nincs találat");
        } else {
            message.setVisibility(View.GONE);
        }
    }

    /** Lowercase without accents, so "allomas" also finds "Állomás". */
    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(HUNGARIAN);
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
