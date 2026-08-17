package com.luispacheco.repartorouter.driver.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val CHOFER_ID_KEY = stringPreferencesKey("chofer_id")
    private val CHOFER_NOMBRE_KEY = stringPreferencesKey("chofer_nombre")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun guardarSesion(token: String, choferId: Long, nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[CHOFER_ID_KEY] = choferId.toString()
            prefs[CHOFER_NOMBRE_KEY] = nombre
        }
    }

    suspend fun obtenerToken(): String? {
        return tokenFlow.first()
    }

    suspend fun obtenerNombreChofer(): String? {
        return context.dataStore.data.map { it[CHOFER_NOMBRE_KEY] }.first()
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }

    // Usado únicamente por el interceptor de Retrofit, que corre en un hilo
    // de red y necesita el token de forma síncrona antes de lanzar la petición.
    fun obtenerTokenBloqueante(): String? {
        return runBlocking { obtenerToken() }
    }
}