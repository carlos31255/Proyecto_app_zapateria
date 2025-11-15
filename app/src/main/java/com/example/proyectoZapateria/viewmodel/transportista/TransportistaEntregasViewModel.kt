package com.example.proyectoZapateria.viewmodel.transportista

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
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
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaEntregasUiState())
    val uiState: StateFlow<TransportistaEntregasUiState> = _uiState.asStateFlow()

    private val transportistaId: Int? = savedStateHandle.get<Int>("transportistaId")

    init {
        Log.d("TransportistaEntregasVM", "ViewModel inicializado")
        cargarEntregas()
    }

    private fun cargarEntregas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val idTransportista = transportistaId ?: authRepository.currentUser.value?.idPersona

                if (idTransportista == null) {
                    Log.e("TransportistaEntregasVM", "No se pudo obtener el ID del transportista")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo obtener el ID del transportista"
                    )
                    return@launch
                }

                Log.d("TransportistaEntregasVM", "Cargando entregas para transportista ID: $idTransportista")

                val result = entregasRepository.obtenerEntregasPorTransportista(idTransportista)

                if (result.isSuccess) {
                    val entregas = result.getOrNull() ?: emptyList()

                    val pendientes = entregas.count { it.estadoEntrega.lowercase() == "pendiente" }
                    val entregadas = entregas.count { it.estadoEntrega.lowercase() == "entregada" }
                    val completadas = entregas.count { it.estadoEntrega.lowercase() == "completada" }

                    Log.d("TransportistaEntregasVM", "Entregas cargadas: ${entregas.size} (Pendientes: $pendientes, Entregadas: $entregadas, Completadas: $completadas)")

                    _uiState.value = TransportistaEntregasUiState(
                        entregas = entregas,
                        pendientesCount = pendientes,
                        completadasCount = completadas + entregadas,
                        isLoading = false
                    )
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e("TransportistaEntregasVM", "Error al cargar entregas: $errorMsg")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                Log.e("TransportistaEntregasVM", "Error al cargar entregas: ${e.message}", e)
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

