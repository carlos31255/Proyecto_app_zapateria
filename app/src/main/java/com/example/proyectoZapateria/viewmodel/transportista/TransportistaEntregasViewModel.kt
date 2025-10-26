package com.example.proyectoZapateria.viewmodel.transportista

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.EntregaRepository
import com.example.proyectoZapateria.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransportistaEntregasViewModel @Inject constructor(
    // Repositorio de entregas para acceder a los datos de entregas
    private val entregaRepository: EntregaRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    // Flow que obtiene el ID del transportista: primero el argumento si existe, sino el usuario autenticado
    private val transportistaIdFlow: Flow<Int> = savedStateHandle.get<Int>("transportistaId")?.let { id ->
        flowOf(id)
    } ?: authRepository.currentUser.map { it?.idPersona ?: -1 }

    init {
        Log.d("TransportistaVM", "ViewModel inicializado")
        // No logueamos aquí el id estático porque puede llegar después (desde AuthRepository)
    }

    // Este es el único StateFlow que la UI observará.
    // Filtramos valores -1 para esperar a que el id real esté disponible (p. ej. restauración de sesión)
    val uiState: StateFlow<TransportistaEntregasUiState> =
        transportistaIdFlow
            .filter { it != -1 }
            .flatMapLatest { transportistaId ->
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
            }
            // Convertimos el Flow en un StateFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = TransportistaEntregasUiState(isLoading = true)
            )
}