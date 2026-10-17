package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

public class Line {

    @SerializedName("vonal_id")
    public String id;

    @SerializedName("vonal_nev")
    public String name;

    // "1" if the line is circular (single direction), "0" if it has two directions
    @SerializedName("korjarat")
    public String circular;

    public boolean isCircular() {
        return "1".equals(circular);
    }
}
