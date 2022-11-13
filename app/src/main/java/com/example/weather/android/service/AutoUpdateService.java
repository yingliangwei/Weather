package com.example.weather.android.service;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.weather.android.gson.Weather;
import com.example.weather.android.util.HttpUtil;
import com.example.weather.android.util.Utility;
import com.example.weather.android.util.Utils;
import com.lljjcoder.style.citylist.utils.CityListLoader;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private final Map<String, String> cityWeatherIdInfoBeans = new HashMap<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cityWeatherIdInfoBeans.putAll(CityListLoader.getInstance().getCityWeatherIdInfoBeans());
        loadWeatherId();
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        }
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 加载地域转换天气id;
     */
    public void loadWeatherId() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://weather.cma.cn/api/map/weather/1?t=" + System.currentTimeMillis()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONArray city = data.getJSONArray("city");
                    for (int i = 0; i < city.length(); i++) {
                        JSONArray values = city.getJSONArray(i);
                        cityWeatherIdInfoBeans.put(values.getString(1), values.getString(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @param name 地域昵称获取id
     * @return
     */
    public String getWeatherIdName(String name) {
        Set<String> key = cityWeatherIdInfoBeans.keySet();
        for (String values : key) {
            String f = cityWeatherIdInfoBeans.get(values);
            if (name.contains(values)) {
                return f;
            }
        }
        return cityWeatherIdInfoBeans.get(name);
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather() {
        if (cityWeatherIdInfoBeans.size() == 0) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = String.valueOf(weather.location.name);
            String weatherUrl = "https://weather.cma.cn/api/weather/view?stationid=" + getWeatherIdName(weatherId);
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Log.d("数据", responseText);
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weather", responseText);
                    editor.apply();

                    Utils.sendNotification(getApplicationContext(),weatherId, weather.now.temperature + "℃" + " " + weather.daily.get(0).dayText);
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 更新必应每日一图
     */
    private void updateBingPic() {
        String requestBingPic = "https://api.no0a.cn/api/bing/0";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bingPic = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(bingPic);
                    int status = jsonObject.getInt("status");
                    if (status == 1) {
                        JSONObject bing = jsonObject.getJSONObject("bing");
                        String url = bing.getString("url");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("bing_pic", url);
                        editor.apply();
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

}
