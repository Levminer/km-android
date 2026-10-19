package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

/** A hálózat egy megállója. Egy megállónak több kocsiállása is lehet. */
public class Stop {

    @SerializedName("foldhely_id")
    public String id;

    @SerializedName("foldhely_nev")
    public String name;
}
