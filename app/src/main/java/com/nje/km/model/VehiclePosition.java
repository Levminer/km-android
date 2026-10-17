package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** Live position of the vehicle running a trip. */
public class VehiclePosition {

    @SerializedName("utolso_megallo_id")
    public String lastStopId;

    // Order of the last stop the vehicle reached (see PathStop.order)
    @SerializedName("utolso_megallo_sorszam")
    public String lastStopOrder;

    // "1" while the vehicle is standing at the last stop
    @SerializedName("megalloban")
    public String atStop;

    // Delay in seconds; negative if the vehicle is early
    @SerializedName("keses_sietes")
    public String delaySeconds;

    public boolean isAtStop() {
        return "1".equals(atStop);
    }
}
