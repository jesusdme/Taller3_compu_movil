package com.example.taller3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class InicioActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CAMERA = 1 // Identificador para la solicitud de permiso de la cámara
    private val REQUEST_IMAGE_CAPTURE = 2 // Identificador para la solicitud de captura de imagen
    private val REQUEST_WRITE_EXTERNAL_STORAGE =3 // Identificador para guardar la imagen
    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion
    private val REQUEST_CODE = 101
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        iniciar()
        auth = Firebase.auth
    }

    private fun iniciar(){

        //Botones
        val registrarse = findViewById<Button>(R.id.registroInicio)
        val home = findViewById<Button>(R.id.iniciarInicio)

        //Info
        val email = findViewById<EditText>(R.id.emailInicio)
        val password = findViewById<EditText>(R.id.passwordInicio)

        home.setOnClickListener{
            //si no tiene permiso preguntar
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_LOCATION)
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

                    if(email.text.isNotEmpty() && password.text.isNotEmpty()){

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnCompleteListener(){
                            if(it.isSuccessful){
                                home(it.result?.user?.email ?:"")
                            }
                            else{
                                Toast.makeText(applicationContext,"Error en los datos", Toast.LENGTH_LONG).show()
                            }

                        }
                    }
                    else{
                        Toast.makeText(applicationContext,"Llene todos los datos", Toast.LENGTH_LONG).show()
                    }

                }
            }
        }

        registrarse.setOnClickListener{
            //si no tiene permiso preguntar
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_LOCATION)
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

                    // Solicitar permiso para usar la cámara si aún no se ha concedido
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                    } else {
                            // El permiso ya ha sido concedido
                            val intent = Intent(this, RegistroActivity::class.java)
                            startActivity(intent)

                    }
                }
            }
        }
    }

    private fun updateUI(currentUser:FirebaseUser?){
        if(currentUser!=null){
            val intent = Intent(this,MapsActivity:: class.java)
            intent.putExtra("email",currentUser.email)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }



    private fun home(email: String){
        val intent = Intent(this,MapsActivity:: class.java).apply {
            putExtra("email",email)
        }
        startActivity(intent)
    }
}