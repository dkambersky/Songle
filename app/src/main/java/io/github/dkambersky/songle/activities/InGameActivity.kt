package io.github.dkambersky.songle.activities

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import io.github.dkambersky.songle.R

class InGameActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /* Load dark mode */
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this,R.raw.style_json))

        /* Default to Crichton St */
        val crichton = LatLng(55.944575,  -3.187129)
        mMap.addMarker(MarkerOptions().position(crichton).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(crichton))

        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            println("Security exception thrown [onMapReady]")
        }
            // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true


    }
}
