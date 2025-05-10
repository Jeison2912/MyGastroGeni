package com.example.mygastrogeni.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mygastrogeni.MainActivity
import com.example.mygastrogeni.R
import com.example.mygastrogeni.ui.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class LoginActivity : AppCompatActivity() {

    private lateinit var editUsuario: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnIrARegistro: Button
    private lateinit var mAuth: FirebaseAuth
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editUsuario = findViewById(R.id.editUsuario)
        editPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnIrARegistro = findViewById(R.id.btnIrARegistro)

        mAuth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        btnIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // Verificar si se acaba de registrar un usuario
        val justRegistered = intent.getBooleanExtra("justRegistered", false)

        //  Verificar si ya hay sesión iniciada (solo si no se acaba de registrar)
        if (!justRegistered) {
            mainScope.launch(Dispatchers.IO) {
                delay(500) // Pequeño delay
                withContext(Main) {
                    checkCurrentUser()
                }
            }
        }
    }

    private fun checkCurrentUser() {
        if (mAuth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun iniciarSesion() {
        val email = editUsuario.text.toString()
        val password = editPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mainScope.launch(Dispatchers.IO) {
                        SessionManager.saveUsername(this@LoginActivity, email)
                        withContext(Main) {
                            navigateToMainActivity()
                        }
                    }
                } else {
                    Log.w("LoginActivity", "Inicio de sesión fallido", task.exception)
                    Toast.makeText(baseContext, "Fallo en la autenticación: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}