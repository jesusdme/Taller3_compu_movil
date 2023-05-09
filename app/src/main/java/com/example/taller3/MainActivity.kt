package com.example.taller3

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3.adapter.UsuarioAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion
    private val REQUEST_CODE = 4
    private lateinit var database: DatabaseReference
    private lateinit var Mutalblelista:MutableList<Usuario>
    private lateinit var lista:List<Usuario>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
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
//                binding = ActivityMapsBinding.inflate(layoutInflater)
//                setContentView(binding.root)

            }
        }
        database = Firebase.database.reference
        val storage = Firebase.storage
        val storageRef = storage.reference.child("imagenes")
        val filenames = mutableListOf<String>()
        val usuariosRef = database.child("Usuarios")

        database.child("Usuarios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //val estado = snapshot.getValue(HashMap::class.java)?.get("estado") as Boolean?
                /*val estadoType = object : GenericTypeIndicator<Boolean>() {}
                val estado = snapshot.child("estado").getValue(estadoType)
                if (estado == true) {
                    usuariosRef.child("nombre").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val nombre = dataSnapshot.getValue(String::class.java)
                            Toast.makeText(applicationContext, "$nombre se ha conectado", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w("TAG", "Error en la lectura del nombre del usuario", error.toException())
                        }
                    })
                }*/

                Mutalblelista= mutableListOf<Usuario>()
                for (data in snapshot.children) {
                    val nombre = data.child("nombre").getValue(String::class.java) ?: ""
                    val apellido = data.child("apellido").getValue(String::class.java) ?: ""
                    val id = data.child("id").getValue(String::class.java)?:""
                    val latitud = data.child("latitud").getValue(Double::class.java) ?: 0.0
                    val longitud = data.child("longitud").getValue(Double::class.java) ?: 0.0
                    val estado = data.child("estado").getValue(Boolean::class.java)?:false
                    val usuario = Usuario(nombre, apellido, id, latitud, longitud,estado)
                    if(estado==true){
                        Mutalblelista.add(usuario)
                    }
                }

                // Actualizar la UI con la nueva lista
                initRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Fallo la lectura: ${error.toException()}")
            }
        })


    }


    fun initRecyclerView(){
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerUsuario)
        recyclerView.layoutManager =LinearLayoutManager(this)
        recyclerView.adapter = UsuarioAdapter(Mutalblelista.toList(), this)

    }
}