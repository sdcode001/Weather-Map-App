package eu.deysouvik.weathermap

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY="f4b13d071ab3b120648b8dffd8079803"
const val BASE_URL="http://api.openweathermap.org/"

interface WeatherApiInterface{
    @GET("data/2.5/weather?appid=$API_KEY")
    fun getDataByCoor(@Query("lat")lat:String,@Query("lon")lon:String): Call<WeatherData>

    @GET("data/2.5/weather?appid=$API_KEY")
    fun getDataByCity(@Query("q")q:String): Call<WeatherData>
}

object WeatherService {
    val weatherApiInterface:WeatherApiInterface

    init{
       val retrofit=Retrofit.Builder()
                    .baseUrl(BASE_URL)
                     .addConverterFactory(GsonConverterFactory.create())
                     .build()
        weatherApiInterface=retrofit.create(WeatherApiInterface::class.java)
    }
}