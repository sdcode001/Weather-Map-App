package eu.deysouvik.weathermap

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

class WeatherMap : AppCompatActivity() {
    lateinit var latitude:String
    lateinit var longitude:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_map)

        latitude=intent.getStringExtra("latitude").toString()
        longitude=intent.getStringExtra("longitude").toString()

        Log.i("coor","$latitude , $longitude")
        val dialog=customProgressBar()
        delayHandler(dialog)
        val mwebView=findViewById<View>(R.id.web_view) as WebView

        mwebView.loadUrl("https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&lat=$latitude&lon=$longitude&zoom=10")

        val websetting=mwebView.settings
        websetting.javaScriptEnabled=true
        mwebView.webViewClient= WebViewClient()

        mwebView.canGoBack()

        mwebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->

            if(keyCode== KeyEvent.KEYCODE_BACK && event.action== MotionEvent.ACTION_UP && mwebView.canGoBack()){

                mwebView.goBack()

                return@OnKeyListener true
            }

            false
        })


    }


    fun customProgressBar(): Dialog {
        val progressBar_dialog= Dialog(this)
        progressBar_dialog.setContentView(R.layout.custom_progress_bar)
        progressBar_dialog.show()
        return progressBar_dialog
    }
    fun cancel_progressBar(progressbar: Dialog){
        progressbar.cancel()
    }


    fun delayHandler(dialog: Dialog){
        Handler().postDelayed(Runnable() {
            run(){
                cancel_progressBar(dialog)
            }
        },5000)
    }




}