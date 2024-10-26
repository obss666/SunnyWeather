package com.sunnyweather.android.ui.adaptr

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sunnyweather.R
import com.sunnyweather.android.Utils.getContentAfterLastSpace
import com.sunnyweather.android.logic.model.PlaceWeather
import com.sunnyweather.android.logic.model.allinfo
import com.sunnyweather.android.ui.activity.ManageActivity
import com.sunnyweather.android.ui.activity.WeatherActivity

class ManageAdaptr(private val activity: ManageActivity)
    : RecyclerView.Adapter<ManageAdaptr.ViewHolder>() {

    private var managerList : List<allinfo> = emptyList()
    private val selectedIndices = mutableListOf<Int>()
    private var isSelecting = false

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener{
        val magplaceName: TextView = view.findViewById(R.id.magplaceName)
        val temperaturerng: TextView = view.findViewById(R.id.status_temperaturerng)
        val temperature: TextView = view.findViewById(R.id.temperature)
        val selectionCheckBox: CheckBox = view.findViewById(R.id.selectionCheckBox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            selectionCheckBox.isEnabled = false
        }

        override fun onLongClick(v: View): Boolean {
            if(isSelecting == false) {
                setIsSelecting(true)
//                val position = adapterPosition
//                if (selectedIndices.contains(position)) {
//                    selectedIndices.remove(position)
//                    selectionCheckBox.isChecked = false
//                } else {
//                    selectedIndices.add(position)
//                    selectionCheckBox.isChecked = true
//                }
            }
            return true
        }

        override fun onClick(v: View) {
            if (isSelecting) {
                val position = adapterPosition
                if (selectedIndices.contains(position)) {
                    selectedIndices.remove(position)
                    selectionCheckBox.isChecked = false
                } else {
                    selectedIndices.add(position)
                    selectionCheckBox.isChecked = true
                }
                notifyDataSetChanged()
            } else {
                val position = adapterPosition
                val info = managerList[position]

                val intent = Intent(v.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", info.place.location.lng)
                    putExtra("location_lat", info.place.location.lat)
                    putExtra("place_name", info.place.name)
                    putExtra("place_address", info.place.address)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_mag_item, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = managerList[position]
        val placeWeather = PlaceWeather(info.place.name, info.weather.daily.temperature[0], info.weather.realtime.temperature)
        holder.magplaceName.text = placeWeather.placename.getContentAfterLastSpace()
        holder.temperaturerng.text = "${placeWeather.temperatureRange.min.toInt()} ~ ${placeWeather.temperatureRange.max.toInt()} °C"
        holder.temperature.text = "${placeWeather.temperature .toInt()} °C"

        holder.selectionCheckBox.visibility = if (isSelecting) View.VISIBLE else View.GONE
        holder.selectionCheckBox.isChecked = selectedIndices.contains(position)
    }

    override fun getItemCount() = managerList.size

    fun updateData(newDataList: List<allinfo>) {
        managerList = newDataList
        selectedIndices.clear()
        isSelecting = false
        notifyDataSetChanged()
    }

    // 更新删除按钮的可见性
    private fun updateSelectingStatus() {
        if (isSelecting) {
            activity.findViewById<View>(R.id.isSelectLayout).visibility = View.VISIBLE
        } else {
            activity.findViewById<View>(R.id.isSelectLayout).visibility = View.GONE
        }
    }

    fun setIsSelecting(flag : Boolean) {
        isSelecting = flag
        notifyDataSetChanged()
        updateSelectingStatus()
    }

    fun getSelectedItems() = selectedIndices.map { managerList[it] }

    fun getManagerList() = managerList
}

