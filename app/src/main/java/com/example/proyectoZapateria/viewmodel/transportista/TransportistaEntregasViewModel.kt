package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.EntregaRepository
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransportistaEntregasViewModel @Inject constructor(
    // Repositorio de entregas para acceder a los datos de entregas
    // Hilt inyecta automáticamente la instancia necesaria
    private val entregaRepository: EntregaRepository,
    authViewModel: AuthViewModel
): ViewModel(){
    // Obtenemos el ID del transportista logueado
    private val transportistaId = authViewModel.currentUser.value!!.idPersona

    // Este es el único StateFlow que la UI observará.
    val uiState: StateFlow<EntregasUiState> =
        // Llamamos al repositorio para obtener el Flow de datos
        entregaRepository.getEntregasPorTransportista(transportistaId)
            // Transformamos la lista de "EntregaConDetalles" en el "EntregasUiState" completo
            .map { listaDeEntregas ->
                // Calculamos los conteos aquí
                val pendientes = listaDeEntregas.count { it.estadoEntrega == "pendiente" }
                val completadas = listaDeEntregas.count { it.estadoEntrega == "completada" }

                // Creamos el estado exitoso
                EntregasUiState(
                    entregas = listaDeEntregas,
                    pendientesCount = pendientes,
                    completadasCount = completadas,
                    isLoading = false // Datos cargados
                )
            }
            // Manejamos cualquier error que ocurra en el Flow
            .catch { e ->
                emit(EntregasUiState(isLoading = false, error = e.message ?: "Error desconocido"))
            }
            // Convertimos el Flow en un StateFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L), // Mantener 5 segundos después del último suscriptor
                initialValue = EntregasUiState(isLoading = true) // El estado inicial mientras carga
            )
}