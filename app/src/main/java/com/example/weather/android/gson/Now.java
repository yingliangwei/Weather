package com.example.weather.android.gson;

import java.io.Serializable;

public class Now implements Serializable {
    /**
     * precipitation : 9999
     * temperature : -2.2
     * pressure : 917
     * humidity : 67
     * windDirection : 西南风
     * windDirectionDegree : 206
     * windSpeed : 4.3
     * windScale : 3级
     */
    public int precipitation;
    public double temperature;
    public int pressure;
    public int humidity;
    public String windDirection;
    public int windDirectionDegree;
    public double windSpeed;
    public String windScale;
}
