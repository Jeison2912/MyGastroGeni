package com.example.mygastrogeni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mygastrogeni.databinding.ActivityMainBinding
import com.example.mygastrogeni.ui.auth.LoginActivity
import com.example.mygastrogeni.ui.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Aquí puedes agregar una acción personalizada", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_favorite, R.id.nav_perfil, R.id.nav_agregar
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            try {
                when (menuItem.itemId) {
                    R.id.nav_logout -> {
                        AlertDialog.Builder(this)
                            .setTitle("Cerrar sesión")
                            .setMessage("¿Estás seguro que deseas cerrar sesión?")
                            .setPositiveButton("Sí") { _, _ ->
                                logoutAndNavigateToLogin()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                        return@setNavigationItemSelectedListener true
                    }

                    else -> {
                        menuItem.isChecked = true
                        navController.navigate(menuItem.itemId)
                        drawerLayout.closeDrawers()
                        return@setNavigationItemSelectedListener true
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error en setNavigationItemSelectedListener: ${e.message}", e)
                e.printStackTrace()
                return@setNavigationItemSelectedListener false
            }
        }

        //  Verificación de sesión *después* de la inicialización
        mainScope.launch(Dispatchers.IO) {
            delay(500) // Pequeño delay
            withContext(Dispatchers.Main) {
                checkSessionAndNavigate()
            }
        }
    }

    private fun checkSessionAndNavigate() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.d("MainActivity", "Usuario no autenticado (Firebase). Iniciando LoginActivity")
            navigateToLogin()
        } else {
            Log.d("MainActivity", "Usuario autenticado (Firebase). Continuando en MainActivity")
            // Aquí puedes agregar lógica adicional si necesitas cargar datos del usuario al inicio
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun logoutAndNavigateToLogin() {
        mainScope.launch(Dispatchers.IO) {
            try {
                SessionManager.logout(this@MainActivity)
                withContext(Dispatchers.Main) {
                    navigateToLogin()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al cerrar sesión: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}