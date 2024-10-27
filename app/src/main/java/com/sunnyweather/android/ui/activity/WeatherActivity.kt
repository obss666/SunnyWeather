package com.sunnyweather.android.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityWeatherBinding
import com.google.android.material.snackbar.Snackbar
import com.sunnyweather.android.Utils.LogUtil
import com.sunnyweather.android.Utils.getContentAfterLastSpace
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.base.BaseBindingActivity
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class WeatherActivity : BaseBindingActivity<ActivityWeatherBinding>() {

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    private lateinit var startForResultLauncher: ActivityResultLauncher<Intent>

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityWeatherBinding {
        return ActivityWeatherBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        val name = intent.getStringExtra("place_name") ?: ""
        val lng = intent.getStringExtra("location_lng") ?: ""
        val lat = intent.getStringExtra("location_lat") ?: ""
        val address =  intent.getStringExtra("place_address") ?: ""
        viewModel.place = Place(name,Location(lng,lat),address)

        viewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        findViewById<Button>(R.id.navBtn).setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        startForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val name = data.getStringExtra("place_name") ?: ""
                    val lng = data.getStringExtra("location_lng") ?: ""
                    val lat = data.getStringExtra("location_lat") ?: ""
                    val address =  data.getStringExtra("place_address") ?: ""
                    LogUtil.d("返回",name + "\n" + lng + "\n" + lat + "\n" + address)
                    viewModel.place = Place(name,Location(lng,lat),address)
                }
            }
        }

        findViewById<Button>(R.id.manBtn).setOnClickListener{
            val intent = Intent(this, ManageActivity::class.java)
            startForResultLauncher.launch(intent)
        }

        binding.addPlace.setOnClickListener{ view ->
            Snackbar.make(view, "已添加到城市列表", Snackbar.LENGTH_SHORT).show()
            viewModel.addPlace(viewModel.place)
            refreshWeather()
        }

    }

    override fun onResume() {
        super.onResume()
        refreshWeather()
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.place.location.lng, viewModel.place.location.lat)
        binding.swipeRefresh.isRefreshing = true
        if(!viewModel.isPlaceExists(viewModel.place)) binding.addPlace.visibility = View.VISIBLE
        else binding.addPlace.visibility = View.GONE
    }

    private fun showWeatherInfo(weather: Weather) {
        findViewById<TextView>(R.id.placeName_D).text = viewModel.place.name.getContentAfterLastSpace()
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} °C"
        findViewById<TextView>(R.id.currentTemp).text = currentTempText
        findViewById<TextView>(R.id.currentSky) .text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        findViewById<TextView>(R.id.currentAQI).text = currentPM25Text
        findViewById<RelativeLayout>(R.id.nowLayout).setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        val forecastLayout = findViewById<LinearLayout>(R.id.forecastLayout)
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        findViewById<TextView>(R.id.coldRiskText).text = lifeIndex.coldRisk[0].desc
        findViewById<TextView>(R.id.dressingText).text = lifeIndex.dressing[0].desc
        findViewById<TextView>(R.id.ultravioletText).text = lifeIndex.ultraviolet[0].desc
        findViewById<TextView>(R.id.carWashingText).text = lifeIndex.carWashing[0].desc
        findViewById<ScrollView>(R.id.weatherLayout).visibility = View.VISIBLE
    }
}