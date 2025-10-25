package com.example.proyectoZapateria.viewmodel.transportista

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.EntregaRepository
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
    private val entregaRepository: EntregaRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    // Obtenemos el ID del transportista desde los argumentos (será establecido por la pantalla)
    private val transportistaId: Int = savedStateHandle.get<Int>("transportistaId") ?: -1

    init {
        Log.d("TransportistaVM", "ViewModel inicializado")
        Log.d("TransportistaVM", "Transportista ID: $transportistaId")
    }

    // Este es el único StateFlow que la UI observará.
    val uiState: StateFlow<TransportistaEntregasUiState> =
        if (transportistaId == -1) {
            // Si no hay ID, emitimos un estado de error
            kotlinx.coroutines.flow.flowOf(
                TransportistaEntregasUiState(
                    isLoading = false,
                    error = "No se pudo obtener el ID del transportista"
                )
            ).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = TransportistaEntregasUiState(isLoading = true)
            )
        } else {
            // Llamamos al repositorio para obtener el Flow de datos
            entregaRepository.getEntregasPorTransportista(transportistaId)
                // Transformamos la lista de "EntregaConDetalles" en el "EntregasUiState" completo
                .map { listaDeEntregas ->
                    // Calculamos los conteos aquí
                    val pendientes = listaDeEntregas.count { it.estadoEntrega == "pendiente" }
                    val completadas = listaDeEntregas.count { it.estadoEntrega == "completada" }

                    Log.d("TransportistaVM", "Entregas cargadas: ${listaDeEntregas.size} (Pendientes: $pendientes, Completadas: $completadas)")

                    // Creamos el estado exitoso
                    TransportistaEntregasUiState(
                        entregas = listaDeEntregas,
                        pendientesCount = pendientes,
                        completadasCount = completadas,
                        isLoading = false // Datos cargados
                    )
                }
                // Manejamos cualquier error que ocurra en el Flow
                .catch { e ->
                    Log.e("TransportistaVM", "Error al cargar entregas: ${e.message}", e)
                    emit(TransportistaEntregasUiState(isLoading = false, error = e.message ?: "Error desconocido"))
                }
                // Convertimos el Flow en un StateFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = TransportistaEntregasUiState(isLoading = true)
                )
        }
}