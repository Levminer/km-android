package com.nje.km.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** The departures of a line within one hour. */
public class ScheduleHour {

    @SerializedName("ora")
    public String hour;

    @SerializedName("jaratok")
    public List<Departure> departures;
}
