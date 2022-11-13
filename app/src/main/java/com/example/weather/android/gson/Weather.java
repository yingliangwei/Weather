package com.example.weather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String lastUpdate;
    public Location location;
    public Now now;
    @SerializedName("daily")
    public List<Daily> daily;
}
