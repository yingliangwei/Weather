package com.lljjcoder.style.citythreelist;

import static com.lljjcoder.style.citylist.utils.CityListLoader.BUNDATA;
import static com.lljjcoder.style.citythreelist.ProvinceActivity.RESULT_DATA;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lljjcoder.style.citylist.bean.CityInfoBean;
import com.lljjcoder.style.citypickerview.R;
import com.lljjcoder.widget.RecycleViewDividerForList;

import java.util.List;

public class CityActivity extends Activity {


    private RecyclerView mCityRecyclerView;

    private CityInfoBean mProInfo = null;

    private String cityName = "";

    private CityBean cityBean = new CityBean();

    private CityBean area = new CityBean();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citylist);
        mProInfo = this.getIntent().getParcelableExtra(BUNDATA);
        initView();

        setData(mProInfo);

    }

    private void setData(CityInfoBean mProInfo) {
        if (mProInfo != null && mProInfo.getCityList().size() > 0) {
            toolbar.setTitle("" + mProInfo.getName());
            final List<CityInfoBean> cityList = mProInfo.getCityList();
            if (cityList == null) {
                return;
            }
            CityAdapter cityAdapter = new CityAdapter(CityActivity.this, cityList);
            mCityRecyclerView.setAdapter(cityAdapter);
            cityAdapter.setOnItemClickListener((view, position) -> {

                cityBean.setId(cityList.get(position).getId());
                cityBean.setName(cityList.get(position).getName());

                Intent intent = new Intent(CityActivity.this, AreaActivity.class);
                intent.putExtra(BUNDATA, cityList.get(position));
                startActivityForResult(intent, RESULT_DATA);
            });

        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());
        mCityRecyclerView = (RecyclerView) findViewById(R.id.city_recyclerview);
        mCityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCityRecyclerView.addItemDecoration(new RecycleViewDividerForList(this, LinearLayoutManager.HORIZONTAL, true));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_DATA && data != null) {
            area = data.getParcelableExtra("area");
            Intent intent = new Intent();
            intent.putExtra("city", cityBean);
            intent.putExtra("area", area);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
