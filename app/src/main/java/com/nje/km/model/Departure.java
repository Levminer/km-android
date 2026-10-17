package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** Egy vonal egy járata, amely a megadott időpontban indul. */
public class Departure {

    @SerializedName("jarat_id")
    public String tripId;

    @SerializedName("indul")
    public String time;

    @SerializedName("ora")
    public String hour;

    @SerializedName("perc")
    public String minute;

    @SerializedName("alacsonypadlo")
    public String lowFloor;

    @SerializedName("wifi")
    public String wifi;

    @SerializedName("kimaradt")
    public String cancelled;

    public boolean isCancelled() {
        return "1".equals(cancelled);
    }
}
