package com.example.mygastrogeni.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SessionManager {

    private const val PREF_NAME = "my_app_prefs"
    private const val KEY_LOGGED_IN = "KEY_LOGGED_IN"
    private const val KEY_USERNAME = "KEY_USERNAME"
    private const val KEY_EMAIL = "KEY_EMAIL"
    private const val KEY_DESC = "KEY_DESC"
    private const val KEY_IMAGEN = "KEY_IMAGEN"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    suspend fun saveUsername(context: Context, username: String) {
        withContext(Dispatchers.IO) {
            try {
                getPrefs(context).edit {
                    putString(KEY_USERNAME, username)
                    putBoolean(KEY_LOGGED_IN, true)
                }
            } catch (e: Exception) {
                Log.e("SessionManager", "Error saving username: ${e.message}", e)
            }
        }
    }

    // ¡IMPORTANTE! Esta función DEBE llamarse getUsername, no getUsuario.
    fun getUsername(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, "") ?: ""
    }

    suspend fun saveEmail(context: Context, email: String) {
        withContext(Dispatchers.IO) {
            try {
                getPrefs(context).edit { putString(KEY_EMAIL, email) }
            } catch (e: Exception) {
                Log.e("SessionManager", "Error saving email: ${e.message}", e)
            }
        }
    }

    fun getEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "") ?: ""
    }

    suspend fun saveDescripcion(context: Context, descripcion: String) {
        withContext(Dispatchers.IO) {
            try {
                getPrefs(context).edit { putString(KEY_DESC, descripcion) }
            } catch (e: Exception) {
                Log.e("SessionManager", "Error saving description: ${e.message}", e)
            }
        }
    }

    fun getDescripcion(context: Context): String {
        return getPrefs(context).getString(KEY_DESC, "") ?: ""
    }

    suspend fun saveImagenPerfil(context: Context, uri: String) {
        withContext(Dispatchers.IO) {
            try {
                getPrefs(context).edit { putString(KEY_IMAGEN, uri) }
            } catch (e: Exception) {
                Log.e("SessionManager", "Error saving profile image URI: ${e.message}", e)
            }
        }
    }

    fun getImagenPerfil(context: Context): String {
        val uri = try {
            getPrefs(context).getString(KEY_IMAGEN, "") ?: ""
        } catch (e: Exception) {
            Log.e("SessionManager", "Error getting profile image URI: ${e.message}", e)
            ""
        }
        return uri
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        getPrefs(context).edit {
            clear()
            apply()
        }
    }
}