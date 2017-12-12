package io.github.dkambersky.songle.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.Style

/**
 * Common Map activity class.
 * Mainly here to keep the API and permission specific
 * functionality separate from the main Game activity
 */
abstract class MapActivity : BaseActivity(),
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    protected lateinit var map: GoogleMap
    protected lateinit var apiClient: GoogleApiClient

    protected var lastLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        apiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()


    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initLocServices()
    }

    private fun addMarker(loc: LatLng, title: String, style: Style) {
        map.addMarker(
                MarkerOptions()
                        .position(loc)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromBitmap(style.icon)))
    }

    protected fun addMarker(point: Placemark) {
        addMarker(point.loc, point.description, point.style)
    }


    private fun initLocServices() {
        try {
            // Visualise current position with a small blue circle
            map.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            println("Security exception thrown [onMapReady]")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 5)
        }
        // Add ”My location” button to the user interface
        try {
            map.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            snack("You need to enable location permissions for the app to work properly!",
                    length = Snackbar.LENGTH_INDEFINITE)
        }

        map.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onStart() {
        super.onStart()
        apiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (apiClient.isConnected) {
            apiClient.disconnect()
        }
    }

    fun createLocationRequest() {
// Set the parameters for the location request
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000 // preferably every 5 seconds
        mLocationRequest.fastestInterval = 1000 // at most every second
        mLocationRequest.priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY
// Can we access the user’s current location?
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    apiClient, mLocationRequest, this as com.google.android.gms.location.LocationListener)
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        createLocationRequest()


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            val api = LocationServices.FusedLocationApi
            lastLocation = api.getLastLocation(apiClient)


            if (lastLocation == null) {
                println("Warning: lastLocation is null")
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1)
        }
    }


    override fun onLocationChanged(current: Location?) {
        if (current == null) {
            println("[onLocationChanged] Location unknown")
        } else {
            println(""" [onLocationChanged] Lat/long now
            (${current.latitude},
            ${current.longitude})"""
            )
// Do something with current location
            println(current)
        }
    }

    override fun onConnectionSuspended(flag: Int) {
        println(" >>>> onConnectionSuspended")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
// An unresolvable error has occurred and a connection to Google APIs
// could not be established. Display an error message, or handle
// the failure silently
        println(" > > > > onConnectionFailed ")
    }

}
