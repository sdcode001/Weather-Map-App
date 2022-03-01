package eu.deysouvik.weathermap

import android.annotation.SuppressLint
import android.content.*
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.icu.util.TimeZone.*
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    var lat:Double=0.0
    var lon:Double=0.0
    var GPSenabled:Boolean=false
    var Wdata:WeatherData?=null
    var City:String=""
    lateinit var msharedPreference:SharedPreferences
    lateinit var msharedPrefTime:SharedPreferences
    lateinit var locationProviderClint:FusedLocationProviderClient
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        msharedPreference=getSharedPreferences(Constants.Weather_DATA, MODE_PRIVATE)
        msharedPrefTime=getSharedPreferences(Constants.Responce_Time, MODE_PRIVATE)
        locationProviderClint=LocationServices.getFusedLocationProviderClient(this)

        if(!isLocationEnabled()){
            val gpsdialog=AlertDialog.Builder(this)
            gpsdialog.setIcon(R.drawable.gpsoff_icon)
            gpsdialog.setMessage("Your Location Provider is turned Off! Please turn it on in Settings")
            gpsdialog.setPositiveButton("Go to Settings",DialogInterface.OnClickListener { dialog, which ->
                val intent_to_settings=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent_to_settings)
            })
            gpsdialog.setNegativeButton("No",DialogInterface.OnClickListener { dialog, which ->  })
            gpsdialog.show()

        }
        else{
             GPSenabled=true

        }

       showOnUI()



    }




    private fun showOnUI(){
        val WData_string=msharedPreference.getString(Constants.Weather_DATA," ")
        if(!WData_string!!.isNullOrEmpty()&&WData_string!=" "){
            Wdata=Gson().fromJson(WData_string,WeatherData::class.java)
            lat=Wdata!!.coord.lat
            lon=Wdata!!.coord.lon
            for(w in Wdata!!.weather){
                tv_main.text=w.main
                tv_main_description.text=w.description
               when(w.id){
                   in 200..299->iv_main.setImageResource(R.drawable.storm)
                   in 300..399->iv_main.setImageResource(R.drawable.rain)
                   in 500..599->iv_main.setImageResource(R.drawable.dreezel2)
                   in 600..699->iv_main.setImageResource(R.drawable.snowflake)
                   in 801..899->iv_main.setImageResource(R.drawable.cloud)
                   in 700..799->iv_main.setImageResource(R.drawable.mist2)
                   800->iv_main.setImageResource(R.drawable.sunny)
               }
            }

            tv_min.text="${to_celcious(Wdata!!.main.temp_min).toString()} o'C"
            tv_max.text="${to_celcious(Wdata!!.main.temp_max).toString()} o'C"
            tv_temp.text="${to_celcious(Wdata!!.main.temp)} o'C"
            tv_humidity.text="Humidity ${Wdata!!.main.humidity}%"
            tv_speed.text=speed(Wdata!!.wind.speed).toString()
            tv_name.text=Wdata!!.name
            tv_country.text=Wdata!!.sys.country
            tv_sunrise_time.text=unixTime(Wdata!!.sys.sunrise)
            tv_sunset_time.text=unixTime(Wdata!!.sys.sunset)
            tv_date.text=unixDate(Wdata!!.dt)
            tv_time.text=msharedPrefTime.getString(Constants.Responce_Time," ")

        }

    }


    fun button(view:View){
        if(permissionCheckManager()&&GPSenabled){
            setMyLocationData()
        }

    }


    private fun setWeatherData(flag:String){
        if(flag=="coor"){
            val apiCall=WeatherService.weatherApiInterface.getDataByCoor(lat.toString(),lon.toString())
            apiCall.enqueue(object : Callback<WeatherData>{
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if(response.isSuccessful){
                        val data=response.body()
                        val Wdata_string=Gson().toJson(data)
                        val editor=msharedPreference.edit()
                        val t_editor=msharedPrefTime.edit()
                        editor.putString(Constants.Weather_DATA,Wdata_string)
                        t_editor.putString(Constants.Responce_Time,currtime())
                        editor.apply()
                        t_editor.apply()
                        showOnUI()
                        et_name.text.clear()
                    }
                    else{
                        Toast.makeText(this@MainActivity, "${response.code()}", Toast.LENGTH_SHORT).show()}
                }
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Connection Failure $t", Toast.LENGTH_SHORT).show()
                }

            })
        }
        else{
            val apiCall=WeatherService.weatherApiInterface.getDataByCity(City)
            apiCall.enqueue(object : Callback<WeatherData>{
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if(response.isSuccessful){
                        val data=response.body()
                        val Wdata_string=Gson().toJson(data)
                        val editor=msharedPreference.edit()
                        val t_editor=msharedPrefTime.edit()
                        editor.putString(Constants.Weather_DATA,Wdata_string)
                        t_editor.putString(Constants.Responce_Time,currtime())
                        editor.apply()
                        t_editor.apply()
                        lat=data!!.coord.lat
                        lon=data!!.coord.lon
                        showOnUI()
                    }
                    else{
                        Toast.makeText(this@MainActivity, "${response.code()}", Toast.LENGTH_SHORT).show()}
                }
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Connection Failure $t", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }



    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager= getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun permissionCheckManager():Boolean{
        var res=true
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_WIFI_STATE

        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){res=true}
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                val permissionDialog=AlertDialog.Builder(this@MainActivity)
                permissionDialog.setMessage("It looks like you have turned off some permissions required for this App to work.please turned on them from Application Settings")
                permissionDialog.setPositiveButton("Go to Settings",DialogInterface.OnClickListener { dialog, which ->
                    try{
                        val intent_to_appSettings=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts("package",packageName,null)
                        intent_to_appSettings.data=uri
                        startActivity(intent_to_appSettings)
                    }catch(e:ActivityNotFoundException){
                        e.printStackTrace()
                    }
                })
                permissionDialog.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialog, which->  })
                permissionDialog.show()
            }
        }).onSameThread().check()
        return res
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationData(){
        val locationRequest=com.google.android.gms.location.LocationRequest()
        locationRequest.priority=PRIORITY_HIGH_ACCURACY
        locationProviderClint.requestLocationUpdates(locationRequest,mlocationCallback, Looper.myLooper())

    }
    private val mlocationCallback=object:LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
           val mlastLocation:Location=locationResult!!.lastLocation
            lat=mlastLocation.latitude
            lon=mlastLocation.longitude
            if(isNetworkAvailable()){
                setWeatherData("coor")
            }
            else{
                Toast.makeText(this@MainActivity, "Network is Unavailable", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun isNetworkAvailable():Boolean{
        val connectivityManager=this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            val network=connectivityManager.activeNetwork?: return false
            val activeNetwork=connectivityManager.getNetworkCapabilities(network)?: return false
            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)-> true
                else -> false
            }
        }
        else{
            val networkinfo=connectivityManager.activeNetworkInfo
            return networkinfo!=null && networkinfo.isConnectedOrConnecting
        }
    }



    fun map_btn(view: View){
        val intent=Intent(this,WeatherMap::class.java)
        intent.putExtra("latitude",lat.toString())
        intent.putExtra("longitude",lon.toString())
        startActivity(intent)
    }


    fun search(view: View){
        if(!et_name.text.isNullOrEmpty()){
            City=et_name.text.toString()
            if(isNetworkAvailable()){
                setWeatherData("city")
            }
            else{
                Toast.makeText(this@MainActivity, "Network is Unavailable", Toast.LENGTH_LONG).show()
            }

        }
        else{
            Toast.makeText(this, "Please Enter City/Place Name", Toast.LENGTH_LONG).show()
        }
        closeKeyBoard()
    }


    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun to_celcious(v: Double):Int{
        return (v-273.0).toInt()
    }


    private fun speed(s:Double):Int{
        return (s*3.6).toInt()
    }



    private fun unixTime(t:Long):String?{
        val time= Date(t*1000L)
        val stf=java.text.SimpleDateFormat("HH:mm",Locale.getDefault())
            stf.timeZone= java.util.TimeZone.getDefault()
            return stf.format(time)
    }
    private fun unixDate(d:Long):String?{
        val date=java.text.SimpleDateFormat("dd MMM yyyy",Locale.getDefault())
        return date.format(d*1000L)
    }

    private fun currtime():String?{
        val time=System.currentTimeMillis()
        val stf=java.text.SimpleDateFormat("hh:mm a",Locale.getDefault())
        return stf.format(time)
    }

}