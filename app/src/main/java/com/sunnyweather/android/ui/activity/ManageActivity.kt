package com.sunnyweather.android.ui.activity

import android.app.ActionBar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunnyweather.databinding.ActivityManageBinding
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

            }
        }

        binding.finishBtn.setOnClickListener {
            viewModel.refreshWeatherDataList()
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