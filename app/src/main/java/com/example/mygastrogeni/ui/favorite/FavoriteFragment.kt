package com.example.mygastrogeni.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.RecetaAdapter
import com.example.mygastrogeni.ui.home.Receta

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recetaAdapter: RecetaAdapter
    private lateinit var textoVacio: TextView
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerViewFavoritos)
        textoVacio = rootView.findViewById(R.id.textEmptyFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recetaAdapter = RecetaAdapter(
            requireContext(),
            mutableListOf(), // Inicialmente lista mutable vacía
            modoFavoritos = true
        ) { recetaEliminada ->
            favoriteViewModel.removeFavorite(recetaEliminada.id)
        }

        recyclerView.adapter = recetaAdapter

        favoriteViewModel.favoriteRecipes.observe(viewLifecycleOwner, Observer { recetas ->
            recetaAdapter.updateList(recetas) // **Usar updateList() en lugar de submitList()**
            mostrarMensajeSiListaVacia(recetas.isEmpty())
        })

        return rootView
    }

    private fun mostrarMensajeSiListaVacia(isEmpty: Boolean) {
        textoVacio.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}