package com.example.weather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
                Log.d("数据", responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        mWeatherId = "" + weather.location.name;
                        showWeatherInfo(weather);

                        Utils.sendNotification(getApplicationContext(),id, weather.now.temperature + "℃" + " " + weather.daily.get(0).dayText);

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
     *
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
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.location.name;
        String updateTime = weather.lastUpdate.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.windDirection;
        binding.title.titleCity.setText(cityName);

        binding.title.titleUpdateTime.setText(updateTime);
        binding.now.degreeText.setText(degree);
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
        for (Daily forecast : weather.daily) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.dayText);
            maxText.setText(String.valueOf(forecast.high));
            minText.setText(String.valueOf(forecast.low));
            binding.forecast.forecastLayout.addView(view);
        }
    }

}
