MyGastroGeni

Este archivo define cómo se ve cada elemento individual en tu lista de recetas. Para que solo muestre el nombre, nos aseguraremos de que solo haya un `TextView` para el nombre (además de la imagen y el botón de favoritos, si los quieres).

**Ubicación:** `app/src/main/res/layout/item_receta.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <ImageView
            android:id="@+id/imageReceta"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:scaleType="centerCrop"
            android:src="@drawable/fav1"
            android:contentDescription="Imagen de la receta" />

        <TextView
            android:id="@+id/textNombreReceta"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textStyle="bold"
            android:textSize="20sp"
            android:text="Nombre de la Receta"
            android:gravity="center_horizontal" />

        <ImageButton
            android:id="@+id/favoritoButton"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="end"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Añadir a favoritos"
            android:src="@drawable/vacio"
            app:tint="@color/material_dynamic_primary60" />

    </LinearLayout>
</androidx.cardview.widget.CardView>
```
2.  RecetaAdapter.kt

Este adaptador es el que "conecta" tus datos de recetas con el diseño de cada ítem en la lista. Aquí nos aseguraremos de que solo el nombre se muestre en el `TextView` correspondiente y de que, al hacer clic, se pasen **todos los detalles** de la receta a la actividad de detalle.

**Ubicación:** `app/src/main/java/com/example/mygastrogeni/ui/RecetaAdapter.kt` (o la ruta de tu paquete para `ui`)

```kotlin
package com.example.mygastrogeni.ui // Ajusta el paquete si es diferente, ej: com.example.mygeniusgastroproyec.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mygastrogeni.R
import com.example.mygastrogeni.databinding.ItemRecetaBinding // Asegúrate de que este import sea correcto
import com.example.mygastrogeni.ui.favorite.FavoritosManager
import com.example.mygastrogeni.ui.home.DetalleRecetaActivity
import com.example.mygastrogeni.ui.home.Receta // Asegúrate de que este import sea correcto

class RecetaAdapter(
    private val context: Context,
    private val recetas: MutableList<Receta>,
    private val modoFavoritos: Boolean = false,
    private val onFavoritoEliminado: ((Receta) -> Unit)? = null
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    private val favoritosManager = FavoritosManager(context)

    inner class RecetaViewHolder(private val binding: ItemRecetaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(receta: Receta) {
            // *** SOLO MOSTRAR EL NOMBRE DE LA RECETA EN LA LISTA PRINCIPAL ***
            binding.textNombreReceta.text = receta.nombre

            // Lógica para cargar la imagen (si la tienes)
            try {
                if (!receta.imagenUri.isNullOrEmpty()) {
                    val uri = Uri.parse(receta.imagenUri)
                    binding.imageReceta.setImageURI(uri)
                } else {
                    binding.imageReceta.setImageResource(R.drawable.fav1) // Imagen por defecto
                }
            } catch (e: Exception) {
                binding.imageReceta.setImageResource(R.drawable.fav1) // Imagen por defecto en caso de error
            }

            // *** MANEJAR EL CLIC EN EL ELEMENTO DE LA LISTA ***
            binding.root.setOnClickListener {
                val intent = Intent(context, DetalleRecetaActivity::class.java).apply {
                    // Pasar TODOS los detalles de la receta a DetalleRecetaActivity
                    putExtra("id", receta.id)
                    putExtra("nombre", receta.nombre)
                    putExtra("descripcion", receta.descripcion)
                    putExtra("ingredientes", receta.ingredientes)
                    putExtra("pasos", receta.pasos)
                    putExtra("imagenUri", receta.imagenUri)
                    putExtra("autor", receta.autor)
                    putExtra("fechaCreacion", receta.fechaCreacion) // Si lo tienes, pásalo
                }
                context.startActivity(intent)
            }

            // Lógica para el botón de favorito (la mantengo como la tenías)
            val yaEsFavorito = favoritosManager.obtenerFavoritos().any { it.id == receta.id }
            actualizarIconoFavorito(binding, yaEsFavorito)

            binding.favoritoButton.setOnClickListener {
                if (yaEsFavorito) {
                    favoritosManager.eliminarFavorito(receta)
                    Toast.makeText(context, "${receta.nombre} eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    actualizarIconoFavorito(binding, false)
                } else {
                    favoritosManager.guardarFavorito(receta)
                    Toast.makeText(context, "${receta.nombre} añadido a tus favoritos", Toast.LENGTH_SHORT).show()
                    actualizarIconoFavorito(binding, true)
                }
            }

            if (modoFavoritos) {
                binding.favoritoButton.setImageResource(R.drawable.eliminar)
                binding.favoritoButton.setOnClickListener {
                    favoritosManager.eliminarFavorito(receta)
                    Toast.makeText(context, "${receta.nombre} eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    onFavoritoEliminado?.invoke(receta)
                }
            }
        }

        private fun actualizarIconoFavorito(binding: ItemRecetaBinding, esFavorito: Boolean) {
            val icon = if (esFavorito) R.drawable.lleno else R.drawable.vacio
            binding.favoritoButton.setImageResource(icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = ItemRecetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position])
    }

    override fun getItemCount(): Int = recetas.size

    fun updateList(nuevaLista: List<Receta>) {
        recetas.clear()
        recetas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
```
3.  DetalleRecetaActivity.kt

