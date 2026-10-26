package com.nje.km;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.MaterialColors;
import com.nje.km.api.ApiClient;
import com.nje.km.model.Platform;
import com.nje.km.model.Stop;

import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Egy megálló kocsiállásainak térképe OpenStreetMap csempékkel. */
public class StopMapFragment extends Fragment {

    private static final String ARG_STOP_ID = "stop_id";
    private static final String ARG_STOP_NAME = "stop_name";

    // Elég közeli nagyítás ahhoz, hogy a megálló kocsiállásai megkülönböztethetők legyenek
    private static final double STOP_ZOOM = 18.0;
    // Kecskemét belvárosa, ez látszik, amíg a kocsiállások betöltődnek
    private static final GeoPoint CITY_CENTRE = new GeoPoint(46.9062, 19.6913);

    private String stopId;
    private String stopName;

    private MapView map;
    private ProgressBar progress;
    private TextView message;
    private Call<List<Platform>> call;

    public static StopMapFragment newInstance(Stop stop) {
        Bundle args = new Bundle();
        args.putString(ARG_STOP_ID, stop.id);
        args.putString(ARG_STOP_NAME, stop.name);

        StopMapFragment fragment = new StopMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StopMapFragment() {
        super(R.layout.fragment_stop_map);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        stopId = args.getString(ARG_STOP_ID);
        stopName = args.getString(ARG_STOP_NAME);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Az OpenStreetMap user agentet kér, a csempék pedig az alkalmazás saját gyorsítótár mappájába kerülnek
        IConfigurationProvider config = org.osmdroid.config.Configuration.getInstance();
        config.setUserAgentValue(context.getPackageName());
        File cache = new File(context.getCacheDir(), "osmdroid");
        config.setOsmdroidBasePath(cache);
        config.setOsmdroidTileCache(new File(cache, "tiles"));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progress = view.findViewById(R.id.progress);
        message = view.findViewById(R.id.message);

        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTilesScaledToDpi(true);
        map.getController().setZoom(14.0);
        map.getController().setCenter(CITY_CENTRE);
        CopyrightOverlay copyright = new CopyrightOverlay(requireContext());
        map.getOverlays().add(copyright);
        if (isNightMode()) {
            // Nincsenek sötét OpenStreetMap csempék, ezért a világosakat invertáljuk
            map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
            copyright.setTextColor(MaterialColors.getColor(map, com.google.android.material.R.attr.colorOnSurface));
        }

        loadPlatforms();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle(stopName, null);
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) {
            call.cancel();
        }
        map.onDetach();
    }

    private void loadPlatforms() {
        message.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        call = ApiClient.get().getPlatforms(ServiceDate.apiFormat(), stopId);
        call.enqueue(new Callback<List<Platform>>() {
            @Override
            public void onResponse(@NonNull Call<List<Platform>> call, @NonNull Response<List<Platform>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showPlatforms(response.body());
                } else {
                    showMessage("Nem sikerült betölteni a kocsiállásokat (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Platform>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showMessage("Nem sikerült betölteni a kocsiállásokat.\nEllenőrizd az internetkapcsolatot!");
                }
            }
        });
    }

    private void showPlatforms(List<Platform> platforms) {
        progress.setVisibility(View.GONE);

        Drawable pin = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_pin).mutate();
        pin.setTint(MaterialColors.getColor(map, androidx.appcompat.R.attr.colorPrimary));

        List<GeoPoint> points = new ArrayList<>();
        for (Platform platform : platforms) {
            GeoPoint point = toGeoPoint(platform);
            if (point == null) {
                continue;
            }
            points.add(point);

            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setIcon(pin);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(platform.name);
            marker.setSnippet("Kocsiállás: " + platform.number);
            map.getOverlays().add(marker);
        }
        map.invalidate();

        if (points.isEmpty()) {
            showMessage("Nincs elérhető kocsiállás");
            return;
        }
        map.getController().setZoom(STOP_ZOOM);
        map.getController().setCenter(BoundingBox.fromGeoPoints(points).getCenterWithDateLine());
    }

    @Nullable
    private static GeoPoint toGeoPoint(Platform platform) {
        try {
            return new GeoPoint(Double.parseDouble(platform.latitude), Double.parseDouble(platform.longitude));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private boolean isNightMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void showMessage(String text) {
        progress.setVisibility(View.GONE);
        message.setText(text);
        message.setVisibility(View.VISIBLE);
    }
}
