package com.example.mygastrogeni.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.receta.RecetaFirebaseAdapter
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel

class HomeFragment : Fragment() {

    private val recetaViewModel: RecetaViewModel by viewModels()
    private lateinit var adapter: RecetaFirebaseAdapter
    private var listaRecetas: List<Receta> = emptyList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        adapter = RecetaFirebaseAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recetaViewModel.todasLasRecetas.observe(viewLifecycleOwner) { recetas ->
            listaRecetas = recetas
            adapter.actualizarLista(recetas)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().lowercase()
                val filtradas = listaRecetas.filter {
                    it.nombre?.lowercase()?.contains(texto) ?: false
                }
                adapter.actualizarLista(filtradas)
                return true
            }
        })

        val searchText = searchView.findViewById<AutoCompleteTextView>(
            androidx.appcompat.R.id.search_src_text
        )
        searchText.setTextColor(resources.getColor(android.R.color.white, null))
        searchText.setHintTextColor(resources.getColor(android.R.color.darker_gray, null))
    }
}