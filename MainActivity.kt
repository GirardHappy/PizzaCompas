package com.example.pizzacompas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {

    lateinit var sensorListener:SensorListener
    lateinit var pizzaSlice: ImageView
    lateinit var locationClient:FusedLocationProviderClient
    lateinit var currentLocation: Location
    lateinit var destinationLocation: Location
    lateinit var button:Button



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentLocation = Location("")
        sensorListener = SensorListener(this,getSystemService(SENSOR_SERVICE) as SensorManager)
        pizzaSlice = findViewById(R.id.pizzaSliceView)
        button = findViewById<Button>(R.id.bb)

        button.setOnClickListener()
        {
            if(checkGPS()) {
                button.visibility = View.GONE
                getCurrentLocationStart()
                getDestinationLocation()
            }
        }
    }

    private fun getDestinationLocation(){
        Thread{
            var latp: String = currentLocation.latitude.toString()
            var lngp: String = currentLocation.longitude.toString()
            var i: Int = 0
            var s: String
            var d: Document?=null

            try {
                d = Jsoup.connect("https://www.google.com/maps/search/pizza/@${latp},${lngp},20z/data=!4m4!2m3!5m1!2e1!6e5").header("Cookie", "CONSENT=YES+cb.20210418-17-p0.it+FX+917;").get()
            } catch (e: java.net.UnknownHostException) {
                Log.w("Coord in MainActy", "Connessione a Internet Assente")
            } catch (e: Exception) {
                Log.w("Coord in MainActy", "$e")
            }
            if(d!=null) {
                s = d.toString()
                s = s.substring(s.indexOf("\"categorical-search-results-injection\""))
                for (n in 0..7)
                    i = s.indexOf('[', i + 1)
                s = s.substring(i + 1)
                s = s.substring(0, s.indexOf(']'))

                var latd: String = s.substring(0, s.indexOf(","))
                var lngd: String = s.substring(s.indexOf(",") + 1)
                latd = latd.substring(0, 2) + "." + latd.substring(2)
                lngd = lngd.substring(0, 2) + "." + lngd.substring(2)
                destinationLocation = Location("")
                destinationLocation.latitude = latd.toDouble()
                destinationLocation.longitude = lngd.toDouble()
                this@MainActivity.runOnUiThread(Runnable {
                    pizzaUpdate()
                })
            }
        }.start()
    }

    private fun getCurrentLocationStart(){
        val cancellationToken: CancellationToken = CancellationTokenSource().token
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            Thread {
                while(true) {
                    locationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken
                    ).addOnSuccessListener {
                        currentLocation = it
                        Log.d(
                            "currentLocation",
                            "Lat: ${currentLocation.latitude} | Long: ${currentLocation.longitude}"
                        )
                    }
                    sleep(10 * 1000)//10 sec
                }
            }.start()
        }
    }

    private fun checkGPS():Boolean {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val cancellationToken: CancellationToken = CancellationTokenSource().token
                locationClient = LocationServices.getFusedLocationProviderClient(this)
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken
                ).addOnSuccessListener {
                    currentLocation = it
                }
            }
            else{
                Toast.makeText(this, "GPS disattivato", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Posizione non consentita", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }




    private fun pizzaUpdate(){
        var rotation:Float = 0f
        var atan: Float
        Thread{
            while (true) {
                rotation = (sensorListener.dn+currentLocation.bearingTo(destinationLocation))*-1
                this@MainActivity.runOnUiThread(Runnable {
                    pizzaSlice.rotation = rotation
                })
                Log.d("mh","dn: ${sensorListener.dn} | bearing: ${currentLocation.bearingTo(destinationLocation)} | coord: ${"https://www.google.com/maps/search/pizza/@${currentLocation.latitude},${currentLocation.longitude},20z/data=!4m4!2m3!5m1!2e1!6e5"}")
                sleep(50)//50
            }
        }.start()
    }
}