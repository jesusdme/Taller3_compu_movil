package com.example.taller3

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller3.databinding.ActivityRastreoBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.*
import kotlin.math.*

class RastreoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRastreoBinding

    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var idDes=""
    var latDes: Double = 0.0
    var lonDes: Double = 0.0
    var persona = ""
    lateinit var conec: MutableList<Boolean>
    var distancia = 0.0

    var inic=false

    var miLat: Double = 0.0
    var miLon: Double = 0.0

    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_LOCATION
            )

        } else //si tiene permiso usar la ubicacion
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            } else {

            }
        }


        binding = ActivityRastreoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras

        lonDes = bundle?.getDouble("longitud")!!
        latDes = bundle?.getDouble("latitud")!!
        persona = bundle.getString("persona").toString()
        idDes=bundle.getString("id").toString()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = createLocationRequest()


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var ubicacion = locationResult.lastLocation
                Log.i("ubicacion", "--------------$ubicacion---------")
                if (ubicacion != null) {
                    showUserLocation()
                }
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        requestLocationFunction()
        showUserLocation()

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18F))


    }


    private fun showUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    var ubicAc = LatLng(it.latitude, it.longitude)


                    //funcion que actualiza la ubicacion destino usando la id del destino
                    ubicDes()


                    ruta(latDes, lonDes, it.latitude, it.longitude)

                    distancia = abs(calcularDistancia(miLat, miLon, it.latitude, it.longitude))

                    if (distancia >= 30) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicAc))
                        miLat = it.latitude
                        miLon = it.longitude

                    }

                }
            }


    }

    private fun ubicDes() {
        database = Firebase.database.reference
        database.child("Usuarios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var cont=0
                for (data in snapshot.children) {
                    val nombre = data.child("nombre").getValue(String::class.java) ?: ""
                    val apellido = data.child("apellido").getValue(String::class.java) ?: ""
                    val id = data.child("id").getValue(String::class.java) ?: ""
                    val latitud = data.child("latitud").getValue(Double::class.java) ?: 0.0
                    val longitud = data.child("longitud").getValue(Double::class.java) ?: 0.0
                    val estado = data.child("estado").getValue(Boolean::class.java) ?: false
                    cont++
                    if (inic==true && estado==true)
                    {
                        if (conec[cont]== false)
                            Toast.makeText(this@RastreoActivity, nombre+" "+apellido+" se ha conectado", Toast.LENGTH_SHORT).show()
                    }
                    if (inic==true)
                        conec[cont] =estado


                    if (id == idDes && estado == true) {
                        latDes = latitud
                        lonDes = longitud
                        Log.w("TAG", "longitud: ${lonDes} latitud: ${latDes} ")


                    }

                }
                if (inic==false)
                {
                    conec = MutableList(cont+1) { true }
                    inic=true
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Fallo la lectura: ${error.toException()}")
            }
        })
    }


    private fun requestLocationFunction() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        )
            return

        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            showUserLocation()
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            setInterval(25000)
            setFastestInterval(10000)
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }
    }


    fun ruta(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {

        val startPoint = LatLng(lat2, lon2) // Primera ubicación
        val endPoint = LatLng(lat1, lon1) // Segunda ubicación

// Crear la línea recta entre las dos ubicaciones
        val line = PolylineOptions()
            .add(startPoint, endPoint)
            .width(5f)
            .color(Color.RED)

// Agregar la línea al mapa
        mMap.clear()
        val distanciaMark = abs(calcularDistancia(lat2, lon2, lat1, lon1))
        val distanciaMarkAbrev = String.format("%.2f", distanciaMark)

        mMap.addPolyline(line)
        mMap.addMarker(
            MarkerOptions().position(endPoint)
                .title(persona)
                .snippet(distanciaMarkAbrev+" metros") //Texto de Información
        )

    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                dLon / 2
            ) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanciaEnKM = radioTierra * c
        val distanciaEnMetros = distanciaEnKM * 1000

        return distanciaEnMetros
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mMap.clear() // Limpiar todos los marcadores en el mapa
    }
}