package com.example.proyectoZapateria.viewmodel.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.domain.validation.validateProfileEmail
import com.example.proyectoZapateria.domain.validation.validateProfileName
import com.example.proyectoZapateria.domain.validation.validateProfilePhone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

data class AdminPerfilUiState(
    val nombre: String = "",
    val apellido: String = "",
    val rut: String = "",
    val email: String = "",
    val telefono: String = "",
    val calle: String = "",
    val numeroPuerta: String = "",
    val username: String = "",
    val rol: String = "",
    val categoria: String = "",
    val profileImageUri: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminPerfilViewModel @Inject constructor(
    private val personaRemoteRepository: PersonaRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val usuarioRemoteRepository: UsuarioRemoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPerfilUiState())
    val uiState: StateFlow<AdminPerfilUiState> = _uiState.asStateFlow()

    // Estado de edición
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    // Campos editables
    private val _editNombre = MutableStateFlow("")
    private val _editApellido = MutableStateFlow("")
    private val _editEmail = MutableStateFlow("")
    private val _editTelefono = MutableStateFlow("")
    private val _editCalle = MutableStateFlow("")
    private val _editNumeroPuerta = MutableStateFlow("")

    val editNombre = _editNombre.asStateFlow()
    val editApellido = _editApellido.asStateFlow()
    val editEmail = _editEmail.asStateFlow()
    val editTelefono = _editTelefono.asStateFlow()
    val editCalle = _editCalle.asStateFlow()
    val editNumeroPuerta = _editNumeroPuerta.asStateFlow()

    private var personaActual: PersonaDTO? = null

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    data class PhotoLoadTrigger(val idPersona: Long, val timestamp: Long = System.currentTimeMillis())

    private val _needsPhotoLoad = MutableStateFlow<PhotoLoadTrigger?>(null)
    val needsPhotoLoad: StateFlow<PhotoLoadTrigger?> = _needsPhotoLoad.asStateFlow()

    init {
        cargarPerfil()
    }


    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val currentUser = authRemoteRepository.currentUser.value
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay sesión activa"
                    )
                    return@launch
                }

                // Obtener persona desde API
                val result = personaRemoteRepository.obtenerPersonaPorId(currentUser.idPersona)
                if (result.isSuccess) {
                    val persona = result.getOrNull()
                    if (persona != null) {
                        personaActual = persona

                        _uiState.value = AdminPerfilUiState(
                            nombre = persona.nombre ?: "",
                            apellido = persona.apellido ?: "",
                            rut = persona.rut ?: "",
                            email = persona.email ?: "",
                            telefono = persona.telefono ?: "",
                            calle = persona.calle ?: "",
                            numeroPuerta = persona.numeroPuerta ?: "",
                            username = persona.username ?: "",
                            rol = currentUser.nombreRol,
                            categoria = "",
                            isLoading = false
                        )

                        // Poblar campos editables
                        _editNombre.value = persona.nombre ?: ""
                        _editApellido.value = persona.apellido ?: ""
                        _editEmail.value = persona.email ?: ""
                        _editTelefono.value = persona.telefono ?: ""
                        _editCalle.value = persona.calle ?: ""
                        _editNumeroPuerta.value = persona.numeroPuerta ?: ""

                        // Marcar que necesita cargar la foto (con timestamp para forzar recarga)
                        _needsPhotoLoad.value = PhotoLoadTrigger(currentUser.idPersona)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No se encontró información del usuario"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al obtener persona: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }

    fun reload() {
        cargarPerfil()
    }

    fun startEdit() {
        _isEditing.value = true
    }

    fun cancelEdit() {
        // Restaurar valores originales
        personaActual?.let { p ->
            _editNombre.value = p.nombre ?: ""
            _editApellido.value = p.apellido ?: ""
            _editEmail.value = p.email ?: ""
            _editTelefono.value = p.telefono ?: ""
            _editCalle.value = p.calle ?: ""
            _editNumeroPuerta.value = p.numeroPuerta ?: ""
        }
        _isEditing.value = false
    }

    fun updateEditField(
        nombre: String? = null,
        apellido: String? = null,
        email: String? = null,
        telefono: String? = null,
        calle: String? = null,
        numeroPuerta: String? = null
    ) {
        nombre?.let { _editNombre.value = it }
        apellido?.let { _editApellido.value = it }
        email?.let { _editEmail.value = it }
        telefono?.let { _editTelefono.value = it }
        calle?.let { _editCalle.value = it }
        numeroPuerta?.let { _editNumeroPuerta.value = it }
    }

    fun guardarCambios(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Validaciones
                val nombreError = validateProfileName(_editNombre.value)
                if (nombreError != null) {
                    callback(false, nombreError)
                    return@launch
                }

                val apellidoError = validateProfileName(_editApellido.value)
                if (apellidoError != null) {
                    callback(false, apellidoError)
                    return@launch
                }

                val emailError = validateProfileEmail(_editEmail.value)
                if (emailError != null) {
                    callback(false, emailError)
                    return@launch
                }

                if (_editTelefono.value.isNotBlank()) {
                    val telefonoError = validateProfilePhone(_editTelefono.value)
                    if (telefonoError != null) {
                        callback(false, telefonoError)
                        return@launch
                    }
                }

                val persona = personaActual?.copy(
                    nombre = _editNombre.value,
                    apellido = _editApellido.value,
                    email = _editEmail.value.ifBlank { null },
                    telefono = _editTelefono.value.ifBlank { null },
                    calle = _editCalle.value.ifBlank { null },
                    numeroPuerta = _editNumeroPuerta.value.ifBlank { null }
                )

                if (persona != null) {
                    val result = personaRemoteRepository.actualizarPersona(persona.idPersona!!, persona)
                    if (result.isSuccess) {
                        personaActual = result.getOrNull()

                        // Actualizar UI
                        _uiState.value = _uiState.value.copy(
                            nombre = persona.nombre ?: "",
                            apellido = persona.apellido ?: "",
                            email = persona.email ?: "",
                            telefono = persona.telefono ?: "",
                            calle = persona.calle ?: "",
                            numeroPuerta = persona.numeroPuerta ?: ""
                        )

                        // Actualizar usuario en memoria (AuthRemoteRepository)
                        val currentUser = authRemoteRepository.currentUser.value
                        if (currentUser != null) {
                            val updatedUsuario = currentUser.copy(
                                nombre = persona.nombre ?: "",
                                apellido = persona.apellido ?: "",
                                email = persona.email ?: "",
                                telefono = persona.telefono ?: "",
                                calle = persona.calle ?: "",
                                numeroPuerta = persona.numeroPuerta ?: ""
                            )
                            authRemoteRepository.setCurrentUser(updatedUsuario)
                        }

                        _isEditing.value = false
                        callback(true, null)
                    } else {
                        callback(false, "Error al actualizar: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    callback(false, "Error: no se pudo actualizar")
                }
            } catch (e: Exception) {
                callback(false, "Error al guardar: ${e.message}")
            }
        }
    }

    fun getNombreCompleto(): String {
        return "${_uiState.value.nombre} ${_uiState.value.apellido}"
    }

    fun onProfileImageSelected(context: Context, file: File) {
        viewModelScope.launch {
            try {
                val currentUser = authRemoteRepository.currentUser.value
                if (currentUser == null) {
                    Log.e("AdminPerfilVM", "No hay usuario autenticado")
                    return@launch
                }

                _profileImageUri.value = Uri.fromFile(file)

                // Comprimir la imagen antes de subir (máximo 1024x1024, calidad 80%)
                val compressedFile = withContext(Dispatchers.IO) {
                    com.example.proyectoZapateria.util.ImageCompressor.compressImageFromFile(
                        context, file, maxWidth = 1024, maxHeight = 1024, quality = 80
                    )
                }

                if (compressedFile == null) {
                    Log.e("AdminPerfilVM", "Error al comprimir imagen")
                    return@launch
                }

                val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                val fotoPart = okhttp3.MultipartBody.Part.createFormData(
                    "foto",
                    compressedFile.name,
                    requestFile
                )

                val result = usuarioRemoteRepository.subirFotoPerfil(currentUser.idPersona, fotoPart)

                result.onSuccess {
                    // Eliminar archivo comprimido temporal
                    compressedFile.delete()
                    kotlinx.coroutines.delay(500)
                    cargarFotoPerfil(context, currentUser.idPersona)
                }.onFailure { error ->
                    compressedFile.delete()
                    Log.e("AdminPerfilVM", "Error al subir foto: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("AdminPerfilVM", "Error al procesar imagen: ${e.message}")
            }
        }
    }

    fun cargarFotoPerfil(context: Context, idPersona: Long) {
        viewModelScope.launch {
            try {
                val result = usuarioRemoteRepository.obtenerFotoPerfil(idPersona)
                result.onSuccess { fotoBytes ->
                    if (fotoBytes != null && fotoBytes.isNotEmpty()) {
                        _profileImageUri.value?.let { oldUri ->
                            try {
                                val oldFile = File(oldUri.path ?: "")
                                if (oldFile.exists()) oldFile.delete()
                            } catch (e: Exception) {
                                // Silenciar error al eliminar archivo anterior
                            }
                        }

                        val timestamp = System.currentTimeMillis()
                        val tempFile = File(context.cacheDir, "profile_${idPersona}_${timestamp}.jpg")
                        tempFile.writeBytes(fotoBytes)
                        _profileImageUri.value = Uri.fromFile(tempFile)
                        _uiState.value = _uiState.value.copy(profileImageUri = tempFile.absolutePath)
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminPerfilVM", "Error al cargar foto: ${e.message}")
            }
        }
    }
}
