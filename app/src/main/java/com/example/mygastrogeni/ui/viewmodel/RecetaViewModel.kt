package com.example.mygastrogeni.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mygastrogeni.ui.home.Receta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecetaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val _todasLasRecetas = MutableLiveData<List<Receta>>()
    val todasLasRecetas: LiveData<List<Receta>> get() = _todasLasRecetas

    init {
        obtenerRecetas()
        // insertarRecetasPredefinidas()  // Eliminamos la llamada a la función
    }

    // Obtener todas las recetas de Firebase
    private fun obtenerRecetas() {
        db.collection("recetas")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val recetas = task.result?.map { document ->
                        document.toObject(Receta::class.java).apply {
                            id = document.id
                        }
                    }
                    _todasLasRecetas.value = recetas ?: emptyList()
                }
            }
    }

    // Insertar receta en Firebase
    fun insertar(receta: Receta) {
        val recetaData = hashMapOf(
            "nombre" to receta.nombre,
            "descripcion" to receta.descripcion,
            "ingredientes" to receta.ingredientes,
            "pasos" to receta.pasos,
            "imagenUri" to receta.imagenUri,
            "autor" to receta.autor
            // "fechaCreacion" REMOVED
        )

        db.collection("recetas")
            .add(recetaData)
            .addOnSuccessListener {
                obtenerRecetas()  // Refrescar las recetas después de agregar una nueva
            }
            .addOnFailureListener { e ->
                // Manejar error en la inserción
            }
    }

    // Actualizar receta en Firebase
    fun actualizar(id: String, receta: Receta) {
        val recetaData = hashMapOf(
            "nombre" to receta.nombre,
            "descripcion" to receta.descripcion,
            "ingredientes" to receta.ingredientes,
            "pasos" to receta.pasos,
            "imagenUri" to receta.imagenUri,
            "autor" to receta.autor
            // "fechaCreacion" REMOVED
        )

        db.collection("recetas")
            .document(id)  // Aquí usamos el ID de la receta que queremos actualizar
            .set(recetaData)
            .addOnSuccessListener {
                obtenerRecetas()  // Refrescar las recetas después de actualizar una
            }
            .addOnFailureListener { e ->
                // Manejar error en la actualización
            }
    }

    // Insertar recetas predefinidas en Firebase si no existen
    private fun insertarRecetasPredefinidas() {
        viewModelScope.launch(Dispatchers.IO) {
            val recetasActuales = todasLasRecetas.value
            if (recetasActuales.isNullOrEmpty()) {
                val recetas = listOf<Receta>()
                recetas.forEach {
                    insertar(it)
                }
            }
        }
    }

    // Función para obtener la fecha actual en formato ISO para guardar en Firestore
    private fun obtenerFechaActual(): String {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return LocalDateTime.now().format(formatter)
    }
}