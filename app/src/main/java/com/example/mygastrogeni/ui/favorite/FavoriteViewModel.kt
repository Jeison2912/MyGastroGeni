package com.example.mygastrogeni.ui.favorite

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mygastrogeni.ui.home.Receta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteViewModel : ViewModel() {

    private val _favoriteRecipes = MutableLiveData<List<Receta>>()
    val favoriteRecipes: LiveData<List<Receta>> = _favoriteRecipes

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    init {
        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val favoriteIds = document.get("recetasFavoritas") as? List<String> ?: emptyList()
                        if (favoriteIds.isNotEmpty()) {
                            loadRecipesByIds(favoriteIds)
                        } else {
                            _favoriteRecipes.value = emptyList()
                        }
                    } else {
                        _favoriteRecipes.value = emptyList()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FavoriteViewModel", "Error al obtener IDs de favoritos: ${e.message}", e)
                    _favoriteRecipes.value = emptyList()
                }
        } else {
            _favoriteRecipes.value = emptyList()
        }
    }

    private fun loadRecipesByIds(ids: List<String>) {
        val recipesList = mutableListOf<Receta>()
        val recipesLoaded = mutableSetOf<String>()

        ids.forEach { recipeId ->
            if (!recipesLoaded.contains(recipeId)) {
                db.collection("recetas").document(recipeId)
                    .get()
                    .addOnSuccessListener { recipeDocument ->
                        if (recipeDocument.exists()) {
                            var receta = recipeDocument.toObject(Receta::class.java)
                            receta?.id = recipeDocument.id
                            receta?.imagenUri = recipeDocument.getString("imagenUri").toString()
                            receta?.let {
                                recipesList.add(it)
                                _favoriteRecipes.value = recipesList.toList()
                                recipesLoaded.add(recipeId)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FavoriteViewModel", "Error al cargar receta con ID $recipeId: ${e.message}", e)
                    }
            }
        }
    }

    fun removeFavorite(recipeId: String) {
        val usuario = mAuth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid)
                .update("recetasFavoritas", com.google.firebase.firestore.FieldValue.arrayRemove(recipeId))
                .addOnSuccessListener {
                    Log.d("FavoriteViewModel", "Receta eliminada de favoritos en Firestore")
                    loadFavoriteRecipes()
                }
                .addOnFailureListener { e ->
                    Log.e("FavoriteViewModel", "Error al eliminar favorito de Firestore: ${e.message}", e)
                }
        }
    }
}