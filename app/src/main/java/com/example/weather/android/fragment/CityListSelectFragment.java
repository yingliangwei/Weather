package com.example.weather.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.android.MainActivity;
import com.example.weather.android.WeatherActivity;
import com.example.weather.databinding.ActivityCityListSelect1Binding;
import com.github.promeg.pinyinhelper.Pinyin;
import com.lljjcoder.style.citylist.bean.CityInfoBean;
import com.lljjcoder.style.citylist.sortlistview.CharacterParser;
import com.lljjcoder.style.citylist.sortlistview.PinyinComparator;
import com.lljjcoder.style.citylist.sortlistview.SideBar;
import com.lljjcoder.style.citylist.sortlistview.SortAdapter;
import com.lljjcoder.style.citylist.sortlistview.SortModel;
import com.lljjcoder.style.citylist.utils.CityListLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityListSelectFragment extends Fragment {

    public SortAdapter adapter;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> sourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private List<CityInfoBean> cityListInfo = new ArrayList<>();
    private CityInfoBean cityInfoBean = new CityInfoBean();
    private ActivityCityListSelect1Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityCityListSelect1Binding.inflate(inflater, container, false);
        initView();
        initList();
        setCityData(CityListLoader.getInstance().getCityListData());
        return binding.getRoot();
    }

    private void setCityData(List<CityInfoBean> cityList) {
        cityListInfo = cityList;
        if (cityListInfo == null) {
            return;
        }
        int count = cityList.size();
        String[] list = new String[count];
        for (int i = 0; i < count; i++)
            list[i] = cityList.get(i).getName();

        sourceDateList.addAll(filledData(cityList));
        // 根据a-z进行排序源数据
        Collections.sort(sourceDateList, pinyinComparator);
        adapter.notifyDataSetChanged();
    }

    /**
     * 为ListView填充数据
     *
     * @return
     */
    private List<SortModel> filledData(List<CityInfoBean> cityList) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < cityList.size(); i++) {

            CityInfoBean result = cityList.get(i);

            if (result != null) {

                SortModel sortModel = new SortModel();

                String cityName = result.getName();
                //汉字转换成拼音
                if (!TextUtils.isEmpty(cityName) && cityName.length() > 0) {

//                    String pinyin = "";
//                    if (cityName.equals("重庆市")) {
//                        pinyin = "chong";
//                    }
//                    else if (cityName.equals("长沙市")) {
//                        pinyin = "chang";
//                    }
//                    else if (cityName.equals("长春市")) {
//                        pinyin = "chang";
//                    }
//                    else {
//                        pinyin = mPinYinUtils.getStringPinYin(cityName.substring(0, 1));
//                    }
//
                    String pinyin = Pinyin.toPinyin(cityName.substring(0, 1), "").toLowerCase();
                    if (!TextUtils.isEmpty(pinyin)) {

                        sortModel.setName(cityName);

                        String sortString = pinyin.substring(0, 1).toUpperCase();

                        // 正则表达式，判断首字母是否是英文字母
                        if (sortString.matches("[A-Z]")) {
                            sortModel.setSortLetters(sortString.toUpperCase());
                        } else {
                            sortModel.setSortLetters("#");
                        }
                        mSortList.add(sortModel);
                    } else {
                        Log.d("citypicker_log", "null,cityName:-> " + cityName + "       pinyin:-> " + pinyin);
                    }

                }

            }
        }
        return mSortList;
    }

    private void initList() {
        sourceDateList = new ArrayList<>();
        adapter = new SortAdapter(getContext(), sourceDateList);
        binding.countryLvcountry.setAdapter(adapter);

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        binding.sidrbar.setTextView(binding.dialog);
        //设置右侧触摸监听
        binding.sidrbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    binding.countryLvcountry.setSelection(position);
                }
            }
        });

        binding.countryLvcountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = ((SortModel) adapter.getItem(position)).getName();
                cityInfoBean = CityInfoBean.findCity(cityListInfo, cityName);
                if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.binding.drawerLayout.closeDrawers();
                    activity.binding.swipeRefresh.setRefreshing(true);
                    activity.requestWeather(cityInfoBean.getName());
                } else if (getActivity() instanceof MainActivity) {
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", cityInfoBean.getName());
                    startActivity(intent);
                }
            }
        });

        //根据输入框输入值的改变来过滤搜索
        binding.cityInputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : sourceDateList) {
                String name = sortModel.getName();
                if (name.contains(filterStr) || characterParser.getSelling(name).startsWith(filterStr)) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }


    private void initView() {

    }
}
