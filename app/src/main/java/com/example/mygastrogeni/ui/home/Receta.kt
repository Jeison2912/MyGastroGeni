package com.example.mygastrogeni.ui.home

data class Receta(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ingredientes: String = "",
    val pasos: String = "",
    var imagenUri: String = "",
    val autor: String = "",
    val fechaCreacion: String = "",
    val categoria: String = ""
)