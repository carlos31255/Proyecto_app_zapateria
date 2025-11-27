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
        android.util.Log.d("TransportistaEntregasVM", "=== ViewModel inicializado ===")
        android.util.Log.d("TransportistaEntregasVM", "transportistaId desde SavedState: $transportistaId")
        cargarEntregas()

        // Suscribirse a actualizaciones globales de entregas para recargar automáticamente
        viewModelScope.launch {
            entregasRepository.updatesFlow.collect {
                android.util.Log.d("TransportistaEntregasVM", "⚡ Detectada actualización de entregas -> recargando")
                try {
                    cargarEntregas()
                } catch (e: Exception) {
                    android.util.Log.e("TransportistaEntregasVM", "Error recargando entregas tras update: ${e.message}", e)
                }
            }
        }
    }

    private fun cargarEntregas() {
        viewModelScope.launch {
            android.util.Log.d("TransportistaEntregasVM", ">>> INICIO cargarEntregas()")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val personaId = authRemoteRepository.currentUser.value?.idPersona
                android.util.Log.d("TransportistaEntregasVM", "personaId del usuario actual: $personaId")

                val idTransportistaRemoto: Long? = try {
                    if (personaId != null) {
                        android.util.Log.d("TransportistaEntregasVM", "Consultando transportista por personaId=$personaId")
                        val resp = transportistaRemoteRepository.obtenerPorPersona(personaId)
                        val id = resp.getOrNull()?.idTransportista
                        android.util.Log.d("TransportistaEntregasVM", "ID transportista remoto obtenido: $id")
                        id
                    } else {
                        android.util.Log.w("TransportistaEntregasVM", "personaId es null, no se puede consultar transportista")
                        null
                    }
                } catch (ex: Exception) {
                    android.util.Log.e("TransportistaEntregasVM", "Error consultando transportista remoto: ${ex.message}", ex)
                    null
                }

                val idTransportista = transportistaId ?: idTransportistaRemoto ?: personaId
                android.util.Log.d("TransportistaEntregasVM", "ID transportista final a usar: $idTransportista")

                if (idTransportista == null) {
                    android.util.Log.e("TransportistaEntregasVM", "❌ No se pudo obtener el ID del transportista")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo obtener el ID del transportista"
                    )
                    return@launch
                }

                // Obtener entregas asignadas a este transportista
                android.util.Log.d("TransportistaEntregasVM", "📦 Llamando a repository.obtenerEntregasPorTransportista($idTransportista)")
                val resultAsignadas = entregasRepository.obtenerEntregasPorTransportista(idTransportista)

                // Obtener TODAS las entregas para encontrar las pendientes sin asignar
                android.util.Log.d("TransportistaEntregasVM", "📦 Llamando a repository.obtenerTodasLasEntregas()")
                val resultTodas = entregasRepository.obtenerTodasLasEntregas()

                if (resultAsignadas.isSuccess) {
                    val entregasAsignadas = resultAsignadas.getOrNull() ?: emptyList()

                    // Buscar entregas pendientes sin asignar de todas las entregas
                    val entregasPendientesSinAsignar = if (resultTodas.isSuccess) {
                        val todasEntregas = resultTodas.getOrNull() ?: emptyList()
                        android.util.Log.d("TransportistaEntregasVM", "📦 Total entregas en sistema: ${todasEntregas.size}")

                        // Filtrar: estado pendiente Y sin transportista asignado
                        todasEntregas.filter {
                            val estadoLower = it.estadoEntrega.lowercase()
                            val esPendiente = estadoLower == "pendiente" || estadoLower == "pending"
                            val sinAsignar = it.idTransportista == null

                            if (esPendiente && sinAsignar) {
                                android.util.Log.d("TransportistaEntregasVM", "  ✅ Entrega disponible: ID=${it.idEntrega}, Estado=${it.estadoEntrega}, Transportista=${it.idTransportista}")
                            }

                            esPendiente && sinAsignar
                        }
                    } else {
                        android.util.Log.w("TransportistaEntregasVM", "No se pudieron obtener todas las entregas")
                        emptyList()
                    }

                    android.util.Log.d("TransportistaEntregasVM", "✅ Entregas asignadas: ${entregasAsignadas.size}")
                    android.util.Log.d("TransportistaEntregasVM", "✅ Entregas pendientes sin asignar: ${entregasPendientesSinAsignar.size}")

                    // Combinar ambas listas: primero las pendientes sin asignar (para tomar), luego las asignadas
                    val todasLasEntregas = entregasPendientesSinAsignar + entregasAsignadas

                    android.util.Log.d("TransportistaEntregasVM", "📋 Total entregas a mostrar: ${todasLasEntregas.size}")

                    todasLasEntregas.forEachIndexed { index, entrega ->
                        android.util.Log.d("TransportistaEntregasVM", "  [$index] ID=${entrega.idEntrega}, Estado=${entrega.estadoEntrega}, TransportistaID=${entrega.idTransportista}, BoletaID=${entrega.idBoleta}")
                    }

                    val pendientes = todasLasEntregas.count { it.estadoEntrega.lowercase() == "pendiente" }
                    val entregadas = todasLasEntregas.count { it.estadoEntrega.lowercase() == "entregada" }
                    val completadas = todasLasEntregas.count { it.estadoEntrega.lowercase() == "completada" }

                    android.util.Log.d("TransportistaEntregasVM", "📊 Estadísticas: Pendientes=$pendientes, Entregadas=$entregadas, Completadas=$completadas")

                    _uiState.value = TransportistaEntregasUiState(
                        entregas = todasLasEntregas,
                        pendientesCount = pendientes,
                        completadasCount = completadas + entregadas,
                        isLoading = false
                    )
                    android.util.Log.d("TransportistaEntregasVM", "<<< FIN cargarEntregas() - ÉXITO")
                } else {
                    val errorMsg = resultAsignadas.exceptionOrNull()?.message ?: "Error desconocido"
                    android.util.Log.e("TransportistaEntregasVM", "❌ Error al cargar entregas: $errorMsg", resultAsignadas.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    android.util.Log.d("TransportistaEntregasVM", "<<< FIN cargarEntregas() - ERROR")
                }
            } catch (e: Exception) {
                android.util.Log.e("TransportistaEntregasVM", "💥 Excepción al cargar entregas: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                android.util.Log.d("TransportistaEntregasVM", "<<< FIN cargarEntregas() - EXCEPCIÓN")
            }
        }
    }

    fun recargarEntregas() {
        cargarEntregas()
    }
}
