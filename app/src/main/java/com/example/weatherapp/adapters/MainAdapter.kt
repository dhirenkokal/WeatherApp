package com.example.weatherapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherapp.Model.ModelMain
import com.example.weatherapp.R
import java.util.*

class MainAdapter(private val items: List<ModelMain>) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        val randomColor = getRandomColor()
        holder.cvListWeather.setCardBackgroundColor(randomColor)
        holder.tvNameDay.text = data.timeNow
        holder.tvDescription.text = data.strWeather
        holder.tvTemp.text = String.format(Locale.getDefault(), "%.0f°C", data.currentTemp)
        holder.tvTempMin.text = String.format(Locale.getDefault(), "%.0f°C", data.tempMin)
        holder.tvTempMax.text = String.format(Locale.getDefault(), "%.0f°C", data.tempMax)

        data.descWeather?.let { setWeatherAnimation(holder.iconTemp, it) }
    }

    private fun getRandomColor(): Int {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color.rgb(r, g, b)
    }

    private fun setWeatherAnimation(iconTemp: LottieAnimationView, descWeather: String) {
        val animationResId = when (descWeather) {
            "broken clouds" -> R.raw.broken_clouds
            "light rain" -> R.raw.light_rain
            "overcast clouds" -> R.raw.overcast_clouds
            "moderate rain" -> R.raw.moderate_rain
            "few clouds" -> R.raw.few_clouds
            "heavy intensity rain" -> R.raw.heavy_intentsity
            "clear sky" -> R.raw.clear_sky
            "scattered clouds" -> R.raw.scattered_clouds
            else -> R.raw.unknown
        }
        iconTemp.setAnimation(animationResId)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListWeather: CardView
        var tvNameDay: TextView
        var tvTemp: TextView
        var tvTempMin: TextView
        var tvDescription: TextView
        var tvTempMax: TextView
        var iconTemp: LottieAnimationView

        init {
            cvListWeather = itemView.findViewById(R.id.cvListWeather)
            tvNameDay = itemView.findViewById(R.id.tvNameDay)
            tvTemp = itemView.findViewById(R.id.tvTemp)
            tvTempMin = itemView.findViewById(R.id.tvTempMin)
            tvTempMax = itemView.findViewById(R.id.tvTempMax)
            iconTemp = itemView.findViewById(R.id.iconTemp)
            tvDescription = itemView.findViewById(R.id.tvDescription)
        }
    }
}
