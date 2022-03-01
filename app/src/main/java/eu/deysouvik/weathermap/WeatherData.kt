package eu.deysouvik.weathermap

import java.io.Serializable

data class WeatherData(
    val coord:coordinate,
    val weather:List<dataweather>,
    val base:String,
    val main:maindata,
    val wind:windData,
    val clouds:cloudData,
    val dt:Long,
    val sys:sysData,
    val name:String,
    val cod:Int
):Serializable
data class coordinate(
   val lon:Double,
   val lat:Double
):Serializable
data class dataweather(
    val id:Int,
    val main:String,
    val description:String,
    val icon:String
):Serializable
data class maindata(
    val temp:Double,
    val feels_like:Double,
    val temp_min:Double,
    val temp_max:Double,
    val pressure:Int,
    val humidity:Int
):Serializable
data class windData(
    val speed:Double,
    val deg:Int
):Serializable
data class cloudData(
    val all:Int
):Serializable
data class sysData(
    val type:Int,
    val message:Double,
    val country:String,
    val sunrise:Long,
    val sunset:Long
):Serializable

