package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** Egy kocsiállásra érkező járat. */
public class StopArrival {

    @SerializedName("jarat_id")
    public String tripId;

    @SerializedName("vonal_id")
    public String lineId;

    // A vonalszám, utána az útvonal, pl. "110 Vasútállomás - ... - Vasútállomás"
    @SerializedName("nyomvonal_nev")
    public String routeName;

    @SerializedName("erkezes")
    public String arrival;

    @SerializedName("erk_minute")
    public String minutesLeft;

    @SerializedName("megalloban")
    public String atStop;

    public boolean isAtStop() {
        return "1".equals(atStop);
    }

    /** Az útvonal neve az elején álló vonalszám nélkül. */
    public String routeWithoutLine() {
        String prefix = lineId + " ";
        return routeName != null && routeName.startsWith(prefix)
                ? routeName.substring(prefix.length())
                : routeName;
    }
}
