package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

public class Line {

    @SerializedName("vonal_id")
    public String id;

    @SerializedName("vonal_nev")
    public String name;

    // "1", ha a vonal körjárat (egy irány), "0", ha két iránya van
    @SerializedName("korjarat")
    public String circular;

    public boolean isCircular() {
        return "1".equals(circular);
    }
}
