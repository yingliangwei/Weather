package com.example.weather.android.gson;

import java.io.Serializable;

public class Daily implements Serializable {
    /**
     * date : 2022/11/13
     * high : 10
     * dayText : 晴
     * dayCode : 0
     * dayWindDirection : 南风
     * dayWindScale : 3~4级
     * low : -1
     * nightText : 晴
     * nightCode : 0
     * nightWindDirection : 东风
     * nightWindScale : 微风
     */

    public String date;
    public int high;
    public String dayText;
    public int dayCode;
    public String dayWindDirection;
    public String dayWindScale;
    public int low;
    public String nightText;
    public int nightCode;
    public String nightWindDirection;
    public String nightWindScale;
}
