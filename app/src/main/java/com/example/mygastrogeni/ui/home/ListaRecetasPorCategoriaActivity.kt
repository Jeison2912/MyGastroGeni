package com.example.mygastrogeni.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.RecetaAdapter
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import com.example.mygastrogeni.ui.utils.capitalizeWords

class ListaRecetasPorCategoriaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recetaAdapter: RecetaAdapter
    private val recetasList = mutableListOf<Receta>()
    private lateinit var textTituloCategoria: TextView
    private lateinit var recetaViewModel: RecetaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_recetas_por_categoria)


        recetaViewModel = ViewModelProvider(this)[RecetaViewModel::class.java]

        textTituloCategoria = findViewById(R.id.textTituloCategoria)
        recyclerView = findViewById(R.id.recyclerViewRecetasPorCategoria)


        val categoriaRecibida = intent.getStringExtra("nombre_categoria")


        val categoriaAFiltrar = categoriaRecibida?.trim()?.capitalizeWords()

        if (categoriaAFiltrar.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se encontró la categoría para filtrar.", Toast.LENGTH_LONG).show()
            Log.e("ListaRecetasCat", "Categoría recibida del Intent es nula o vacía.")
            finish()
            return
        }

        textTituloCategoria.text = categoriaAFiltrar
        recetaAdapter = RecetaAdapter(this, recetasList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recetaAdapter


        recetaViewModel.todasLasRecetas.observe(this) { todasLasRecetas ->
            val recetasFiltradas = todasLasRecetas.filter { receta ->
                val categoriaRecetaNormalizada = receta.categoria?.trim()?.capitalizeWords() ?: ""

                Log.d("ListaRecetasCat", "Comparando: Receta Cat: '$categoriaRecetaNormalizada' con Filtro Cat: '$categoriaAFiltrar'")

                categoriaRecetaNormalizada == categoriaAFiltrar
            }

            if (recetasFiltradas.isEmpty()) {
                Log.d("ListaRecetasCat", "No se encontraron recetas para la categoría: '$categoriaAFiltrar'")
                Toast.makeText(this, "No hay recetas para esta categoría.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("ListaRecetasCat", "Encontradas ${recetasFiltradas.size} recetas para la categoría: '$categoriaAFiltrar'")
            }

            recetaAdapter.updateList(recetasFiltradas)
        }
    }
}