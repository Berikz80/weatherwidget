package by.isb.weatherwidget


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
     lateinit var latitude: String
     lateinit var longitude: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
            }
        }





        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {starLocationUpdate(locationCallback)}
            }
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> starLocationUpdate(locationCallback)
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }


        }
    }

    @SuppressLint("MissingPermission")
    private fun starLocationUpdate(locationCallback: LocationCallback) {
        fusedLocationProvider.requestLocationUpdates(
            getRequested(), locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun getRequested() = LocationRequest.create().apply {
        interval = 180000
        fastestInterval = 100000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY


    }

}