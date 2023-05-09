package com.example.taller3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller3.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.*
import kotlin.math.*



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var miLat: Double = 0.0
    var miLon: Double = 0.0
    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion
    var inic=false
    lateinit var conec: MutableList<Boolean>

    var distancia = 0.0
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

//
// binding = ActivityMapsBinding.inflate(layoutInflater)
// setContentView(binding.root)


            }
        }



        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val bundle = intent.extras
        val email = bundle?.getString("email")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap



        requestLocationFunction()
        showUserLocation()

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18F))

//leer json
        marcadores()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this,MainActivity::class.java)
        when(item.itemId){
            R.id.usuariosMenu->startActivity(intent)
            R.id.cerrarSesion->{
                auth.signOut()
                val intent = Intent(this,InicioActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            R.id.Activo->{
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid
                val isActiveRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId.toString()).child("estado")
                isActiveRef.setValue(true)

                Toast.makeText(this, "Su sesión esta Activa", Toast.LENGTH_LONG).show()
            }
            R.id.Inactivo->{
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid
                val isActiveRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId.toString()).child("estado")
                isActiveRef.setValue(false)
                Toast.makeText(this, "Su sesión esta inactiva", Toast.LENGTH_LONG).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }




    private fun showUserLocation() {
        usConec()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
// TODO: Consider calling
// ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
// public void onRequestPermissionsResult(int requestCode, String[] permissions,
// int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    var ubicAc = LatLng(it.latitude, it.longitude)
                    distancia = abs(calcularDistancia(miLat, miLon, it.latitude, it.longitude))



                    if (distancia >= 30) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicAc))
                        mMap.addMarker(
                            MarkerOptions().position(ubicAc)
                                .title("UBICACION")
                                .snippet("") //Texto de Información
                                .alpha(0.5f)//Trasparencia
                        )
                        miLat = it.latitude
                        miLon = it.longitude
                    }

                }
            }


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
    private fun geoCoderSearchLatLang(latLng: LatLng): String? {
        val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        var address = ""

        if (addresses != null && addresses.size > 0) {
            val returnedAddress = addresses[0]
            address = "${returnedAddress.thoroughfare}, ${returnedAddress.locality}"
        }
        return address
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

    fun marcadores() {
        val jsonString =
            application.assets.open("locations.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

// Obtenemos el objeto "locations"
        val locationsObject = jsonObject.getJSONObject("locations")

// Obtenemos el array "locationsArray"
        val locationsArray = jsonObject.getJSONArray("locationsArray")

// Iteramos sobre los elementos del array "locationsArray"
        for (i in 0 until locationsArray.length()) {
            val locationObject = locationsArray.getJSONObject(i)
            val lat = locationObject.getDouble("latitude")
            val lon = locationObject.getDouble("longitude")
            val name = locationObject.getString("name")
// Haz lo que necesites con los valores
            var marc = LatLng(lat, lon)
//añadir marcadores
            mMap.addMarker(
                MarkerOptions().position(marc)
                    .title(name)
                    .snippet(i.toString()) //Texto de Información
            )
        }
    }
    private fun usConec() {
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
                            Toast.makeText(this@MapsActivity, nombre+" "+apellido+" se ha conectado", Toast.LENGTH_SHORT).show()
                    }
                    if (inic==true) {
                        if (cont >= conec.size)
                            conec.add(true)
                    }

                    if (inic==true)
                        conec[cont] =estado

                }
                if (inic==false)
                {
                    conec = MutableList(cont) { true }
                    inic=true
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Fallo la lectura: ${error.toException()}")
            }
        })
    }
}