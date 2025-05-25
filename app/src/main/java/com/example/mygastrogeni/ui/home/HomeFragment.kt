package com.example.mygastrogeni.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R
import com.example.mygastrogeni.databinding.FragmentHomeBinding
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import com.example.mygastrogeni.ui.utils.capitalizeWords // Importa la función de extensión

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var categoriaAdapter: CategoriaAdapter

    private val recetaViewModel: RecetaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerViewCategorias = binding.recyclerViewCategorias
        recyclerViewCategorias.layoutManager = GridLayoutManager(requireContext(), 2)

        categoriaAdapter = CategoriaAdapter(requireContext(), emptyList())
        recyclerViewCategorias.adapter = categoriaAdapter

        recetaViewModel.todasLasRecetas.observe(viewLifecycleOwner, Observer { recetas ->
            // --- LOGGING PARA DEPURACIÓN ---
            Log.d("HomeFragment", "Observer activado. Total de recetas recibidas: ${recetas.size}")
            recetas.forEachIndexed { index, receta ->
                Log.d("HomeFragment", "Receta $index: Nombre='${receta.nombre}', Categoría='${receta.categoria}'")
            }
            // --- FIN LOGGING ---

            val categoriasActualizadas = crearListaCategoriasDesdeRecetas(recetas)

            // --- LOGGING PARA DEPURACIÓN ---
            Log.d("HomeFragment", "Categorías generadas para el adaptador: ${categoriasActualizadas.size}")
            categoriasActualizadas.forEachIndexed { index, categoria ->
                Log.d("HomeFragment", "Categoría $index: Nombre='${categoria.nombre}', ImagenResId='${categoria.imagenResId}'")
            }
            // --- FIN LOGGING ---

            categoriaAdapter.actualizarCategorias(categoriasActualizadas)
        })

        return root
    }

    private fun crearListaCategoriasDesdeRecetas(recetas: List<Receta>): List<Categoria> {
        val categoriasUnicas = mutableMapOf<String, Categoria>()

        // IMPORTANTE: Asegúrate de que estos nombres de categorías coincidan EXACTAMENTE
        // con los de tu string-array unificado en strings.xml (ej. singular o plural)
        val imagenesCategoriasBase = mapOf(
            "Postre" to R.drawable.icecream4,     // Usando nombres en singular
            "Desayuno" to R.drawable.breakfast,   // Usando nombres en singular
            "Almuerzo" to R.drawable.fav3,        // Usando nombres en singular
            "Cena" to R.drawable.cena,
            "Bebida" to R.drawable.bebidas,
            "Vegetariana" to R.drawable.vegetarianas // Usando nombres en singular
            // Añade aquí cualquier otra categoría base que quieras con una imagen específica
        )

        // 1. Inicializar con categorías base y sus imágenes específicas
        imagenesCategoriasBase.forEach { (nombre, imagenResId) ->
            categoriasUnicas[nombre] = Categoria(nombre, imagenResId)
        }

        // 2. Procesar las recetas para añadir categorías dinámicas si no existen
        recetas.forEach { receta ->
            // MUY IMPORTANTE: Normalizar el nombre de la categoría al leerla de la receta
            val categoriaNombreNormalizada = receta.categoria?.trim()?.capitalizeWords() ?: ""

            Log.d("HomeFragment", "Receta '${receta.nombre}' tiene categoría normalizada: '$categoriaNombreNormalizada'")

            if (categoriaNombreNormalizada.isNotEmpty()) {
                if (!categoriasUnicas.containsKey(categoriaNombreNormalizada)) {
                    // Si la categoría de la receta NO está ya en nuestro mapa (ni como base ni por otra receta)
                    categoriasUnicas[categoriaNombreNormalizada] = Categoria(categoriaNombreNormalizada, R.drawable.adjuntar) // Imagen genérica
                    Log.d("HomeFragment", "Añadida nueva categoría dinámica: '${categoriaNombreNormalizada}' con imagen genérica.")
                } else {
                    Log.d("HomeFragment", "Categoría '${categoriaNombreNormalizada}' ya existe en el mapa (base o añadida por otra receta).")
                }
            } else {
                Log.w("HomeFragment", "Receta '${receta.nombre}' tiene una categoría vacía o nula. No se usará para la lista de categorías.")
            }
        }

        return categoriasUnicas.values.sortedBy { it.nombre }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}