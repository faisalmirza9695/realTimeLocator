package com.faisalmirza.realtimelocator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.lang.ref.PhantomReference

class MainActivity : FragmentActivity() {

    companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 10000
        const val MIN_TIME = 100L
        const val MIN_DIST = 5f
    }

    private lateinit var mMap: GoogleMap
    private lateinit var databaseReference: DatabaseReference
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpMapFragment()
        checkLocationPermission()
        initializeFirebaseDb()
    }

    private fun setUpMapFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

//            val sydney = LatLng(-34.0, 151.0)
//            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

            locationListener = object : LocationListener{
                override fun onLocationChanged(location: Location?) {
                    etLatitude.setText(location?.latitude?.toString())
                    etLongitude.setText(location?.longitude?.toString())
                }

                override fun onProviderEnabled(provider: String?) {

                }

                override fun onProviderDisabled(provider: String?) {

                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                }
            }

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener)
        }
    }

    private fun initializeFirebaseDb(){
        databaseReference = FirebaseDatabase.getInstance().getReference("Location")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dbLatitudeString = dataSnapshot.child("latitude").value.toString()
                        .substring(1, dataSnapshot.child("latitude").value.toString().length - 1)
                    val dbLongitudeString = dataSnapshot.child("longitude").value.toString()
                        .substring(1, dataSnapshot.child("longitude").value.toString().length - 1)

                    val stringLat = dbLatitudeString.split(", ")
                    stringLat.sorted()
                    val latitude = stringLat[stringLat.size-1].split("=")[1]

                    val stringLong = dbLongitudeString.split(", ")
                    stringLong.sorted()
                    val longitude = stringLong[stringLong.size-1].split("=")[1]

                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())

                    mMap.addMarker(MarkerOptions().position(latLng).title("$latitude , $longitude"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun checkLocationPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )

        } else {
            // Permission has already been granted
        }
    }

    fun updateButtonOnClick(view: View){
        databaseReference.child("latitude").push().setValue(etLatitude.text.toString())
        databaseReference.child("longitude").push().setValue(etLongitude.text.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
