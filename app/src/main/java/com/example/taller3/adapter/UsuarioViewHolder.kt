package com.example.taller3.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3.R
import com.example.taller3.RastreoActivity
import com.example.taller3.Usuario
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

class UsuarioViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {

    val foto = view.findViewById<ImageView>(R.id.user_image)
    val nombre = view.findViewById<TextView>(R.id.user_name)
    val apellido = view.findViewById<TextView>(R.id.user_lastname)
    val rastreo = view.findViewById<Button>(R.id.rastreo)


    fun render(usuarioModel: Usuario)
    {
        nombre.text =usuarioModel.Nombre
        apellido.text=usuarioModel.Apellido
        val persona = usuarioModel.Nombre+" "+usuarioModel.Apellido
        val id =usuarioModel.Id

        //ACA
        /*val drawable = foto.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()*/

        val storage= FirebaseStorage.getInstance().reference.child("imagenes/$id")
        val localfile = File.createTempFile("TempImage","jpeg")
        storage.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            foto.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(context, "no se pudo subir la imagen", Toast.LENGTH_SHORT).show()
        }


        rastreo.setOnClickListener(){

            println( "lat:  " +usuarioModel.Latitud.toString()+" lon: "+
            usuarioModel.Longitud.toString() )
            val intent = Intent(context, RastreoActivity::class.java)
            intent.putExtra("latitud",usuarioModel.Latitud)
            intent.putExtra("longitud",usuarioModel.Longitud)
            intent.putExtra("persona",persona)
            intent.putExtra("id",id)
            context.startActivity(intent)
        }
    }
}