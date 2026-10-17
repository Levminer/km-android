package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** A single trip of a line, starting at the given time. */
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
