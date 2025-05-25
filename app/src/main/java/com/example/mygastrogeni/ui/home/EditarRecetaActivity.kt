package com.example.mygastrogeni.ui.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log // ¡Añadir esta importación!
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.utils.SessionManager
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import java.io.FileNotFoundException
import com.example.mygastrogeni.ui.utils.capitalizeWords // Importa la función de extensión

class EditarRecetaActivity : AppCompatActivity() {

    private lateinit var viewModel: RecetaViewModel
    private var imagenUri: String? = null
    private var recetaId: String = ""

    private lateinit var imageEditar: ImageView
    private lateinit var editNombre: EditText
    private lateinit var editDescripcion: EditText
    private lateinit var editIngredientes: EditText
    private lateinit var editPreparacion: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnActualizar: Button

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imagenUri = uri.toString()
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageEditar.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_receta)

        viewModel = ViewModelProvider(this)[RecetaViewModel::class.java]

        imageEditar = findViewById(R.id.imageEditar)
        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)
        editIngredientes = findViewById(R.id.editIngredientes)
        editPreparacion = findViewById(R.id.editPreparacion)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        btnActualizar = findViewById(R.id.btnActualizar)

        // Usa el string-array unificado (ej. categorias_array)
        val categoriasArray = resources.getStringArray(R.array.categorias_array)
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriasArray)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        // Recopilar datos de la receta si se está editando una existente
        recetaId = intent.getStringExtra("id") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val ingredientes = intent.getStringExtra("ingredientes") ?: ""
        val preparacion = intent.getStringExtra("pasos") ?: ""
        imagenUri = intent.getStringExtra("imagenUri")
        val categoriaRecetaExistente = intent.getStringExtra("categoria") ?: ""

        // Cargar datos en la UI
        editNombre.setText(nombre)
        editDescripcion.setText(descripcion)
        editIngredientes.setText(ingredientes)
        editPreparacion.setText(preparacion)

        // Seleccionar la categoría existente en el Spinner (importante normalizar)
        if (categoriaRecetaExistente.isNotEmpty()) {
            val categoriaNormalizadaExistente = categoriaRecetaExistente.trim().capitalizeWords()
            val categoriaIndex = categoriasArray.indexOf(categoriaNormalizadaExistente)
            if (categoriaIndex >= 0) {
                spinnerCategoria.setSelection(categoriaIndex)
            } else {
                // Opcional: si la categoría existente no está en el spinner, puedes añadirla programáticamente
                // o mostrar un mensaje. Por ahora, simplemente no la selecciona.
            }
        }

        if (!imagenUri.isNullOrEmpty()) {
            try {
                val inputStream = contentResolver.openInputStream(Uri.parse(imagenUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageEditar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageEditar.setImageResource(R.drawable.adjuntar)
            }
        }

        imageEditar.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnActualizar.setOnClickListener {
            val nuevoNombre = editNombre.text.toString().trim()
            val nuevaDescripcion = editDescripcion.text.toString().trim()
            val nuevosIngredientes = editIngredientes.text.toString().trim()
            val nuevaPreparacion = editPreparacion.text.toString().trim()

            // NORMALIZAR LA CATEGORÍA ANTES DE GUARDAR
            val nuevaCategoria = spinnerCategoria.selectedItem?.toString()?.trim()?.capitalizeWords() ?: ""

            Log.d("EditarReceta", "Categoría seleccionada (normalizada) para actualizar: '$nuevaCategoria'")

            if (nuevoNombre.isNotEmpty() && nuevaDescripcion.isNotEmpty() &&
                nuevosIngredientes.isNotEmpty() && nuevaPreparacion.isNotEmpty() &&
                nuevaCategoria.isNotEmpty()
            ) {
                // Desactivar el botón para evitar múltiples clics
                btnActualizar.isEnabled = false

                val recetaActualizada = Receta(
                    id = recetaId,
                    nombre = nuevoNombre,
                    descripcion = nuevaDescripcion,
                    ingredientes = nuevosIngredientes,
                    pasos = nuevaPreparacion,
                    imagenUri = imagenUri ?: "",
                    autor = SessionManager.getUsername(this),
                    categoria = nuevaCategoria // Usar la categoría normalizada
                )

                if (recetaId.isNotEmpty()) {
                    // Si recetaId no está vacío, significa que es una edición
                    viewModel.actualizar(recetaId, recetaActualizada,
                        onSuccess = {
                            Toast.makeText(this, "Receta actualizada con éxito", Toast.LENGTH_SHORT).show()
                            finish() // Cierra la actividad después de la actualización exitosa
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(this, "Error al actualizar receta: $errorMessage", Toast.LENGTH_LONG).show()
                            btnActualizar.isEnabled = true // Re-habilitar botón
                        }
                    )
                } else {
                    // Si recetaId está vacío, significa que es una nueva receta
                    viewModel.agregarReceta(recetaActualizada,
                        onSuccess = {
                            Toast.makeText(this, "Receta agregada con éxito", Toast.LENGTH_SHORT).show()
                            finish() // Cierra la actividad después de agregar exitosamente
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(this, "Error al agregar receta: $errorMessage", Toast.LENGTH_LONG).show()
                            btnActualizar.isEnabled = true // Re-habilitar botón
                        }
                    )
                }
            } else {
                Toast.makeText(this, "Completa todos los campos, incluida la categoría", Toast.LENGTH_LONG).show()
            }
        }
    }
}