package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** Egy megálló egy kocsiállása a GPS pozíciójával. */
public class Platform {

    @SerializedName("kocsiall_id")
    public String id;

    @SerializedName("kocsiall_nev")
    public String name;

    @SerializedName("kocsiall_szam")
    public String number;

    @SerializedName("kocsiall_gps_x")
    public String longitude;

    @SerializedName("kocsiall_gps_y")
    public String latitude;
}
