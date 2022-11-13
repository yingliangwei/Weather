package com.example.weather.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.example.weather.R;
import com.example.weather.android.gson.Daily;
import com.example.weather.android.gson.Weather;
import com.example.weather.android.service.AutoUpdateService;
import com.example.weather.android.util.HttpUtil;
import com.example.weather.android.util.Utility;
import com.example.weather.android.util.Utils;
import com.example.weather.databinding.ActivityWeatherBinding;
import com.lljjcoder.style.citylist.utils.CityListLoader;
import com.miraclegarden.library.app.MiracleGardenActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends MiracleGardenActivity<ActivityWeatherBinding> {
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        initWeather();
        //刷新数据控件
        binding.swipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));
        //侧滑点击
        binding.title.navButton.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
    }

    private void initWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Log.d("缓存数据", weatherString);
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if (weather != null) {
                mWeatherId = String.valueOf(weather.location.name);
            }
            if (weather != null) {
                showWeatherInfo(weather);
            }
            String bingPic = prefs.getString("bing_pic", null);
            if (bingPic != null) {
                Glide.with(WeatherActivity.this).load(bingPic).into(binding.bingPicImg);
            } else {
                loadBingPic();
            }
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            binding.weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
    }

    /**
     * 沉浸式状态栏
     */
    private void initStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String id) {
        String weatherUrl = "https://weather.cma.cn/api/weather/view?stationid=" + CityListLoader.getInstance().getWeatherIdName(id);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        mWeatherId = "" + weather.location.name;
                        showWeatherInfo(weather);
                        Utils.sendNotification(getApplicationContext(), id, weather.now.temperature + "℃" + " " + weather.daily.get(0).dayText);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                    binding.swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
        loadBingPic();
    }


    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "https://api.no0a.cn/api/bing/0";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(bingPic);
                    int status = jsonObject.getInt("status");
                    if (status == 1) {
                        JSONObject bing = jsonObject.getJSONObject("bing");
                        String url = bing.getString("url");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("bing_pic", url);
                        editor.apply();
                        runOnUiThread(() -> Glide.with(WeatherActivity.this).load(url).into(binding.bingPicImg));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    @SuppressLint("SetTextI18n")
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.location.name;
        String updateTime = weather.lastUpdate.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        Daily daily = weather.daily.get(0);
        String weatherInfo = "天气:" + daily.dayText + " " + weather.now.windDirection + ":" + weather.now.windScale;
        //图标
        binding.now.image.setImageDrawable(drawable(daily.dayText));
        //标题
        binding.title.titleCity.setText(cityName);
        //天气时间
        binding.title.titleUpdateTime.setText(updateTime);
        //温度
        binding.now.degreeText.setText(degree);
        //天气风向
        binding.now.weatherInfoText.setText(weatherInfo);
        binding.forecast.forecastLayout.removeAllViews();

        String comfort = "湿度：" + weather.now.humidity + "%";
        String carWash = "气压：" + weather.now.pressure + "hPa";

        String sport = weather.now.windDirection + " " + weather.now.windScale;
        findViewById(R.id.comfort_text);
        binding.suggestion.comfortText.setText(comfort);
        binding.suggestion.carWashText.setText(carWash);
        binding.suggestion.sportText.setText(sport);
        binding.weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

        if (weather.daily == null) {
            return;
        }
        //7天天气
        for (Daily forecast : weather.daily) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.dayText);
            imageView.setImageDrawable(drawable(forecast.dayText));
            maxText.setText(forecast.high + "℃");
            minText.setText(forecast.low + "℃");
            binding.forecast.forecastLayout.addView(view);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable drawable(String dayText) {
        String[] strings = new String[]{
                "晴",
                "多云",
                "阴",
                "大风",
                "小雨",
                "中雨",
                "大雨",
                "暴雨",
                "雷阵雨",
                "雨夹雪",
                "小雪",
                "中雪",
                "大雪",
                "暴雪",
                "冰雹",
                "轻度雾霾",
                "中度雾霾",
                "重度雾霾",
                "雾",
                "浮尘"};
        int[] ints = new int[]{
                R.drawable.ic_clear_day,
                R.drawable.ic_partly_cloud_day,
                R.drawable.ic_cloudy,
                R.drawable.ic_cloudy,
                R.drawable.ic_light_rain, R.drawable.ic_moderate_rain, R.drawable.ic_heavy_rain, R.drawable.ic_storm_rain,
                R.drawable.ic_thunder_shower, R.drawable.ic_sleet, R.drawable.ic_light_snow,
                R.drawable.ic_moderate_snow, R.drawable.ic_heavy_snow, R.drawable.ic_heavy_snow,
                R.drawable.ic_hail, R.drawable.ic_light_haze, R.drawable.ic_moderate_haze, R.drawable.ic_heavy_haze,
                R.drawable.ic_fog, R.drawable.ic_fog};
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(dayText)) {
                return getDrawable(ints[i]);
            }
        }
        return getResources().getDrawable(R.drawable.ic_clear_day);
    }

}
