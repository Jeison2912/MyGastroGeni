package com.example.mygastrogeni.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R

class CategoriaAdapter(
    private val context: Context,
    private var categorias: List<Categoria>
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewCategoria: ImageView = view.findViewById(R.id.imageCategoria)
        val textViewCategoria: TextView = view.findViewById(R.id.textNombreCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.textViewCategoria.text = categoria.nombre
        holder.imageViewCategoria.setImageResource(categoria.imagenResId)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ListaRecetasPorCategoriaActivity::class.java).apply {
                putExtra("nombre_categoria", categoria.nombre)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = categorias.size

    fun actualizarCategorias(nuevaListaCategorias: List<Categoria>) {
        this.categorias = nuevaListaCategorias
        notifyDataSetChanged()
    }
}