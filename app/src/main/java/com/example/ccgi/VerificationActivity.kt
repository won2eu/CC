package com.example.ccgi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import android.util.Log



class VerificationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    private val PNU_BOUNDS = LatLngBounds(
        LatLng(35.2290, 129.0810),
        LatLng(35.2430, 129.0965)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.mapView)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        val verifyBtn = findViewById<Button>(R.id.btn_verify_location)
        verifyBtn.setOnClickListener {
            checkLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("VerificationActivity", "onMapReady called")
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        enableLocationAndShow()
    }

    private fun enableLocationAndShow() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
            return
        }

        googleMap.isMyLocationEnabled = true

        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            numUpdates = 1 // 한 번만 받아오도록 설정
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        val current = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16f))
                    } else {
                        Toast.makeText(this@VerificationActivity, "정확한 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            null
        )
    }


    private fun checkLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                Log.d("VerificationActivity", "현재 위도: ${location.latitude}, 경도: ${location.longitude}")

                if (PNU_BOUNDS.contains(currentLatLng)) {

                    Toast.makeText(this, "\u2705 캠퍼스 안에 있습니다!", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent().putExtra("verified", true)
                    setResult(RESULT_OK, resultIntent)
                    finish() // 🔴 finish() 후 return을 안 하면 아래 else까지 실행될 수 있음

                } else {
                    Toast.makeText(this, "\u274C 캠퍼스 밖입니다!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationAndShow()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 지도 생명주기
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
