package com.example.proyectoZapateria.data.localstorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension para crear el DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Repositorio para manejar la persistencia de la sesión del usuario
 * usando DataStore (reemplazo moderno de SharedPreferences)
 */
@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ROLE_ID_KEY = longPreferencesKey("user_role_id")
        private val IS_LOGGED_IN_KEY = stringPreferencesKey("is_logged_in")
    }

    /**
     * Guarda los datos de sesión del usuario
     */
    suspend fun saveSession(
        userId: Long,
        username: String,
        userRole: String,
        userRoleId: Long
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[USER_ROLE_KEY] = userRole
            preferences[USER_ROLE_ID_KEY] = userRoleId
            preferences[IS_LOGGED_IN_KEY] = "true"
        }
    }

    /**
     * Obtiene el ID del usuario guardado
     */
    val userId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Obtiene el nombre de usuario guardado
     */
    val username: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    /**
     * Obtiene el rol del usuario guardado
     */
    val userRole: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    /**
     * Obtiene el ID del rol del usuario guardado
     */
    val userRoleId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[USER_ROLE_ID_KEY]
    }

    /**
     * Verifica si hay una sesión activa
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] == "true"
    }

    /**
     * Obtiene todos los datos de sesión en un solo objeto
     */
    val sessionData: Flow<SessionData?> = dataStore.data.map { preferences ->
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]
        val userRole = preferences[USER_ROLE_KEY]
        val userRoleId = preferences[USER_ROLE_ID_KEY]
        val isLoggedIn = preferences[IS_LOGGED_IN_KEY] == "true"

        if (isLoggedIn && userId != null && username != null && userRole != null && userRoleId != null) {
            SessionData(
                userId = userId,
                username = username,
                userRole = userRole,
                userRoleId = userRoleId
            )
        } else {
            null
        }
    }

    /**
     * Cierra la sesión eliminando todos los datos guardados
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * Clase de datos que representa la sesión del usuario
 */
data class SessionData(
    val userId: Long,
    val username: String,
    val userRole: String,
    val userRoleId: Long
)
