package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** A stop of the network. One stop can have several platforms. */
public class Stop {

    @SerializedName("foldhely_id")
    public String id;

    @SerializedName("foldhely_nev")
    public String name;
}
