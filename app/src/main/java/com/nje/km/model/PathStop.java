package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** A stop on the route of a trip. */
public class PathStop {

    @SerializedName("megallo_id")
    public String stopId;

    @SerializedName("megallo_nev")
    public String name;

    // Position of the stop on the route, starting from 1
    @SerializedName("sorrend")
    public String order;

    @SerializedName("erkezesi_ido")
    public String arrival;
}
