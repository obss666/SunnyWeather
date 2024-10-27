package com.sunnyweather.android.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityManageBinding
import com.sunnyweather.android.Utils.getContentAfterLastSpace
import com.sunnyweather.android.logic.model.PlaceWithWeather
import com.sunnyweather.android.ui.adaptr.ManageAdaptr
import com.sunnyweather.android.ui.base.BaseBindingActivity
import com.sunnyweather.android.viewmodel.ManageViewModel

class ManageActivity : BaseBindingActivity<ActivityManageBinding>() {
    val viewModel by lazy { ViewModelProvider(this).get(ManageViewModel::class.java) }

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityManageBinding {
        return ActivityManageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.magToolbar)

        // 设置返回箭头可点击并关联点击事件处理
        if (getSupportActionBar()!= null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.magToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.placeList = mutableListOf()

        val layoutManager = LinearLayoutManager(this)
        val adapter = ManageAdaptr(this, viewModel.placeList)
        binding.magRecyclerView.layoutManager = layoutManager
        binding.magRecyclerView.adapter = adapter

        viewModel.weatherDataListLiveData.observe(this) { result ->
            val info = result.getOrNull()
            if (info != null) {
                viewModel.placeList.clear()
                viewModel.placeList.addAll(info)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        binding.deleteBtn.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if(selectedItems.isEmpty()) {
                Toast.makeText(this,"请勾选要删除的城市", Toast.LENGTH_SHORT).show()
            } else {
                val placeList = selectedItems.map { it.place }
                for (place in placeList) viewModel.deletePlace(place)
                refreshWeatherDataList()
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
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this,"请选择一个提醒城市", Toast.LENGTH_SHORT).show()
            }
        }

        binding.finishBtn.setOnClickListener {
            adapter.setIsSelecting(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_item -> {
                val builder = AlertDialog.Builder(this)

                builder.setTitle("帮助中心")
                builder.setMessage(
                    "1. 本应用的主要功能是提供天气查询服务，您可以在首页输入城市名称获取对应天气信息。\n" +
                            "2. 城市列表中长按可进入选择状态，设置提醒城市，下次打开app即是该城市的数据。\n" +
                            "3. 若城市没添加到城市列表中，可在天气页面滑到最下方，点击右下角按钮添加。 \n" +
                            "4. 其他功能待开发。 \n"
                )
                val dialog = builder.create()
                dialog.show()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manage, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshWeatherDataList()
    }

    fun refreshWeatherDataList() {
        viewModel.refreshWeatherDataList()
    }
}