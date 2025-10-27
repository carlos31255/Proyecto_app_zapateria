package com.example.proyectoZapateria.viewmodel.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientePedidosViewModel @Inject constructor(
    private val boletaVentaDao: BoletaVentaDao,
    private val detalleBoletaRepository: DetalleBoletaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val pedidos: List<BoletaVentaEntity> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = authRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }

                val idPersona = current.idPersona
                val pedidos = boletaVentaDao.getByCliente(idPersona)
                // colectar una emisión rápida
                val lista = pedidos.first()
                _uiState.value = _uiState.value.copy(isLoading = false, pedidos = lista)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    // Devuelve un Flow con los productos (nombre, talla, cantidad, marca) para una boleta
    fun getProductosForBoleta(idBoleta: Int): Flow<List<ProductoDetalle>> {
        return detalleBoletaRepository.getProductos(idBoleta)
    }
}