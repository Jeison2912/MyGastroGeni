package com.example.mygastrogeni.ui.receta

import android.Manifest // Importa Manifest para acceder a los nombres de los permisos
import android.content.Intent
import android.content.pm.PackageManager // Para verificar el permiso
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat // Para ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.utils.SessionManager
import com.example.mygastrogeni.ui.viewmodel.RecetaViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AgregarFragment : Fragment() {

    private lateinit var editNombre: EditText
    private lateinit var editIngredientes: EditText
    private lateinit var editPreparacion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var imageView: ImageView

    private var imagenUri: Uri? = null

    private val recetaViewModel: RecetaViewModel by viewModels()

    // 1. ActivityResultLauncher para SOLICITAR permisos de almacenamiento
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido. Ahora puedes lanzar el selector de imágenes.
            Log.d("AgregarFragment", "Permiso de almacenamiento concedido. Lanzando imagePicker.")
            imagePicker.launch("image/*")
        } else {
            // Permiso denegado. Informa al usuario.
            Log.w("AgregarFragment", "Permiso de almacenamiento denegado.")
            Toast.makeText(requireContext(), "Permiso de almacenamiento es necesario para seleccionar imágenes.", Toast.LENGTH_LONG).show()
        }
    }

    // 2. imagePicker para seleccionar la imagen y persistir la URI
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                // **LA LÍNEA CLAVE: Tomar el permiso persistente de la URI (solo lectura)**
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)

                imagenUri = uri // Asigna la URI a la variable de la clase

                // Mostrar la imagen en el ImageView de previsualización
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                inputStream?.close()

                Log.d("AgregarFragment", "Permiso de URI persistente (solo lectura) tomado y imagen cargada para previsualización: $uri")

            } catch (e: SecurityException) {
                // Captura específica para problemas de permisos con la URI
                Log.e("AgregarFragment", "SecurityException al tomar permiso persistente de URI: ${e.message}", e)
                Toast.makeText(requireContext(), "Error de seguridad al acceder a la imagen. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                imageView.setImageResource(R.drawable.adjuntar)
                imagenUri = null
            } catch (e: Exception) {
                // Otros errores al cargar la imagen (ej. FileNotFoundException)
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
        editIngredientes = view.findViewById(R.id.editIngredientes)
        editPreparacion = view.findViewById(R.id.editPreparacion)
        btnGuardar = view.findViewById(R.id.btnGuardarReceta)
        imageView = view.findViewById(R.id.adjuntar)

        imageView.setOnClickListener {
            // Llama a este método para verificar y solicitar permisos antes de lanzar imagePicker
            checkAndRequestPermissions()
        }

        btnGuardar.setOnClickListener {
            guardarReceta()
        }

        // Asegúrate de que la imagen por defecto se muestre al iniciar el fragmento
        imageView.setImageResource(R.drawable.adjuntar)

        return view
    }

    // 3. Método para verificar y solicitar los permisos
    private fun checkAndRequestPermissions() {
        // Decide qué permiso solicitar basado en la versión de Android
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES // Para Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // Para Android 12 e inferiores
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                // El permiso ya está concedido, lanza el selector de imágenes directamente.
                Log.d("AgregarFragment", "Permiso ya concedido. Lanzando imagePicker.")
                imagePicker.launch("image/*")
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Explica al usuario por qué necesitas el permiso antes de solicitarlo de nuevo.
                Log.d("AgregarFragment", "Mostrando racional para el permiso.")
                Toast.makeText(requireContext(), "Necesitamos permiso para leer tus imágenes.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                // Solicitar el permiso (primera vez o después de "No volver a preguntar").
                Log.d("AgregarFragment", "Solicitando permiso por primera vez o después de 'No volver a preguntar'.")
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun guardarReceta() {
        val nombre = editNombre.text.toString().trim()
        val ingredientes = editIngredientes.text.toString().trim()
        val preparacion = editPreparacion.text.toString().trim()
        val imagenUriString = imagenUri?.toString() ?: ""
        val autor = SessionManager.getUsername(requireContext())
        val fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        if (nombre.isEmpty() || ingredientes.isEmpty() || preparacion.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        } else if (imagenUri == null) {
            Toast.makeText(requireContext(), "Por favor, selecciona una imagen para la receta", Toast.LENGTH_SHORT).show()
        }
        else {
            val nuevaReceta = com.example.mygastrogeni.ui.home.Receta(
                nombre = nombre,
                descripcion = "",
                ingredientes = ingredientes,
                pasos = preparacion,
                imagenUri = imagenUriString,
                autor = autor ?: "",
                fechaCreacion = fechaCreacion
            )

            recetaViewModel.insertar(nuevaReceta)

            Toast.makeText(requireContext(), "Receta guardada: $nombre", Toast.LENGTH_SHORT).show()

            editNombre.text.clear()
            editIngredientes.text.clear()
            editPreparacion.text.clear()
            imageView.setImageResource(R.drawable.adjuntar)
            imagenUri = null
        }
    }
}