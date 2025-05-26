package com.example.mygastrogeni.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mygastrogeni.MainActivity
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.models.User
import com.example.mygastrogeni.ui.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextFullName: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var textViewLogin: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        mAuth = FirebaseAuth.getInstance()

        editTextFullName = findViewById(R.id.editTextFullName)
        editEmail = findViewById(R.id.editTextEmail)
        editPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        btnRegistrar = findViewById(R.id.buttonRegister)
        textViewLogin = findViewById(R.id.textViewLogin)


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


        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = mAuth.currentUser
                    val userId = firebaseUser?.uid ?: ""


                    guardarDatosUsuarioEnFirestore(userId, fullName, email, "", "")
                } else {
                    // Manejo de errores de autenticación
                    Log.e("RegistroActivity", "Error de registro en Auth: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
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


        db.collection("usuarios")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("RegistroActivity", "Documento de usuario creado en Firestore")
                Toast.makeText(this, "Registro exitoso y perfil creado.", Toast.LENGTH_SHORT).show()


                lifecycleScope.launch {
                    SessionManager.saveUsername(this@RegistroActivity, fullName)
                    SessionManager.saveEmail(this@RegistroActivity, email)
                    SessionManager.saveImagenPerfil(this@RegistroActivity, profileImageUrl)
                    SessionManager.saveDescripcion(this@RegistroActivity, description)
                }
               val intent = Intent(this@RegistroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("RegistroActivity", "Error al guardar datos de usuario en Firestore: ${e.message}", e)
                Toast.makeText(this, "Error al guardar datos de usuario.", Toast.LENGTH_SHORT).show()

                mAuth.currentUser?.delete()
                Toast.makeText(this, "Error en el perfil. Por favor, intenta registrarte de nuevo.", Toast.LENGTH_LONG).show()
            }
    }
}