package com.example.mygastrogeni.ui.Perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PerfilViewModel : ViewModel() {

    private val _nombre = MutableLiveData<String>().apply {
        value = "Nombre de Usuario"
    }
    val nombre: LiveData<String> = _nombre

    private val _email = MutableLiveData<String>().apply {
        value = "usuario@example.com"
    }
    val email: LiveData<String> = _email

    private val _descripcion = MutableLiveData<String>().apply {
        value = "Aquí puedes poner una breve descripción del perfil."
    }
    val descripcion: LiveData<String> = _descripcion

    fun actualizarPerfil(nuevoNombre: String, nuevoEmail: String, nuevaDescripcion: String) {
        _nombre.value = nuevoNombre
        _email.value = nuevoEmail
        _descripcion.value = nuevaDescripcion
    }
}