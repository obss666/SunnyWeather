package com.sunnyweather.android.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunnyweather.databinding.ActivityManageBinding
import com.sunnyweather.android.Utils.getContentAfterLastSpace
import com.sunnyweather.android.ui.SunnyWeatherApplication
import com.sunnyweather.android.ui.adaptr.ManageAdaptr
import com.sunnyweather.android.ui.base.BaseBindingActivity
import com.sunnyweather.android.viewmodel.WeatherViewModel


class ManageActivity : BaseBindingActivity<ActivityManageBinding>() {
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityManageBinding {
        return ActivityManageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.magToolbar)

        val layoutManager = LinearLayoutManager(this)
        val adapter = ManageAdaptr(this)

        viewModel.weatherDataListLiveData.observe(this) { result ->
            val info = result.getOrNull()
            if (info != null) {
                adapter.updateData(info)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.refreshWeatherDataList()
        binding.magRecyclerView.layoutManager = layoutManager
        binding.magRecyclerView.adapter = adapter

        binding.deleteBtn.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if(selectedItems.isEmpty()) {
                Toast.makeText(this,"请勾选要删除的城市", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.deletePlace(selectedItems.map { it.place })
                viewModel.refreshWeatherDataList()
                adapter.setIsSelecting(false)
            }
        }

        binding.remindBtn.setOnClickListener{
            val selectedItems = adapter.getSelectedItems()
            if(selectedItems.size == 1) {
                val place = selectedItems[0].place
                viewModel.savePlace(place)
                Toast.makeText(this,"已设置${place.name.getContentAfterLastSpace()}作为提醒城市",
                    Toast.LENGTH_SHORT).show()
                adapter.setIsSelecting(false)
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                    putExtra("place_address", place.address)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this,"请选择一个提醒城市", Toast.LENGTH_SHORT).show()
            }
        }

        binding.finishBtn.setOnClickListener {
            adapter.setIsSelecting(false)
        }

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}