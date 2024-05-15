package com.example.weatherapp.Api

object ApiEndpoint {
    var BASEURL = "https://api.openweathermap.org/data/2.5/"
    var CurrentWeather = "weather?"
    var ListWeather = "forecast?"
    var Daily = "forecast/daily?"
    var UnitsAppid = "&units=metric&appid=494192e7ba12c8276e4f951df79d7838"
    var UnitsAppidDaily = "&units=metric&cnt=15&appid=494192e7ba12c8276e4f951df79d7838"
}
