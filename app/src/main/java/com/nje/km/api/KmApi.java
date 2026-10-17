package com.nje.km.api;

import com.nje.km.model.Line;
import com.nje.km.model.PathStop;
import com.nje.km.model.ScheduleHour;
import com.nje.km.model.VehiclePosition;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface KmApi {

    @GET("tracks/{date}")
    Call<List<Line>> getLines(@Path("date") String date);

    // az irány 1 vagy 2; a körjáratoknak csak 1-es iránya van
    @GET("routes/{date}/{lineId}/{direction}")
    Call<List<ScheduleHour>> getLineSchedule(
            @Path("date") String date,
            @Path("lineId") String lineId,
            @Path("direction") int direction);

    @GET("path/{date}/{tripId}")
    Call<List<PathStop>> getTripPath(@Path("date") String date, @Path("tripId") String tripId);

    // Üres lista, ha a járat éppen nem közlekedik
    @GET("vehicleposition/{date}/{tripId}")
    Call<List<VehiclePosition>> getVehiclePosition(@Path("date") String date, @Path("tripId") String tripId);
}
