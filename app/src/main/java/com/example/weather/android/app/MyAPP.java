package com.example.weather.android.app;

import com.lljjcoder.style.citylist.utils.CityListLoader;

import org.litepal.LitePalApplication;

public class MyAPP extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CityListLoader.getInstance().loadCityData(this);
        CityListLoader.getInstance().loadWeatherId();
    }
}