Esta actividad es la que se abre cuando el usuario hace clic en una receta. Su función es recibir todos los datos de la receta desde el `Intent` y mostrarlos en sus respectivas vistas.

**Ubicación:** `app/src/main/java/com/example/mygastrogeni/ui/home/DetalleRecetaActivity.kt` (o la ruta de tu paquete para `ui.home`)

```kotlin
package com.example.mygastrogeni.ui.home // Ajusta el paquete si es diferente, ej: com.example.mygeniusgastroproyec.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewmodels.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.utils.SessionManager
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel // Asegúrate de que este import sea correcto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileNotFoundException

class DetalleRecetaActivity : AppCompatActivity() {

    private val recetaViewModel: RecetaViewModel by viewModels()

    // Firebase instances
    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_receta)

        // Obtener datos de la receta del Intent (con valores por defecto seguros)
        val nombre = intent.getStringExtra("nombre") ?: "Sin nombre"
        val descripcion = intent.getStringExtra("descripcion") ?: "Sin descripción"
        val ingredientes = intent.getStringExtra("ingredientes") ?: "Sin ingredientes"
        val pasos = intent.getStringExtra("pasos") ?: "Sin pasos"
        val imagenUri = intent.getStringExtra("imagenUri") ?: "" // Usar URI para imágenes cargadas
        val autor = intent.getStringExtra("autor") ?: ""
        val recetaId = intent.getStringExtra("id") ?: ""
        val fechaCreacion = intent.getStringExtra("fechaCreacion") ?: "" // Recibe la fecha

        val usuarioActual = SessionManager.getUsuario(this) // Obtener el usuario de la sesión

        // Inicializar vistas
        val imagenReceta: ImageView = findViewById(R.id.imageDetalle)
        val tituloReceta: TextView = findViewById(R.id.textTitulo)
        val descripcionReceta: TextView = findViewById(R.id.textDescripcion)
        val ingredientesReceta: TextView = findViewById(R.id.textIngredientes)
        val pasosReceta: TextView = findViewById(R.id.textPasos)
        val btnEliminar: Button = findViewById(R.id.btnEliminar)
        val btnEditar: Button = findViewById(R.id.btnEditar)
        val btnAgregarReceta: Button = findViewById(R.id.btnAgregarReceta) // Este botón podría ser redundante aquí

        // Mostrar datos de la receta
        tituloReceta.text = nombre
        descripcionReceta.text = descripcion
        ingredientesReceta.text = ingredientes
        pasosReceta.text = pasos

        // Cargar la imagen
        if (!imagenUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(imagenUri)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imagenReceta.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                Log.e("DetalleReceta", "Error al cargar la imagen URI: ${e.message}", e)
                imagenReceta.setImageResource(R.drawable.fav1) // Imagen por defecto
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DetalleReceta", "Error inesperado al cargar la imagen URI: ${e.message}", e)
                imagenReceta.setImageResource(R.drawable.fav1)
                Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si no hay imagenUri, usar imagen por defecto o considerar si deberías tener un imagenResId aquí.
            imagenReceta.setImageResource(R.drawable.fav1)
        }


        // Mostrar botones de editar/eliminar solo si el usuario actual es el autor
        mostrarBotonesEdicionEliminacion(btnEditar, btnEliminar, autor, usuarioActual, recetaId)

        // Eliminar receta
        btnEliminar.setOnClickListener { eliminarReceta(recetaId) }

        // Editar receta
        btnEditar.setOnClickListener { editarReceta(recetaId, nombre, ingredientes, pasos, imagenUri, fechaCreacion) }

        // Lógica para el botón agregar receta (si decides mantenerlo y darle una función)
        btnAgregarReceta.setOnClickListener {
            // Ejemplo: ir a la actividad de agregar nueva receta con campos prellenados
            // o simplemente ir a la actividad de agregar nueva receta vacía.
            val intent = Intent(this, EditarRecetaActivity::class.java) // Asumiendo que EditarRecetaActivity es también para agregar
            startActivity(intent)
        }
    }

    // Función para mostrar/ocultar botones de edición/eliminación
    private fun mostrarBotonesEdicionEliminacion(
        btnEditar: Button,
        btnEliminar: Button,
        autor: String,
        usuarioActual: String?,
        recetaId: String
    ) {
        if (autor == usuarioActual && recetaId.isNotEmpty()) {
            btnEditar.visibility = View.VISIBLE
            btnEliminar.visibility = View.VISIBLE
        } else {
            btnEditar.visibility = View.GONE
            btnEliminar.visibility = View.GONE
        }
    }

    // Función para eliminar receta
    private fun eliminarReceta(recetaId: String) {
        db.collection("recetas")
            .document(recetaId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                finish()  // Regresa a la actividad anterior
            }
            .addOnFailureListener { e ->
                Log.e("DetalleReceta", "Error al eliminar receta: ${e.message}", e)
                Toast.makeText(this, "Error al eliminar receta", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para editar receta
    private fun editarReceta(
        recetaId: String,
        nombre: String,
        ingredientes: String,
        pasos: String,
        imagenUri: String?,
        fechaCreacion: String // Añadimos fechaCreacion
    ) {
        val intent = Intent(this, EditarRecetaActivity::class.java).apply {
            putExtra("id", recetaId)
            putExtra("nombre", nombre)
            putExtra("ingredientes", ingredientes)
            putExtra("preparacion", pasos) // Usas "preparacion" aquí en el Intent
            putExtra("imagenUri", imagenUri)
            putExtra("modoEdicion", true)
            putExtra("fechaCreacion", fechaCreacion) // Pasar la fecha de creación
        }
        startActivity(intent)
        finish()
    }

    // La función agregarReceta de esta actividad podría ser redundante si ya tienes EditarRecetaActivity
    // que puede usarse para agregar también. Considera si necesitas este método aquí.
    private fun agregarReceta(nombre: String, ingredientes: String, descripcion: String, pasos: String) {
        val receta = hashMapOf(
            "nombre" to nombre,
            "ingredientes" to ingredientes,
            "descripcion" to descripcion,
            "pasos" to pasos,
            "autor" to mAuth.currentUser?.uid  // Obtener el UID del usuario actual
        )

        db.collection("recetas")
            .add(receta)
            .addOnSuccessListener { documentReference ->
                Log.d("DetalleReceta", "Receta agregada con ID: ${documentReference.id}")
                Toast.makeText(this, "Receta agregada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DetalleReceta", "Error al agregar receta: ${e.message}", e)
                Toast.makeText(this, "Error al agregar receta", Toast.LENGTH_SHORT).show()
            }
    }
}
```
Pasos Finales Importantes:
