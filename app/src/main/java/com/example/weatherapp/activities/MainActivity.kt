package com.example.weatherapp.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.Api.ApiEndpoint
import com.example.weatherapp.Model.ModelMain
import com.example.weatherapp.R
import com.example.weatherapp.adapters.MainAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.json.JSONException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {

    private var lat: Double? = null
    private var lng: Double? = null
    private var today: String? = null
    private var mProgressBar: ProgressDialog? = null
    private var mainAdapter: MainAdapter? = null
    private val modelMain: MutableList<ModelMain> = ArrayList()
    var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var tvHumidity: TextView? = null
    var tvWindSpeed: TextView? = null
    var tvTemperature: TextView? = null
    var tvCityName: TextView? = null
    var tvWeather: TextView? = null
    var tvDate: TextView? = null
    var tvDescription: TextView? = null
    var iconTemp: LottieAnimationView? = null
    private var fabNextDays: ExtendedFloatingActionButton? = null
    var rvListWeather: RecyclerView? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSearchBar()

        modelMain.clear()
        mainAdapter?.notifyDataSetChanged()

        tvHumidity = findViewById(R.id.tvHumidity)
        tvDescription = findViewById(R.id.tvDescription)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCityName = findViewById(R.id.tvCityName)
        tvWeather = findViewById(R.id.tvWeather)
        tvDate = findViewById(R.id.tvDate)
        rvListWeather = findViewById(R.id.rvListWeather)
        fabNextDays = findViewById(R.id.fabNextDays) as ExtendedFloatingActionButton
        iconTemp = findViewById(R.id.iconTemp)

        //set Transparent Statusbar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        //set Permission
        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        val dateNow = Calendar.getInstance().time
        today = DateFormat.format("EEE", dateNow) as String

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please Wait...")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Displaying data...")

        mainAdapter = MainAdapter(modelMain)

        rvListWeather?.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        rvListWeather?.setHasFixedSize(true)
        rvListWeather?.setAdapter(mainAdapter)

        fabNextDays!!.setOnClickListener {

            //Fragment CODE Once You Pay for the Service Just Uncomment The Code Below It will show the Data of 15 Days
//            val transaction = supportFragmentManager.beginTransaction()
//            val fragmentNextDays = FragmentNextDays()
//            transaction.add(R.id.fragment_container, fragmentNextDays)
//            transaction.commit()

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)

            val alertDialog = dialogBuilder.create()
            val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
            messageTextView.text =
                "I Have added the Coding part to Find the Data for Next 15 Days but It's Paid Service from API, So Could'nt Include it in the UI but you can Find the code in Backend"

            Handler().postDelayed({
                alertDialog.dismiss()
            }, 5000)

            val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
            closeButton.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        //method get LatLong & get Date
        getToday()
        getLatlong()
    }

    private fun setupSearchBar() {
        val searchBar = findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Implement logic to fetch and display suggestions based on entered text
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val cityName = searchBar.text.toString().trim()
                if (cityName.isNotEmpty()) {
                    fetchWeatherByCityName(cityName)
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a city name", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun fetchWeatherByCityName(cityName: String) {
        mProgressBar?.show()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            val url = "${ApiEndpoint.BASEURL}${ApiEndpoint.CurrentWeather}q=${cityName}${ApiEndpoint.UnitsAppid}"

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        mProgressBar?.dismiss()
                        val jsonArrayOne = response.getJSONArray("weather")
                        val jsonObjectOne = jsonArrayOne.getJSONObject(0)
                        val jsonObjectTwo = response.getJSONObject("main")
                        val jsonObjectThree = response.getJSONObject("wind")
                        val strWeather = jsonObjectOne.getString("main")
                        val strDescWeather = jsonObjectOne.getString("description")
                        val strWindSpeed = jsonObjectThree.getString("speed")
                        val strHumidity = jsonObjectTwo.getString("humidity")
                        val strName = response.getString("name")
                        val strTemperature = jsonObjectTwo.getDouble("temp")

                        // Handle weather data and update UI accordingly
                        // For example:
                        // tvCityName?.text = strName
                        // tvTemperature?.text = String.format(Locale.getDefault(), "%.0f°C", strTemperature)

                    } catch (e: JSONException) {
                        mProgressBar?.dismiss()
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to fetch weather data!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                { error ->
                    mProgressBar?.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                })

            Volley.newRequestQueue(this@MainActivity).add(jsonObjectRequest)
        } else {
            mProgressBar?.dismiss()
            Toast.makeText(
                this@MainActivity,
                "Please Turn On Internet Connection",
                Toast.LENGTH_LONG
            ).show()
        }
    }



    private fun getToday() {
        val date = Calendar.getInstance().time
        val Date = DateFormat.format("d MMM yyyy", date) as String
        val formatDate = "$today, $Date"
        tvDate?.text = formatDate
    }

    private fun getLatlong() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                115
            )
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 20000, 0f, this)
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                onLocationChanged(location)
            } else {
                // Use default location if last known location is not available
                val defaultLat = 37.7749 // Default latitude
                val defaultLng = -122.4194 // Default longitude
                val defaultLocation = Location("Default")
                defaultLocation.latitude = defaultLat
                defaultLocation.longitude = defaultLng
                onLocationChanged(defaultLocation)
            }
        } else {
            Toast.makeText(this, "No location provider available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude

        //Clear the previus data
        modelMain.clear()
        mainAdapter?.notifyDataSetChanged()

        //method get Data Weather
        getCurrentWeather()
        getListWeather()
    }

    private fun getCurrentWeather() {
        mProgressBar?.show()

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            val url =
                "${ApiEndpoint.BASEURL}${ApiEndpoint.CurrentWeather}lat=${lat}&lon=${lng}${ApiEndpoint.UnitsAppid}"
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        mProgressBar?.dismiss()
                        val jsonArrayOne = response.getJSONArray("weather")
                        val jsonObjectOne = jsonArrayOne.getJSONObject(0)
                        val jsonObjectTwo = response.getJSONObject("main")
                        val jsonObjectThree = response.getJSONObject("wind")
                        val strWeather = jsonObjectOne.getString("main")
                        val strDescWeather = jsonObjectOne.getString("description")
                        val strWindSpeed = jsonObjectThree.getString("speed")
                        val strHumidity = jsonObjectTwo.getString("humidity")
                        val strName = response.getString("name")
                        val strTemperature = jsonObjectTwo.getDouble("temp")

                        when (strDescWeather) {
                            "broken clouds" -> {
                                iconTemp?.setAnimation(R.raw.broken_clouds)
                                tvWeather?.text = "Broken Clouds"
                            }

                            "light rain" -> {
                                iconTemp?.setAnimation(R.raw.light_rain)
                                tvWeather?.text = "Light Rain"
                            }

                            "haze" -> {
                                iconTemp?.setAnimation(R.raw.broken_clouds)
                                tvWeather?.text = "Haze"
                            }

                            "overcast clouds" -> {
                                iconTemp?.setAnimation(R.raw.overcast_clouds)
                                tvWeather?.text = "Overcast Clouds"
                            }

                            "moderate rain" -> {
                                iconTemp?.setAnimation(R.raw.moderate_rain)
                                tvWeather?.text = "Moderate Rain"
                            }

                            "few clouds" -> {
                                iconTemp?.setAnimation(R.raw.few_clouds)
                                tvWeather?.text = "Few Clouds"
                            }

                            "heavy intensity rain" -> {
                                iconTemp?.setAnimation(R.raw.heavy_intentsity)
                                tvWeather?.text = "Heavy Rain"
                            }

                            "clear sky" -> {
                                iconTemp?.setAnimation(R.raw.clear_sky)
                                tvWeather?.text = "Clear Sky"
                            }

                            "scattered clouds" -> {
                                iconTemp?.setAnimation(R.raw.scattered_clouds)
                                tvWeather?.text = "Scattered Clouds"
                            }

                            "smoke" -> {
                                iconTemp?.setAnimation(R.raw.few_clouds)
                                tvWeather?.text = "Scattered Clouds"
                            }

                            else -> {
                                iconTemp?.setAnimation(R.raw.unknown)
                                tvWeather?.text = strWeather
                            }
                        }

                        tvCityName?.text = strName
                        tvTemperature?.text =
                            String.format(Locale.getDefault(), "%.0f°C", strTemperature)
                        tvDescription?.text = "Description: $strWeather"
                        tvWindSpeed?.text = "Wind Speed $strWindSpeed km/h"
                        tvHumidity?.text = "Humidity $strHumidity%"
                    } catch (e: JSONException) {
                        mProgressBar?.dismiss()
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to display header data!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                { error ->
                    mProgressBar?.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            Volley.newRequestQueue(this@MainActivity).add(jsonObjectRequest)
        } else {
            mProgressBar?.dismiss()
            Toast.makeText(
                this@MainActivity,
                "Please Turn On Internet Connection",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun getListWeather() {
        mProgressBar?.show()
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val url =
                "${ApiEndpoint.BASEURL}${ApiEndpoint.ListWeather}lat=$lat&lon=$lng${ApiEndpoint.UnitsAppid}"

            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    try {
                        mProgressBar?.dismiss()
                        val jsonArray = response.getJSONArray("list")
                        for (i in 0..6) {
                            val dataApi = ModelMain()
                            val objectList = jsonArray.getJSONObject(i)
                            val jsonObjectOne = objectList.getJSONObject("main")
                            val jsonArrayOne = objectList.getJSONArray("weather")
                            val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                            var timeNow = objectList.getString("dt_txt")
                            val formatDefault =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val formatTimeCustom = SimpleDateFormat("kk:mm", Locale.getDefault())

                            try {
                                val timesFormat = formatDefault.parse(timeNow)
                                timeNow = formatTimeCustom.format(timesFormat)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                            dataApi.timeNow = timeNow
                            dataApi.strWeather = jsonObjectTwo.getString("main")
                            dataApi.currentTemp = jsonObjectOne.getDouble("temp")
                            dataApi.descWeather = jsonObjectTwo.getString("description")
                            dataApi.tempMin = jsonObjectOne.getDouble("temp_min")
                            dataApi.tempMax = jsonObjectOne.getDouble("temp_max")
                            modelMain.add(dataApi)
                        }
                        mainAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        mProgressBar?.dismiss()
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to display data!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                { error ->
                    mProgressBar?.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            Volley.newRequestQueue(this@MainActivity).add(jsonObjectRequest)
        } else {
            mProgressBar?.dismiss()
            Toast.makeText(
                this@MainActivity,
                "Please Turn On Internet Connection",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatlong()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}