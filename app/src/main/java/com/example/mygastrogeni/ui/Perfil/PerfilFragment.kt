package com.example.mygastrogeni.ui.Perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mygastrogeni.R
import com.example.mygastrogeni.databinding.FragmentPerfilBinding
import com.example.mygastrogeni.ui.auth.LoginActivity
import com.example.mygastrogeni.ui.utils.SessionManager // Tu SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false

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

        setEditMode(false)

        loadSavedData()

        binding.buttonEditProfile.setOnClickListener {
            toggleEditMode()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun handlePickedImage(uri: Uri) {
        lifecycleScope.launch {
            SessionManager.saveImagenPerfil(requireContext(), uri.toString())
            Glide.with(this@PerfilFragment)
                .load(uri)
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(binding.imageViewProfilePic)
        }
    }

    private fun showNoImageSelectedToast() {
        Toast.makeText(requireContext(), "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
    }

    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun loadSavedData() {
        lifecycleScope.launch {
            val nombreGuardado = withContext(Dispatchers.IO) { SessionManager.getUsername(requireContext()) }
            val emailGuardado = withContext(Dispatchers.IO) { SessionManager.getEmail(requireContext()) }
            val descripcionGuardada = withContext(Dispatchers.IO) { SessionManager.getDescripcion(requireContext()) }
            val imagenUri = withContext(Dispatchers.IO) { SessionManager.getImagenPerfil(requireContext()) }

            binding.editTextFullName.setText(nombreGuardado)
            binding.editTextEmail.setText(emailGuardado)
            binding.editDescripcion.setText(descripcionGuardada)

            if (imagenUri.isNotEmpty()) {
                Glide.with(this@PerfilFragment)
                    .load(Uri.parse(imagenUri))
                    .placeholder(R.drawable.perfil)
                    .error(R.drawable.perfil)
                    .into(binding.imageViewProfilePic)
            } else {
                binding.imageViewProfilePic.setImageResource(R.drawable.perfil)
            }
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode

        setEditMode(isEditMode)

        if (!isEditMode) {
            updateProfile()
        }
    }

    private fun setEditMode(enable: Boolean) {
        binding.editTextFullName.isEnabled = enable
        binding.editTextEmail.isEnabled = enable
        binding.editDescripcion.isEnabled = enable

        if (enable) {
            binding.buttonEditProfile.text = getString(R.string.guardar_cambios)
            binding.imageViewProfilePic.setOnClickListener { launchImagePicker() }
        } else {
            binding.buttonEditProfile.text = getString(R.string.editar_perfil)
            binding.imageViewProfilePic.setOnClickListener(null)
        }
    }

    private fun updateProfile() {
        val nuevoNombre = binding.editTextFullName.text.toString().trim()
        val nuevoEmail = binding.editTextEmail.text.toString().trim()
        val nuevaDesc = binding.editDescripcion.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Completa al menos nombre y correo", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            SessionManager.saveUsername(requireContext(), nuevoNombre)
            SessionManager.saveEmail(requireContext(), nuevoEmail)
            SessionManager.saveDescripcion(requireContext(), nuevaDesc)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                loadSavedData()
            }
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            SessionManager.logout(requireContext())
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Sesión cerrada.", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}