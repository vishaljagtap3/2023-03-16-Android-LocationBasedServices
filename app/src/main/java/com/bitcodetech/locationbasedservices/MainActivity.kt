package com.bitcodetech.locationbasedservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager : LocationManager

    private val brLocation = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            mt("br triggered")
            for(key in intent!!.extras!!.keySet() ) {
                mt(key)
            }
            if(intent != null && intent.extras != null && intent.extras!!.containsKey(LocationManager.KEY_LOCATION_CHANGED)) {
                val location =
                    intent.extras!!.get(LocationManager.KEY_LOCATION_CHANGED)!! as Location
                mt("Br received location: ${location.latitude} ${location.longitude}")
            }
        }
    }

    private val brProximity = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {

            val entered = intent!!.getBooleanExtra(
                LocationManager.KEY_PROXIMITY_ENTERING,
                false
            )
            mt("#### Boundary Crossed $entered ####")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(
            brLocation,
            IntentFilter("in.bitcode.LOCATION")
        )

        registerReceiver(
            brProximity,
            IntentFilter("in.bitcode.proxy")
        )

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        for(provider in locationManager.allProviders) {
            mt("Provider : $provider")
            val locationProvider = locationManager.getProvider(provider)
            mt("Accuracy: ${locationProvider!!.accuracy}")
            mt("Power: ${locationProvider.powerRequirement}")
            mt("Cost?: ${locationProvider.hasMonetaryCost()}")
            mt("Altitude? ${locationProvider.supportsAltitude()}")
            mt("Cell Net Sat? ${locationProvider.requiresCell()} ${locationProvider.requiresNetwork()} ${locationProvider.requiresSatellite()}")
            val location = locationManager.getLastKnownLocation(provider)
            if(location != null) {
                mt("last location: ${location.latitude}, ${location.longitude}")
            }
            mt("--------------------------------")
        }

        val criteria = Criteria()
        criteria.isCostAllowed = true
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.isAltitudeRequired = true

        val criteriaBasedProviders = locationManager.getProviders(criteria, true)
        for(provider in criteriaBasedProviders) {
            mt(provider)
        }
        mt("---------------------")

        val bestProvider = locationManager.getBestProvider(criteria, true)
        mt("best pro: ${bestProvider}")

        /*locationManager.requestSingleUpdate(
            bestProvider!!,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    mt("Current location is: ${location.latitude} , ${location.longitude}")
                }

            },
            null
        )*/

        val locationPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent("in.bitcode.LOCATION"),
            0
        )
        locationManager.requestSingleUpdate(
            bestProvider!!,
            locationPendingIntent
        )

        val locationListener = MyLocationListener()

        locationManager.requestLocationUpdates(
            bestProvider,
            1000L,
            100.0F,
            locationListener
        )

        //locationManager.removeUpdates(locationListener)

        locationManager.requestLocationUpdates(
            bestProvider,
            1000L,
            100.0F,
            locationPendingIntent
        )

        val proximityIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent("in.bitcode.proxy"),
            0
        )

        locationManager.addProximityAlert(
            18.5626,
            73.9168,
            5000F,
            -1,
            proximityIntent
        )

        //locationManager.removeProximityAlert(proximityIntent)

    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            mt("** ${location.latitude} ${location.longitude} **")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            //super.onStatusChanged(provider, status, extras)
        }

    }

    private fun mt(text : String) {
        Log.e("tag", text)
    }
}