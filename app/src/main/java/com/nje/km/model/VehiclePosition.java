package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** A járaton közlekedő jármű élő pozíciója. */
public class VehiclePosition {

    @SerializedName("utolso_megallo_id")
    public String lastStopId;

    // A jármű által utoljára elért megálló sorszáma (lásd PathStop.order)
    @SerializedName("utolso_megallo_sorszam")
    public String lastStopOrder;

    // "1", amíg a jármű az utolsó megállóban áll
    @SerializedName("megalloban")
    public String atStop;

    // Késés másodpercben; negatív, ha a jármű siet
    @SerializedName("keses_sietes")
    public String delaySeconds;

    public boolean isAtStop() {
        return "1".equals(atStop);
    }
}
