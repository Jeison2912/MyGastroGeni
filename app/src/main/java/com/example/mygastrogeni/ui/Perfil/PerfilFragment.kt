package com.example.mygastrogeni.ui.Perfil

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope //  Usamos lifecycleScope
import com.example.mygastrogeni.databinding.FragmentPerfilBinding
import com.example.mygastrogeni.ui.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    //  ActivityResultLauncher para seleccionar la imagen
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handlePickedImage(it) } ?: showNoImageSelectedToast()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedData()  //  Cargamos los datos al crear la vista
        binding.imagePerfil.setOnClickListener { launchImagePicker() }
        binding.buttonEditarPerfil.setOnClickListener { updateProfile() }
    }

    //  Función para manejar la imagen seleccionada
    private fun handlePickedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            SessionManager.saveImagenPerfil(requireContext(), uri.toString())
            withContext(Dispatchers.Main) {
                binding.imagePerfil.setImageURI(uri)
            }
        }
    }

    //  Función para mostrar el Toast de "No se seleccionó imagen"
    private fun showNoImageSelectedToast() {
        Toast.makeText(requireContext(), "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
    }

    //  Función para lanzar el selector de imágenes
    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    //  Función para cargar los datos guardados
    private fun loadSavedData() {
        lifecycleScope.launch {  //  Usamos lifecycleScope.launch sin Dispatchers.Main porque ya estamos en el hilo principal
            val nombreGuardado = withContext(Dispatchers.IO) { SessionManager.getUsuario(requireContext()) }
            val emailGuardado = withContext(Dispatchers.IO) { SessionManager.getEmail(requireContext()) }
            val descripcionGuardada = withContext(Dispatchers.IO) { SessionManager.getDescripcion(requireContext()) }
            val imagenUri = withContext(Dispatchers.IO) { SessionManager.getImagenPerfil(requireContext()) }

            binding.editNombre.setText(nombreGuardado)
            binding.editEmail.setText(emailGuardado)
            binding.editDescripcion.setText(descripcionGuardada)

            if (imagenUri.isNotEmpty()) {
                binding.imagePerfil.setImageURI(Uri.parse(imagenUri))
            }
        }
    }

    //  Función para actualizar el perfil
    private fun updateProfile() {
        val nuevoNombre = binding.editNombre.text.toString().trim()
        val nuevoEmail = binding.editEmail.text.toString().trim()
        val nuevaDesc = binding.editDescripcion.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Completa al menos nombre y correo", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            SessionManager.saveUsername(requireContext(), nuevoNombre)
            SessionManager.saveEmail(requireContext(), nuevoEmail)
            SessionManager.saveDescripcion(requireContext(), nuevaDesc)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}