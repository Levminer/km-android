package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** Egy járat útvonalának egy megállója. */
public class PathStop {

    @SerializedName("megallo_id")
    public String stopId;

    @SerializedName("megallo_nev")
    public String name;

    // A megálló helye az útvonalon, 1-től kezdve
    @SerializedName("sorrend")
    public String order;

    @SerializedName("erkezesi_ido")
    public String arrival;
}
