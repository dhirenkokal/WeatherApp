package com.example.weatherapp.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.Api.ApiEndpoint
import com.example.weatherapp.Model.ModelNextDay
import com.example.weatherapp.R
import com.example.weatherapp.adapters.NextDayAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpStatus
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentNextDays : DialogFragment(), LocationListener {

    private var lat: Double? = null
    private var lng: Double? = null
    private var nextDayAdapter: NextDayAdapter? = null
    private var rvListWeather: RecyclerView? = null
    private var fabClose: FloatingActionButton? = null
    private var modelNextDays: MutableList<ModelNextDay> = ArrayList()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_next_day, container, false)

        nextDayAdapter = NextDayAdapter(requireActivity(), modelNextDays)
        rvListWeather = rootView.findViewById(R.id.rvListWeather)
        rvListWeather?.layoutManager = LinearLayoutManager(activity)
        rvListWeather?.setHasFixedSize(true)
        rvListWeather?.adapter = nextDayAdapter

        fabClose = rootView.findViewById(R.id.fabClose)
        fabClose?.setOnClickListener {
            dismiss()
        }

        //method get LatLong
        getLatLong()

        return rootView
    }

    @SuppressLint("MissingPermission")
    private fun getLatLong() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider.toString())
        if (location != null) {
            onLocationChanged(location)
        } else {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 20000, 0f, this)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude
        Handler().postDelayed({
            //method get Data Weather
            getListWeather()
        }, 3000)
    }

    private fun getListWeather() {
        val url =
            "${ApiEndpoint.BASEURL}${ApiEndpoint.Daily}lat=${lat}&lon=${lng}${ApiEndpoint.UnitsAppidDaily}"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val main = response.getJSONObject("main")
                    val weatherArray = response.getJSONArray("weather")
                    val weather = weatherArray.getJSONObject(0)
                    val dt = response.optLong("dt")

                    val temp = main.optDouble("temp")
                    val minTemp = main.optDouble("temp_min")
                    val maxTemp = main.optDouble("temp_max")
                    val description = weather.optString("description")
                    val date = if (dt != 0L) {
                        val formatDate = SimpleDateFormat("d MMM yy", Locale.getDefault())
                        formatDate.format(Date(dt * 1000))
                    } else {
                        ""
                    }

                    val dataApi = ModelNextDay().apply {
                        nameDate = date
                        descWeather = description
                        tempMin = minTemp
                        tempMax = maxTemp
                    }
                    modelNextDays.add(dataApi)
                    nextDayAdapter?.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to fetch data!", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            { error ->
                error.printStackTrace()
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    Toast.makeText(
                        requireContext(),
                        "Unauthorized: Please check your API credentials",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch data! Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }


    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

}
