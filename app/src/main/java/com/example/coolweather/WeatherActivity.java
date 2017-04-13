package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/13.
 */

public class WeatherActivity extends AppCompatActivity {


    private ScrollView weatherLayout;
    private TextView titileCity;
    private  TextView titleUpdateTime;
    private TextView degreeText;
    private  TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private  TextView pm25Text;
    private  TextView comfortText;
    private  TextView carWashText;
    private  TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swiperefresglayout;
    private String mWeaherId;
    public DrawerLayout drawerLayout;
    private Button navButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        swiperefresglayout =(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swiperefresglayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout =(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton =(Button)findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        if (Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        weatherLayout =(ScrollView)findViewById(R.id.weather_layout);
        titileCity =(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText =(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        if (weatherString!= null)
        {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeaherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else
        {
            mWeaherId= getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeaherId);

        }
        swiperefresglayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeaherId );
            }
        });

        bingPicImg =(ImageView)findViewById(R.id.bing_pic_img);
        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic != null)
        {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else
        {
                loadBingPic();
        }
    }

    private void loadBingPic()
    {
        String requestBingPic ="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttoRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final  String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void requestWeather(final  String weatherId)
    {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=f74683ede07942ce84358451ac3c5b51";
        HttpUtil.sendOkhttoRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swiperefresglayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final  Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else
                        {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swiperefresglayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }
    private  void showWeatherInfo(Weather weather)
    {
        String cityname = weather.basic.cityName;
        String updataTime = weather.basic.update.updateTime.split("")[1];
        String degree = weather.now.temperature+"°C";
        String weatherInfo= weather.now.more.info;
        titileCity.setText(cityname);
        titleUpdateTime.setText(updataTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList
             ) {
            View v = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText =(TextView)v.findViewById(R.id.date_text);
            TextView infoText =(TextView)v.findViewById(R.id.info_text);
            TextView maxText =(TextView)v.findViewById(R.id.max_text);
            TextView minText =(TextView)v.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(v);
        }
            if (weather.aqi != null)
            {
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

        String comfort ="舒适度:"+weather.suggestion.comfort.info;
        String carwash ="洗车指数:"+weather.suggestion.carWash.info;
        String sport ="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        if (weather != null && "ok".equals(weather.status))
        {
            Intent intent = new Intent(this,AutoUpdateService.class);
            startService(intent);
        }else
        {
            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
}
