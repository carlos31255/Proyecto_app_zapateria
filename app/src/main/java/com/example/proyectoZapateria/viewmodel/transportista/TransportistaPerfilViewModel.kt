package com.example.proyectoZapateria.viewmodel.transportista

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import com.example.proyectoZapateria.data.repository.TransportistaRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransportistaPerfilUiState(
    val nombre: String = "",
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
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TransportistaPerfilViewModel @Inject constructor(
    private val transportistaRepository: TransportistaRepository,
    private val entregasRepository: EntregasRemoteRepository,
    private val ventasRepository: VentasRemoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaPerfilUiState())
    val uiState: StateFlow<TransportistaPerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = authRepository.currentUser.value
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay sesión activa"
                    )
                    return@launch
                }

                val transportistaId = currentUser.idPersona
                Log.d("PerfilVM", "Cargando perfil para ID: $transportistaId")

                // Obtener datos de transportista simples (licencia, vehiculo) desde el repositorio local
                val transportistaEntity = try {
                    transportistaRepository.getTransportistaSimple(transportistaId)
                } catch (ex: Exception) {
                    Log.w("PerfilVM", "No se pudo obtener transportista local para id=$transportistaId: ${ex.message}")
                    null
                }

                // Obtener estadísticas de entregas
                val entregasResult = entregasRepository.obtenerEntregasPorTransportista(transportistaId)

                if (entregasResult.isSuccess) {
                    val entregas = entregasResult.getOrNull() ?: emptyList()
                    val completadas = entregas.count { it.estadoEntrega.lowercase() == "completada" }
                    val entregadas = entregas.count { it.estadoEntrega.lowercase() == "entregada" }
                    val pendientes = entregas.count { it.estadoEntrega.lowercase() == "pendiente" }
                    val total = entregas.size

                    Log.d("PerfilVM", "Estadísticas: Total=$total, Completadas=$completadas, Entregadas=$entregadas, Pendientes=$pendientes")

                    // A partir de las entregas, calcular métricas de boletas asociadas (ventas)
                    val boletaIds = entregas.mapNotNull { it.idBoleta }.distinct()
                    var totalImporte = 0
                    var boletasContadas = 0

                    for (idBoleta in boletaIds) {
                        try {
                            val boletaResult = ventasRepository.obtenerBoletaPorId(idBoleta)
                            if (boletaResult.isSuccess) {
                                val boleta = boletaResult.getOrNull()
                                if (boleta != null) {
                                    totalImporte += boleta.total
                                    boletasContadas += 1
                                }
                            } else {
                                Log.w("PerfilVM", "No se pudo obtener boleta id=$idBoleta: ${boletaResult.exceptionOrNull()?.message}")
                            }
                        } catch (ex: Exception) {
                            Log.w("PerfilVM", "Excepción al obtener boleta id=$idBoleta: ${ex.message}")
                        }
                    }

                    // Construir UI state combinando persona (desde auth currentUser) y transportistaEntity
                    val nombreCompleto = listOfNotNull(currentUser.nombre.takeIf { it.isNotBlank() }, currentUser.apellido.takeIf { it.isNotBlank() }).joinToString(" ").ifBlank { currentUser.username }

                    _uiState.value = TransportistaPerfilUiState(
                        nombre = nombreCompleto,
                        email = currentUser.email,
                        telefono = currentUser.telefono,
                        licencia = transportistaEntity?.licencia ?: "No especificada",
                        vehiculo = transportistaEntity?.vehiculo ?: "No especificado",
                        totalEntregas = total,
                        entregasCompletadas = completadas + entregadas,
                        entregasPendientes = pendientes,
                        totalBoletas = boletasContadas,
                        totalVentasImporte = totalImporte,
                        isLoading = false
                    )
                } else {
                    Log.w("PerfilVM", "Error al obtener entregas para transportista id=$transportistaId: ${entregasResult.exceptionOrNull()?.message}")

                    // Mostrar perfil mínimo con datos de persona y transportista si existe
                    val nombreCompleto = listOfNotNull(currentUser.nombre.takeIf { it.isNotBlank() }, currentUser.apellido.takeIf { it.isNotBlank() }).joinToString(" ").ifBlank { currentUser.username }

                    _uiState.value = TransportistaPerfilUiState(
                        nombre = nombreCompleto,
                        email = currentUser.email,
                        telefono = currentUser.telefono,
                        licencia = transportistaEntity?.licencia ?: "No especificada",
                        vehiculo = transportistaEntity?.vehiculo ?: "No especificado",
                        totalEntregas = 0,
                        entregasCompletadas = 0,
                        entregasPendientes = 0,
                        totalBoletas = 0,
                        totalVentasImporte = 0,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Log.e("PerfilVM", "Error al cargar perfil: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }
}
