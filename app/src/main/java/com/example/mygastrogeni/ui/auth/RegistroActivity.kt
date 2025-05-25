package com.example.mygastrogeni.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Importar lifecycleScope
import com.example.mygastrogeni.MainActivity // Asegúrate de que esta es tu actividad principal después del registro
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.models.User
import com.example.mygastrogeni.ui.utils.SessionManager // Importar SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RegistroActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage

    private lateinit var imageViewProfilePic: CircleImageView
    private lateinit var btnSelectProfilePic: Button // Este botón debería estar en tu layout de registro para seleccionar la imagen
    private lateinit var editTextFullName: EditText
    private lateinit var editTextConfirmPassword: EditText // <-- ¡LÍNEA CORREGIDA! Eliminada la "var" duplicada
    private lateinit var textViewLogin: TextView

    private var selectedImageUri: Uri? = null

    // Launcher para seleccionar imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageViewProfilePic.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inicialización de vistas (asegúrate de que los IDs en activity_registro.xml coincidan)
        imageViewProfilePic = findViewById(R.id.imageViewProfilePic)
        btnSelectProfilePic = findViewById(R.id.btnSelectProfilePic) // Este ID debe existir en tu layout de registro
        editTextFullName = findViewById(R.id.editTextFullName)
        editEmail = findViewById(R.id.editTextEmail)
        editPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        btnRegistrar = findViewById(R.id.buttonRegister) // Este ID debe existir en tu layout de registro
        textViewLogin = findViewById(R.id.textViewLogin) // Este ID debe existir en tu layout de registro

        // Listeners
        btnSelectProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegistrar.setOnClickListener {
            registerUser()
        }

        textViewLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val fullName = editTextFullName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val password = editPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()

        // Validaciones...
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear usuario con Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = mAuth.currentUser
                    val userId = firebaseUser?.uid ?: ""

                    if (selectedImageUri != null) {
                        subirImagenPerfilYGuardarDatos(userId, fullName, email, selectedImageUri!!)
                    } else {
                        // Si no se selecciona imagen, se guarda sin URL de imagen de perfil
                        guardarDatosUsuarioEnFirestore(userId, fullName, email, "", "")
                    }
                } else {
                    // Manejo de errores de autenticación
                    Log.e("RegistroActivity", "Error de registro en Auth: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun subirImagenPerfilYGuardarDatos(userId: String, fullName: String, email: String, imageUri: Uri) {
        val tempFileUri = copiarImagenATemp(imageUri)
        if (tempFileUri == null) {
            Toast.makeText(this, "Error al preparar la imagen para subir. Registrando sin imagen.", Toast.LENGTH_SHORT).show()
            guardarDatosUsuarioEnFirestore(userId, fullName, email, "", "") // Guardar sin imagen si falla la preparación
            return
        }

        // Subir imagen a Firebase Storage
        val storageRef = storage.reference.child("profile_pictures/${userId}.jpg")

        storageRef.putFile(tempFileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val profileImageUrl = downloadUri.toString()
                    guardarDatosUsuarioEnFirestore(userId, fullName, email, profileImageUrl, "")
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegistroActivity", "Error al subir imagen de perfil: ${e.message}", e)
                Toast.makeText(this, "Error al subir imagen de perfil. Registrando sin imagen.", Toast.LENGTH_SHORT).show()
                guardarDatosUsuarioEnFirestore(userId, fullName, email, "", "") // Guardar sin imagen si falla la subida
            }
    }

    // Función auxiliar para copiar la imagen seleccionada a un archivo temporal
    private fun copiarImagenATemp(uri: Uri): Uri? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File.createTempFile("profile_pic_", ".jpg", cacheDir)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e("RegistroActivity", "Error al copiar imagen a cache: ${e.message}", e)
            null
        }
    }

    private fun guardarDatosUsuarioEnFirestore(userId: String, fullName: String, email: String, profileImageUrl: String, description: String) {
        val user = User(
            uid = userId,
            fullName = fullName,
            email = email,
            profileImageUrl = profileImageUrl,
            description = description
        )

        // Guardar datos del usuario en Firestore
        db.collection("usuarios") // Asegúrate que el nombre de tu colección sea "usuarios"
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("RegistroActivity", "Documento de usuario creado en Firestore")
                Toast.makeText(this, "Registro exitoso y perfil creado.", Toast.LENGTH_SHORT).show()

                // Guardar datos en SessionManager también
                lifecycleScope.launch { // Usamos lifecycleScope.launch porque estamos en una Activity
                    SessionManager.saveUsername(this@RegistroActivity, fullName)
                    SessionManager.saveEmail(this@RegistroActivity, email)
                    SessionManager.saveImagenPerfil(this@RegistroActivity, profileImageUrl)
                    SessionManager.saveDescripcion(this@RegistroActivity, description) // Guardar descripción (vacía inicialmente)
                }

                // Redirigir a la actividad principal y limpiar la pila de actividades
                val intent = Intent(this@RegistroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("RegistroActivity", "Error al guardar datos de usuario en Firestore: ${e.message}", e)
                Toast.makeText(this, "Error al guardar datos de usuario.", Toast.LENGTH_SHORT).show()
                // Si falla guardar en Firestore, se puede considerar eliminar el usuario de Authentication
                mAuth.currentUser?.delete()
                Toast.makeText(this, "Error en el perfil. Por favor, intenta registrarte de nuevo.", Toast.LENGTH_LONG).show()
            }
    }
}