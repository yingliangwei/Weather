package com.lljjcoder.style.citylist.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lljjcoder.Constant;
import com.lljjcoder.style.citylist.bean.CityInfoBean;
import com.lljjcoder.utils.utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 作者：liji on 2017/5/21 15:35
 * 邮箱：lijiwork@sina.com
 * QQ ：275137657
 */

public class CityListLoader {

    public static final String BUNDATA = "bundata";

    private static final List<CityInfoBean> mCityListData = new ArrayList<>();
    private static final Map<String, String> cityWeatherIdInfoBeans = new HashMap<>();
    private static List<CityInfoBean> mProListData = new ArrayList<>();

    public Map<String, String> getCityWeatherIdInfoBeans() {
        return cityWeatherIdInfoBeans;
    }

    /**
     * 解析所有的城市数据 357个数据
     */
    public List<CityInfoBean> getCityListData() {
        return mCityListData;
    }

    /**
     * 只解析省份34个数据
     */
    public List<CityInfoBean> getProListData() {
        return mProListData;
    }

    private volatile static CityListLoader instance;

    private CityListLoader() {

    }

    /**
     * 单例模式
     *
     * @return
     */
    public static CityListLoader getInstance() {
        if (instance == null) {
            synchronized (CityListLoader.class) {
                if (instance == null) {
                    instance = new CityListLoader();
                }
            }
        }
        return instance;
    }

    /**
     * 解析357个城市数据
     *
     * @param context
     */
    public void loadCityData(Context context) {

        String cityJson = utils.getJson(context, Constant.CITY_DATA);
        Type type = new TypeToken<ArrayList<CityInfoBean>>() {
        }.getType();

        //解析省份
        ArrayList<CityInfoBean> mProvinceBeanArrayList = new Gson().fromJson(cityJson, type);
        if (mProvinceBeanArrayList == null || mProvinceBeanArrayList.isEmpty()) {
            return;
        }

        for (int p = 0; p < mProvinceBeanArrayList.size(); p++) {

            //遍历每个省份
            CityInfoBean itemProvince = mProvinceBeanArrayList.get(p);

            //每个省份对应下面的市
            ArrayList<CityInfoBean> cityList = itemProvince.getCityList();

            //遍历当前省份下面城市的所有数据
            mCityListData.addAll(cityList);
        }

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
     * 解析34个省市直辖区数据
     *
     * @param context
     */
    public void loadProData(Context context) {
        String cityJson = utils.getJson(context, Constant.CITY_DATA);
        Type type = new TypeToken<ArrayList<CityInfoBean>>() {
        }.getType();

        //解析省份
        mProListData = new Gson().fromJson(cityJson, type);
    }

}
