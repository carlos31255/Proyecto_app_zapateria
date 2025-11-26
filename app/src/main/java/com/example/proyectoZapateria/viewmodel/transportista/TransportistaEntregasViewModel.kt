package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.TransportistaRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransportistaEntregasUiState(
    val entregas: List<EntregaDTO> = emptyList(),
    val pendientesCount: Int = 0,
    val completadasCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TransportistaEntregasViewModel @Inject constructor(
    private val entregasRepository: EntregasRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val transportistaRemoteRepository: TransportistaRemoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaEntregasUiState())
    val uiState: StateFlow<TransportistaEntregasUiState> = _uiState.asStateFlow()

    private val transportistaId: Long? = run {
        val raw = savedStateHandle.get<Any?>("transportistaId")
        when (raw) {
            is Long -> raw
            is Int -> raw.toLong()
            is Number -> raw.toLong()
            else -> null
        }
    }

    init {
        // ViewModel inicializado
        cargarEntregas()

        // Suscribirse a actualizaciones globales de entregas para recargar automáticamente
        viewModelScope.launch {
            entregasRepository.updatesFlow.collect {
                // Detectada actualización de entregas -> recargando
                try {
                    cargarEntregas()
                } catch (e: Exception) {
                    // Error recargando entregas tras update: ${e.message}
                }
            }
        }
    }

    private fun cargarEntregas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val personaId = authRemoteRepository.currentUser.value?.idPersona
                val idTransportistaRemoto: Long? = try {
                    if (personaId != null) {
                        val resp = transportistaRemoteRepository.obtenerPorPersona(personaId)
                        resp.getOrNull()?.idTransportista
                    } else null
                } catch (ex: Exception) {
                    // Error consultando transportista remoto: ${ex.message}
                    null
                }

                val idTransportista = transportistaId ?: idTransportistaRemoto ?: personaId

                if (idTransportista == null) {
                    // No se pudo obtener el ID del transportista
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo obtener el ID del transportista"
                    )
                    return@launch
                }

                // Cargando entregas para transportista ID: $idTransportista

                val result = entregasRepository.obtenerEntregasPorTransportista(idTransportista)

                if (result.isSuccess) {
                    val entregas = result.getOrNull() ?: emptyList()

                    val pendientes = entregas.count { it.estadoEntrega.lowercase() == "pendiente" }
                    val entregadas = entregas.count { it.estadoEntrega.lowercase() == "entregada" }
                    val completadas = entregas.count { it.estadoEntrega.lowercase() == "completada" }

                    // Entregas cargadas: ${entregas.size} (Pendientes: $pendientes, Entregadas: $entregadas, Completadas: $completadas)

                    _uiState.value = TransportistaEntregasUiState(
                        entregas = entregas,
                        pendientesCount = pendientes,
                        completadasCount = completadas + entregadas,
                        isLoading = false
                    )
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    // Error al cargar entregas: $errorMsg
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                // Error al cargar entregas: ${e.message}
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun recargarEntregas() {
        cargarEntregas()
    }
}
