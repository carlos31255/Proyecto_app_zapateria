package com.example.proyectoZapateria.viewmodel.transportista
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import com.example.proyectoZapateria.data.model.UsuarioCompleto
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.TransportistaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.domain.validation.validateProfileName
import com.example.proyectoZapateria.domain.validation.validateProfilePhone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

data class TransportistaPerfilUiState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val licencia: String = "",
    val vehiculo: String = "",
    val totalEntregas: Int = 0,
    val entregasCompletadas: Int = 0,
    val entregasPendientes: Int = 0,
    // Métricas relacionadas a boletas (ventas) asociadas a las entregas
    val totalBoletas: Int = 0,
    val totalVentasImporte: Int = 0,
    val profileImageUri: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TransportistaPerfilViewModel @Inject constructor(
    private val transportistaRemoteRepository: TransportistaRemoteRepository,
    private val entregasRepository: EntregasRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val personaRemoteRepository: PersonaRemoteRepository,
    private val usuarioRemoteRepository: UsuarioRemoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaPerfilUiState())
    val uiState: StateFlow<TransportistaPerfilUiState> = _uiState.asStateFlow()


    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _editNombre = MutableStateFlow("")
    private val _editApellido = MutableStateFlow("")
    private val _editTelefono = MutableStateFlow("")
    private val _editLicencia = MutableStateFlow("")
    private val _editVehiculo = MutableStateFlow("")

    val editNombre = _editNombre.asStateFlow()
    val editApellido = _editApellido.asStateFlow()
    val editTelefono = _editTelefono.asStateFlow()
    val editLicencia = _editLicencia.asStateFlow()
    val editVehiculo = _editVehiculo.asStateFlow()

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    // Usar un contador para forzar la recarga cada vez
    data class PhotoLoadTrigger(val idPersona: Long, val timestamp: Long = System.currentTimeMillis())

    private val _needsPhotoLoad = MutableStateFlow<PhotoLoadTrigger?>(null)
    val needsPhotoLoad: StateFlow<PhotoLoadTrigger?> = _needsPhotoLoad.asStateFlow()

    init {
        cargarPerfil()
    }


    private var currentTransportistaDto: TransportistaDTO? = null

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUser = authRemoteRepository.currentUser.value
                if (currentUser == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No hay sesión activa") }
                    return@launch
                }

                val personaId = currentUser.idPersona

                // Obtener Transportista
                currentTransportistaDto = try {
                    val res = transportistaRemoteRepository.obtenerPorPersona(personaId)
                    res.getOrNull()
                } catch (_: Exception) { null }

                // Obtener Estadísticas
                val idParaEntregas = currentTransportistaDto?.idTransportista ?: personaId
                val entregasResult = entregasRepository.obtenerEntregasPorTransportista(idParaEntregas)
                var total = 0
                var completadas = 0
                var pendientes = 0

                if (entregasResult.isSuccess) {
                    val lista = entregasResult.getOrNull() ?: emptyList()
                    total = lista.size
                    completadas = lista.count { it.estadoEntrega.equals("completada", true) || it.estadoEntrega.equals("entregada", true) }
                    pendientes = lista.count { it.estadoEntrega.equals("pendiente", true) }
                }

                // Actualizar UI
                _uiState.update {
                    it.copy(
                        nombre = currentUser.nombre,
                        apellido = currentUser.apellido,
                        email = currentUser.email,
                        telefono = currentUser.telefono,
                        licencia = currentTransportistaDto?.licencia ?: "",
                        vehiculo = currentTransportistaDto?.tipoVehiculo ?: "",
                        totalEntregas = total,
                        entregasCompletadas = completadas,
                        entregasPendientes = pendientes,
                        isLoading = false
                    )
                }

                // Marcar que necesita cargar la foto (con timestamp para forzar recarga)
                _needsPhotoLoad.value = PhotoLoadTrigger(personaId)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
            }
        }
    }

    fun startEdit() {
        val current = _uiState.value
        _editNombre.value = current.nombre
        _editApellido.value = current.apellido
        _editTelefono.value = current.telefono
        _editLicencia.value = current.licencia
        _editVehiculo.value = current.vehiculo
        _isEditing.value = true
    }

    fun cancelEdit() {
        _isEditing.value = false
    }

    fun updateEditField(
        nombre: String? = null,
        apellido: String? = null,
        telefono: String? = null,
        licencia: String? = null,
        vehiculo: String? = null
    ) {
        nombre?.let { _editNombre.value = it }
        apellido?.let { _editApellido.value = it }
        telefono?.let { _editTelefono.value = it }
        licencia?.let { _editLicencia.value = it }
        vehiculo?.let { _editVehiculo.value = it }
    }

    fun onProfileImageSelected(context: Context, file: File) {
        viewModelScope.launch {
            try {
                val currentUser = authRemoteRepository.currentUser.value
                if (currentUser == null) {
                    Log.e("TransportistaPerfilVM", "No hay usuario autenticado")
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
                    Log.e("TransportistaPerfilVM", "Error al comprimir imagen")
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
                    Log.e("TransportistaPerfilVM", "Error al subir foto: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("TransportistaPerfilVM", "Error al procesar imagen: ${e.message}")
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
                        _uiState.update { it.copy(profileImageUri = tempFile.absolutePath) }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransportistaPerfilVM", "❌ Excepción al cargar foto: ${e.message}", e)
            }
        }
    }

    fun guardarCambios(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Validaciones
                validateProfileName(_editNombre.value)?.let { callback(false, it); return@launch }
                validateProfileName(_editApellido.value)?.let { callback(false, it); return@launch }
                validateProfilePhone(_editTelefono.value)?.let { callback(false, it); return@launch }

                if (_editVehiculo.value.isBlank()) { callback(false, "El tipo de vehículo es obligatorio"); return@launch }
                if (_editLicencia.value.isBlank()) { callback(false, "La licencia es obligatoria"); return@launch }

                val currentUser = authRemoteRepository.currentUser.value ?: return@launch

                // 2. Actualizar Persona
                val personaResult = personaRemoteRepository.obtenerPersonaPorId(currentUser.idPersona)
                val personaActual = personaResult.getOrNull()

                if (personaActual != null) {
                    val personaUpdate = personaActual.copy(
                        nombre = _editNombre.value,
                        apellido = _editApellido.value,
                        telefono = _editTelefono.value
                    )
                    val updateRes = personaRemoteRepository.actualizarPersona(currentUser.idPersona, personaUpdate)
                    if (updateRes.isFailure) {
                        callback(false, "Error al actualizar datos personales")
                        return@launch
                    }
                    // actualizar usuario en memoria
                    val updatedUsuario = UsuarioCompleto(
                        idPersona = currentUser.idPersona,
                        nombre = personaUpdate.nombre ?: "",
                        apellido = personaUpdate.apellido ?: "",
                        rut = personaUpdate.rut ?: "",
                        telefono = personaUpdate.telefono ?: "",
                        email = personaUpdate.email ?: "",
                        idComuna = personaUpdate.idComuna,
                        calle = personaUpdate.calle ?: currentUser.calle,
                        numeroPuerta = personaUpdate.numeroPuerta ?: currentUser.numeroPuerta,
                        username = currentUser.username,
                        fechaRegistro = currentUser.fechaRegistro,
                        estado = personaUpdate.estado ?: currentUser.estado,
                        idRol = currentUser.idRol,
                        nombreRol = currentUser.nombreRol,
                        activo = currentUser.activo
                    )
                    authRemoteRepository.setCurrentUser(updatedUsuario)
                }

                // 3. Actualizar Transportista (Microservicio Transportista)
                if (currentTransportistaDto != null) {
                    // Actualizar existente
                    val transUpdate = currentTransportistaDto!!.copy(
                        licencia = _editLicencia.value,
                        tipoVehiculo = _editVehiculo.value
                    )
                    val transRes = transportistaRemoteRepository.actualizar(transUpdate.idTransportista!!, transUpdate)
                    if (transRes.isFailure) {
                        callback(false, "Error al actualizar datos del vehículo")
                        return@launch
                    }
                } else {
                    // Crear nuevo si no existe
                    val nuevo = TransportistaDTO(
                        idTransportista = null,
                        idPersona = currentUser.idPersona,
                        licencia = _editLicencia.value,
                        tipoVehiculo = _editVehiculo.value,
                        activo = true
                    )
                    transportistaRemoteRepository.crear(nuevo)
                }

                // 4. Éxito
                cargarPerfil()
                _isEditing.value = false
                callback(true, null)

            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }
    }
}
