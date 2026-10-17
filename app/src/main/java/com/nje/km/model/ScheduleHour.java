package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Egy vonal indulásai egy órán belül. */
public class ScheduleHour {

    @SerializedName("ora")
    public String hour;

    @SerializedName("jaratok")
    public List<Departure> departures;
}
