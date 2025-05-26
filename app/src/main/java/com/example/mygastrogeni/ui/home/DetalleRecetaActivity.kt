package com.example.mygastrogeni.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.utils.SessionManager
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileNotFoundException

class DetalleRecetaActivity : AppCompatActivity() {

    private val recetaViewModel: RecetaViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_receta)


        val nombre = intent.getStringExtra("nombre") ?: "Sin nombre"
        val descripcion = intent.getStringExtra("descripcion") ?: "Sin descripción"
        val ingredientes = intent.getStringExtra("ingredientes") ?: "Sin ingredientes"
        val pasos = intent.getStringExtra("pasos") ?: "Sin pasos"
        val imagenUri = intent.getStringExtra("imagenUri") ?: ""
        val autor = intent.getStringExtra("autor") ?: ""
        val recetaId = intent.getStringExtra("id") ?: ""

        val usuarioActual = SessionManager.getUsername(this)


        val imagenReceta: ImageView = findViewById(R.id.imageDetalle)
        val iconoFavorito: ImageView = findViewById(R.id.imageView4)
        val tituloReceta: TextView = findViewById(R.id.textTitulo)
        val descripcionReceta: TextView = findViewById(R.id.textDescripcion)
        val ingredientesReceta: TextView = findViewById(R.id.textIngredientes)
        val pasosReceta: TextView = findViewById(R.id.textPasos)
        val btnEliminar: Button = findViewById(R.id.btnEliminar)
        val btnEditar: Button = findViewById(R.id.btnEditar)


        tituloReceta.text = nombre
        descripcionReceta.text = descripcion
        ingredientesReceta.text = ingredientes
        pasosReceta.text = pasos

        if (imagenUri.isNotEmpty()) {
            Glide.with(this)
                .load(imagenUri)
                .placeholder(R.drawable.fav1)
                .error(R.drawable.fav1)
                .into(imagenReceta)
        } else {
            imagenReceta.setImageResource(R.drawable.fav1)
        }


        if (autor == usuarioActual && recetaId.isNotEmpty()) {
            btnEditar.visibility = View.VISIBLE
            btnEliminar.visibility = View.VISIBLE
        } else {
            btnEditar.visibility = View.GONE
            btnEliminar.visibility = View.GONE
        }


        btnEliminar.setOnClickListener {
            db.collection("recetas")
                .document(recetaId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("DetalleReceta", "Error al eliminar receta: ${e.message}", e)
                    Toast.makeText(this, "Error al eliminar receta", Toast.LENGTH_SHORT).show()
                }
        }


        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarRecetaActivity::class.java).apply {
                putExtra("id", recetaId)
                putExtra("nombre", nombre)
                putExtra("ingredientes", ingredientes)
                putExtra("pasos", pasos)
                putExtra("imagenUri", imagenUri)
                putExtra("modoEdicion", true)
            }
            startActivity(intent)
            finish()
        }

        verificarEstadoFavorito(recetaId, iconoFavorito)
    }


    fun toggleFavorito(view: View) {
        val usuarioActual = mAuth.currentUser
        if (usuarioActual == null) {
            Toast.makeText(this, "Debes iniciar sesión para agregar a favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        val recetaId = intent.getStringExtra("id") ?: ""
        val imageViewFavorito = view as ImageView

        if (recetaId.isNotEmpty()) {
            val usuarioId = usuarioActual.uid
            val recetasFavoritasRef = db.collection("usuarios").document(usuarioId)

            if (imageViewFavorito.tag == "no_favorito") {

                recetasFavoritasRef.update("recetasFavoritas", FieldValue.arrayUnion(recetaId))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                        imageViewFavorito.setImageResource(R.drawable.favo)
                        imageViewFavorito.tag = "favorito"
                    }
                    .addOnFailureListener { e ->
                        Log.e("DetalleReceta", "Error al agregar a favoritos: ${e.message}", e)
                        Toast.makeText(this, "Error al agregar a favoritos", Toast.LENGTH_SHORT).show()
                    }
            } else {
                recetasFavoritasRef.update("recetasFavoritas", FieldValue.arrayRemove(recetaId))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        imageViewFavorito.setImageResource(R.drawable.favo)
                        imageViewFavorito.tag = "no_favorito"
                    }
                    .addOnFailureListener { e ->
                        Log.e("DetalleReceta", "Error al eliminar de favoritos: ${e.message}", e)
                        Toast.makeText(this, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun verificarEstadoFavorito(recetaId: String, iconoFavorito: ImageView) {
        val usuarioActual = mAuth.currentUser
        if (usuarioActual != null && recetaId.isNotEmpty()) {
            val usuarioId = usuarioActual.uid
            val recetasFavoritasRef = db.collection("usuarios").document(usuarioId)

            recetasFavoritasRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val favoritos = document.get("recetasFavoritas") as? List<String>
                        if (favoritos?.contains(recetaId) == true) {
                            iconoFavorito.setImageResource(R.drawable.favo)
                            iconoFavorito.tag = "favorito"
                        } else {
                            iconoFavorito.setImageResource(R.drawable.favorito)
                            iconoFavorito.tag = "no_favorito"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetalleReceta", "Error al verificar favorito: ${e.message}", e)
                }
        }
    }
}