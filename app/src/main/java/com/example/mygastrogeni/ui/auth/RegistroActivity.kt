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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var editUsuario: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        editUsuario = findViewById(R.id.editUsuario)
        editPassword = findViewById(R.id.editPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        mAuth = FirebaseAuth.getInstance()

        btnRegistrar.setOnClickListener {
            val email = editUsuario.text.toString()
            val password = editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            user?.uid?.let { uid ->
                                val userDocument = hashMapOf(
                                    "email" to email,
                                    "recetasFavoritas" to listOf<String>()
                                )

                                db.collection("usuarios")
                                    .document(uid)
                                    .set(userDocument)
                                    .addOnSuccessListener {
                                        Log.d("RegistroActivity", "Documento de usuario creado en Firestore")
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        // **Redirigir directamente a MainActivity**
                                        startActivity(Intent(this@RegistroActivity, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("RegistroActivity", "Error al crear documento de usuario en Firestore: ${e.message}", e)
                                        Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                                        user.delete()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}