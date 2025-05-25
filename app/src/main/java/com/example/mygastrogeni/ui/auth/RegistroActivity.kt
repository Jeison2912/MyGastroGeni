package com.example.mygastrogeni.ui.auth

import android.content.Intent
import android.net.Uri // Se puede eliminar si no se usa en otro lugar, pero no causa daño
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
// import androidx.activity.result.contract.ActivityResultContracts // ELIMINADO: No se necesita el Launcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mygastrogeni.MainActivity
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.models.User // Asumo que esta es tu clase de modelo de usuario
import com.example.mygastrogeni.ui.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // ELIMINADO: No se necesita Firebase Storage si no subes imágenes
import kotlinx.coroutines.Dispatchers // Se puede eliminar si no se usa explícitamente en el ámbito
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Se puede eliminar si no se usa explícitamente en el ámbito
// import de.hdodenhof.circleimageview.CircleImageView // ELIMINADO: No se necesita la vista de imagen circular
// import java.io.File // ELIMINADO: No se necesita para manejar archivos temporales
// import java.io.FileOutputStream // ELIMINADO: No se necesita para manejar archivos temporales

class RegistroActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    // private lateinit var storage: FirebaseStorage // ELIMINADO: No se necesita Firebase Storage

    // ELIMINADO: Variables relacionadas con la imagen
    // private lateinit var imageViewProfilePic: CircleImageView
    // private lateinit var btnSelectProfilePic: Button
    private lateinit var editTextFullName: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var textViewLogin: TextView

    // ELIMINADO: Variable para la URI de la imagen seleccionada
    // private var selectedImageUri: Uri? = null

    // ELIMINADO: Launcher para seleccionar imagen
    // private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    //     if (uri != null) {
    //         selectedImageUri = uri
    //         imageViewProfilePic.setImageURI(uri)
    //     }
    // }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        mAuth = FirebaseAuth.getInstance()
        // storage = FirebaseStorage.getInstance() // ELIMINADO: No se inicializa Storage

        // Inicialización de vistas (¡ATENCIÓN! Asegúrate que los IDs de imagen ya no están en tu XML)
        // ELIMINADO: Vistas de imagen
        // imageViewProfilePic = findViewById(R.id.imageViewProfilePic)
        // btnSelectProfilePic = findViewById(R.id.btnSelectProfilePic)

        editTextFullName = findViewById(R.id.editTextFullName)
        editEmail = findViewById(R.id.editTextEmail)
        editPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        btnRegistrar = findViewById(R.id.buttonRegister)
        textViewLogin = findViewById(R.id.textViewLogin)

        // ELIMINADO: Listener para seleccionar imagen
        // btnSelectProfilePic.setOnClickListener {
        //     pickImageLauncher.launch("image/*")
        // }

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

                    // Ya no hay selección de imagen, siempre se guarda sin URL de imagen de perfil
                    guardarDatosUsuarioEnFirestore(userId, fullName, email, "", "") // profileImageUrl ahora es siempre ""
                } else {
                    // Manejo de errores de autenticación
                    Log.e("RegistroActivity", "Error de registro en Auth: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ELIMINADO: Métodos relacionados con la subida de imagen
    // private fun subirImagenPerfilYGuardarDatos(userId: String, fullName: String, email: String, imageUri: Uri) { ... }
    // private fun copiarImagenATemp(uri: Uri): Uri? { ... }

    private fun guardarDatosUsuarioEnFirestore(userId: String, fullName: String, email: String, profileImageUrl: String, description: String) {
        val user = User(
            uid = userId,
            fullName = fullName,
            email = email,
            // profileImageUrl ya es "" aquí, porque siempre se le pasa así ahora
            profileImageUrl = profileImageUrl,
            description = description
        )

        // Guardar datos del usuario en Firestore
        db.collection("usuarios")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("RegistroActivity", "Documento de usuario creado en Firestore")
                Toast.makeText(this, "Registro exitoso y perfil creado.", Toast.LENGTH_SHORT).show()

                // Guardar datos en SessionManager (profileImageUrl también será "" aquí)
                lifecycleScope.launch {
                    SessionManager.saveUsername(this@RegistroActivity, fullName)
                    SessionManager.saveEmail(this@RegistroActivity, email)
                    SessionManager.saveImagenPerfil(this@RegistroActivity, profileImageUrl) // Se guardará como ""
                    SessionManager.saveDescripcion(this@RegistroActivity, description)
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