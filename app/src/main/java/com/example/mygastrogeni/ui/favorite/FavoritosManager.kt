package com.example.mygastrogeni.ui.favorite

import android.content.Context
import android.util.Log
import com.example.mygastrogeni.ui.home.Receta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritosManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("favoritos", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun guardarFavorito(receta: Receta) {
        val gson = Gson()
        val recetasJson = sharedPreferences.getString("recetas", null)
        val recetasListType = object : TypeToken<MutableList<Receta>>() {}.type
        val recetas: MutableList<Receta> = try {
            if (recetasJson != null) {
                gson.fromJson(recetasJson, recetasListType)
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            Log.e("FavoritosManager", "Error al leer JSON: ${e.message}")
            mutableListOf()
        }
        recetas.add(receta)
        val updatedJson = gson.toJson(recetas)
        editor.putString("recetas", updatedJson)
        editor.apply()
    }

    fun obtenerFavoritos(): MutableList<Receta> {
        val gson = Gson()
        val recetasJson = sharedPreferences.getString("recetas", null)
        val recetasListType = object : TypeToken<MutableList<Receta>>() {}.type
        return try {
            if (recetasJson != null) {
                gson.fromJson(recetasJson, recetasListType)
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            Log.e("FavoritosManager", "Error al cargar favoritos: ${e.message}")
            editor.remove("recetas").apply()
            mutableListOf()
        }
    }

    fun eliminarFavorito(receta: Receta) {
        val gson = Gson()
        val recetasJson = sharedPreferences.getString("recetas", null)
        val recetasListType = object : TypeToken<MutableList<Receta>>() {}.type
        val recetas: MutableList<Receta> = try {
            if (recetasJson != null) {
                gson.fromJson(recetasJson, recetasListType)
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            Log.e("FavoritosManager", "Error al leer JSON para eliminar: ${e.message}")
            mutableListOf()
        }
        recetas.removeAll { it.id == receta.id } // Usar ID para eliminar
        val updatedJson = gson.toJson(recetas)
        editor.putString("recetas", updatedJson)
        editor.apply()
    }
}