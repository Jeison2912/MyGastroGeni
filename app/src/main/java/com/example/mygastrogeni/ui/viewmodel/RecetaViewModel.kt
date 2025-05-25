package com.example.mygastrogeni.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mygastrogeni.ui.home.Receta
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecetaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val _todasLasRecetas = MutableLiveData<List<Receta>>()
    val todasLasRecetas: LiveData<List<Receta>> get() = _todasLasRecetas

    private var firestoreListener: ListenerRegistration? = null

    init {
        obtenerRecetasEnTiempoReal()
    }

    private fun obtenerRecetasEnTiempoReal() {
        firestoreListener?.remove() // Detiene cualquier escucha anterior

        firestoreListener = db.collection("recetas")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("RecetaViewModel", "Error al escuchar recetas: ${e.message}", e)
                    _todasLasRecetas.value = emptyList() // O manejar el error visualmente
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val recetasList = mutableListOf<Receta>()
                    for (doc in snapshots.documents) {
                        try {
                            val receta = doc.toObject(Receta::class.java)
                            receta?.let {
                                it.id = doc.id // Asignar el ID del documento
                                recetasList.add(it)
                            }
                        } catch (parseException: Exception) {
                            Log.e("RecetaViewModel", "Error al parsear documento ${doc.id} a Receta: ${parseException.message}", parseException)
                        }
                    }
                    _todasLasRecetas.value = recetasList
                    Log.d("RecetaViewModel", "Recetas actualizadas en tiempo real: ${recetasList.size} recetas")
                } else {
                    Log.d("RecetaViewModel", "No hay snapshots disponibles en tiempo real.")
                    _todasLasRecetas.value = emptyList()
                }
            }
    }

    fun agregarReceta(receta: Receta, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("recetas")
            .add(receta)
            .addOnSuccessListener {
                Log.d("RecetaViewModel", "Receta agregada con éxito a Firestore. ID: ${it.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("RecetaViewModel", "Error al agregar receta: ${e.message}", e)
                onFailure(e.message ?: "Error desconocido al agregar receta")
            }
    }

    fun actualizar(id: String, receta: Receta, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("recetas")
            .document(id)
            .set(receta) // .set() sobrescribe todo el documento. Si solo quieres actualizar campos específicos, usa .update()
            .addOnSuccessListener {
                Log.d("RecetaViewModel", "Receta actualizada con éxito en Firestore. ID: $id")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("RecetaViewModel", "Error al actualizar receta: ${e.message}", e)
                onFailure(e.message ?: "Error desconocido al actualizar receta")
            }
    }

    // Método para eliminar una receta (opcional, pero útil)
    fun eliminarReceta(recetaId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("recetas").document(recetaId)
            .delete()
            .addOnSuccessListener {
                Log.d("RecetaViewModel", "Receta eliminada con éxito: $recetaId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("RecetaViewModel", "Error al eliminar receta: ${e.message}", e)
                onFailure(e.message ?: "Error desconocido al eliminar receta")
            }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove() // ¡Importante! Detener la escucha de Firebase
        Log.d("RecetaViewModel", "Listener de Firestore removido en onCleared.")
    }
}