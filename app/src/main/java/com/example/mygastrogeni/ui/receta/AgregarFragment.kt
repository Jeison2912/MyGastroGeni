package com.example.mygastrogeni.ui.receta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.home.Receta
import com.example.mygastrogeni.ui.utils.SessionManager
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.mygastrogeni.ui.utils.capitalizeWords // Importa la función de extensión

class AgregarFragment : Fragment() {

    private lateinit var editNombre: EditText
    private lateinit var editDescripcion: EditText // Si existe en fragment_agregar.xml, descomenta esta línea
    private lateinit var editIngredientes: EditText
    private lateinit var editPreparacion: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var imageView: ImageView

    private var imagenUri: Uri? = null

    private val recetaViewModel: RecetaViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("AgregarFragment", "Permiso de almacenamiento concedido. Lanzando imagePicker.")
            imagePicker.launch("image/*")
        } else {
            Log.w("AgregarFragment", "Permiso de almacenamiento denegado.")
            Toast.makeText(requireContext(), "Permiso de almacenamiento es necesario para seleccionar imágenes.", Toast.LENGTH_LONG).show()
        }
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)

                imagenUri = uri

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                inputStream?.close()

                Log.d("AgregarFragment", "Permiso de URI persistente (solo lectura) tomado y imagen cargada para previsualización: $uri")

            } catch (e: SecurityException) {
                Log.e("AgregarFragment", "SecurityException al tomar permiso persistente de URI: ${e.message}", e)
                Toast.makeText(requireContext(), "Error de seguridad al acceder a la imagen. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                imageView.setImageResource(R.drawable.adjuntar)
                imagenUri = null
            } catch (e: Exception) {
                Log.e("AgregarFragment", "Error general al cargar la imagen para previsualización: ${e.message}", e)
                Toast.makeText(requireContext(), "Error al cargar imagen. ${e.message}", Toast.LENGTH_LONG).show()
                imageView.setImageResource(R.drawable.adjuntar)
                imagenUri = null
            }
        } else {
            Log.d("AgregarFragment", "Selección de imagen cancelada o URI nula.")
            Toast.makeText(requireContext(), "No se seleccionó ninguna imagen.", Toast.LENGTH_SHORT).show()
            imageView.setImageResource(R.drawable.adjuntar)
            imagenUri = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar, container, false)

        editNombre = view.findViewById(R.id.editNombreReceta)
        // Descomenta la siguiente línea si tienes editDescripcion en tu fragment_agregar.xml
        // editDescripcion = view.findViewById(R.id.editDescripcion)
        editIngredientes = view.findViewById(R.id.editIngredientes)
        editPreparacion = view.findViewById(R.id.editPreparacion)
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria)
        btnGuardar = view.findViewById(R.id.btnGuardarReceta)
        imageView = view.findViewById(R.id.adjuntar)

        // Usa el string-array unificado (ej. categorias_array)
        val categoriasArray = resources.getStringArray(R.array.categorias_array)
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoriasArray)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        imageView.setOnClickListener {
            checkAndRequestPermissions()
        }

        btnGuardar.setOnClickListener {
            guardarReceta()
        }

        imageView.setImageResource(R.drawable.adjuntar)

        return view
    }

    private fun checkAndRequestPermissions() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("AgregarFragment", "Permiso ya concedido. Lanzando imagePicker.")
                imagePicker.launch("image/*")
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Log.d("AgregarFragment", "Mostrando racional para el permiso.")
                Toast.makeText(requireContext(), "Necesitamos permiso para leer tus imágenes.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                Log.d("AgregarFragment", "Solicitando permiso por primera vez o después de 'No volver a preguntar'.")
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun guardarReceta() {
        val nombre = editNombre.text.toString().trim()
        val descripcion = if (::editDescripcion.isInitialized) editDescripcion.text.toString().trim() else "" // Manejo si editDescripcion no está inicializada
        val ingredientes = editIngredientes.text.toString().trim()
        val preparacion = editPreparacion.text.toString().trim()
        val imagenUriString = imagenUri?.toString() ?: ""
        val autor = SessionManager.getUsername(requireContext())
        val fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // NORMALIZAR LA CATEGORÍA ANTES DE GUARDAR
        val categoriaSeleccionada = spinnerCategoria.selectedItem?.toString()?.trim()?.capitalizeWords() ?: ""

        Log.d("AgregarFragment", "Categoría seleccionada (normalizada) para guardar: '$categoriaSeleccionada'")

        if (nombre.isEmpty() || ingredientes.isEmpty() || preparacion.isEmpty() || categoriaSeleccionada.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos, incluida la categoría", Toast.LENGTH_SHORT).show()
        } else if (imagenUri == null) {
            Toast.makeText(requireContext(), "Por favor, selecciona una imagen para la receta", Toast.LENGTH_SHORT).show()
        }
        else {
            // Desactivar el botón para evitar múltiples clics
            btnGuardar.isEnabled = false

            val nuevaReceta = Receta(
                nombre = nombre,
                descripcion = descripcion,
                ingredientes = ingredientes,
                pasos = preparacion,
                imagenUri = imagenUriString,
                autor = autor ?: "",
                fechaCreacion = fechaCreacion,
                categoria = categoriaSeleccionada // Usar la categoría normalizada
            )

            recetaViewModel.agregarReceta(nuevaReceta,
                onSuccess = {
                    Toast.makeText(requireContext(), "Receta agregada con éxito", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                    findNavController().popBackStack() // Navegar de vuelta al HomeFragment
                    btnGuardar.isEnabled = true // Re-habilitar botón
                },
                onFailure = { errorMessage ->
                    Toast.makeText(requireContext(), "Error al agregar receta: $errorMessage", Toast.LENGTH_LONG).show()
                    btnGuardar.isEnabled = true // Re-habilitar botón en caso de error
                }
            )
        }
    }

    private fun limpiarCampos() {
        editNombre.text.clear()
        // Si tienes editDescripcion en tu layout, descomenta la siguiente línea:
        // if (::editDescripcion.isInitialized) editDescripcion.text.clear()
        editIngredientes.text.clear()
        editPreparacion.text.clear()
        spinnerCategoria.setSelection(0) // Restablecer el spinner a la primera opción
        imageView.setImageResource(R.drawable.adjuntar)
        imagenUri = null
    }
}