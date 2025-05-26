package com.example.mygastrogeni.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mygastrogeni.R
import com.example.mygastrogeni.databinding.ItemRecetaBinding
import com.example.mygastrogeni.ui.favorite.FavoritosManager
import com.example.mygastrogeni.ui.home.DetalleRecetaActivity
import com.example.mygastrogeni.ui.home.Receta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RecetaAdapter(
    private val context: Context,
    private var recetas: MutableList<Receta>,
    private val modoFavoritos: Boolean = false,
    private val onFavoritoEliminado: ((Receta) -> Unit)? = null
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    private val favoritosManager = FavoritosManager(context)
    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    inner class RecetaViewHolder(private val binding: ItemRecetaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(receta: Receta) {
            binding.textNombreReceta.text = receta.nombre

            Log.d("RecetaAdapter", "Intentando cargar imagen con URI: ${receta.imagenUri}")

            if (!receta.imagenUri.isNullOrEmpty()) {
                Glide.with(context)
                    .load(receta.imagenUri)
                    .placeholder(R.drawable.carga)
                    .error(R.drawable.error)
                    .into(binding.imageReceta)
                Log.d("RecetaAdapter", "Glide cargando imagen desde URI: ${receta.imagenUri}")
            } else {
                binding.imageReceta.setImageResource(R.drawable.fav1)
                Log.d("RecetaAdapter", "URI de imagen vacía, mostrando imagen por defecto")
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, DetalleRecetaActivity::class.java).apply {
                    putExtra("nombre", receta.nombre)
                    putExtra("descripcion", receta.descripcion)
                    putExtra("ingredientes", receta.ingredientes)
                    putExtra("pasos", receta.pasos)
                    putExtra("imagenUri", receta.imagenUri)
                    putExtra("id", receta.id)
                }
                context.startActivity(intent)
            }

            if (modoFavoritos) {
                binding.favoritoButton.setImageResource(R.drawable.eliminar)
                binding.favoritoButton.setOnClickListener {
                    eliminarFavoritoFirestore(receta)
                    Toast.makeText(context, "${receta.nombre} eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    onFavoritoEliminado?.invoke(receta)
                }
            } else {
                val yaEsFavorito = favoritosManager.obtenerFavoritos().any { it.id == receta.id }
                val icon = if (yaEsFavorito) R.drawable.lleno else R.drawable.vacio
                binding.favoritoButton.setImageResource(icon)
                binding.favoritoButton.setOnClickListener {
                    if (!yaEsFavorito) {
                        agregarFavoritoFirestore(receta)
                        Toast.makeText(context, "${receta.nombre} añadido a tus favoritos", Toast.LENGTH_SHORT).show()
                        binding.favoritoButton.setImageResource(R.drawable.lleno)
                    } else {
                        eliminarFavoritoFirestore(receta)
                        Toast.makeText(context, "${receta.nombre} eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        binding.favoritoButton.setImageResource(R.drawable.vacio)
                    }
                }
            }
        }
    }

    private fun agregarFavoritoFirestore(receta: Receta) {
        val usuario = mAuth.currentUser?.uid ?: return
        db.collection("usuarios").document(usuario)
            .update("recetasFavoritas", FieldValue.arrayUnion(receta.id))
            .addOnSuccessListener {
                Log.d("RecetaAdapter", "Receta ${receta.nombre} agregada a favoritos en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("RecetaAdapter", "Error al agregar favorito a Firestore: ${e.message}", e)
            }
    }

    private fun eliminarFavoritoFirestore(receta: Receta) {
        val usuario = mAuth.currentUser?.uid ?: return
        db.collection("usuarios").document(usuario)
            .update("recetasFavoritas", FieldValue.arrayRemove(receta.id))
            .addOnSuccessListener {
                Log.d("RecetaAdapter", "Receta ${receta.nombre} eliminada de favoritos en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("RecetaAdapter", "Error al eliminar favorito de Firestore: ${e.message}", e)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = ItemRecetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position])
    }

    override fun getItemCount(): Int = recetas.size

    fun updateList(nuevaLista: List<Receta>) {
        recetas.clear()
        recetas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}