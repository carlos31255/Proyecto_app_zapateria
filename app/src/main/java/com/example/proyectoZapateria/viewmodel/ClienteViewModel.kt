package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.repository.ClienteRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClienteConPedidos(
    val cliente: ClienteDTO,
    val pedidos: List<BoletaDTO>
)

@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val clienteRemoteRepository: ClienteRemoteRepository,
    private val ventasRepository: VentasRemoteRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _clientes = MutableStateFlow<List<ClienteDTO>>(emptyList())
    val clientes: StateFlow<List<ClienteDTO>> = _clientes.asStateFlow()

    private val _clienteSeleccionado = MutableStateFlow<ClienteConPedidos?>(null)
    val clienteSeleccionado: StateFlow<ClienteConPedidos?> = _clienteSeleccionado.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _clientesFiltrados = MutableStateFlow<List<ClienteDTO>>(emptyList())
    val clientesFiltrados: StateFlow<List<ClienteDTO>> = _clientesFiltrados.asStateFlow()

    init {
        cargarClientes()
    }

    fun cargarClientes() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val result = clienteRemoteRepository.obtenerTodosLosClientes()
                if (result.isSuccess) {
                    val listaClientes = result.getOrNull() ?: emptyList()
                    _clientes.value = listaClientes
                    _clientesFiltrados.value = filtrarClientes(listaClientes, _searchQuery.value)
                } else {
                    _errorMessage.value = "Error al cargar clientes: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        _clientesFiltrados.value = filtrarClientes(_clientes.value, query)
    }

    private fun filtrarClientes(clientes: List<ClienteDTO>, query: String): List<ClienteDTO> {
        if (query.isBlank()) return clientes

        return clientes.filter { cliente ->
            cliente.nombreCompleto?.contains(query, ignoreCase = true) == true ||
            cliente.email?.contains(query, ignoreCase = true) == true ||
            cliente.telefono?.contains(query, ignoreCase = true) == true
        }
    }

    fun cargarDetalleCliente(idCliente: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val clienteResult = clienteRemoteRepository.obtenerClientePorId(idCliente)

                if (clienteResult.isSuccess) {
                    val cliente = clienteResult.getOrNull()
                    if (cliente != null) {
                        val pedidosResult = ventasRepository.obtenerBoletasPorCliente(idCliente)

                        val pedidos = if (pedidosResult.isSuccess) {
                            pedidosResult.getOrNull() ?: emptyList()
                        } else {
                            emptyList()
                        }

                        _clienteSeleccionado.value = ClienteConPedidos(
                            cliente = cliente,
                            pedidos = pedidos
                        )
                        _isLoading.value = false
                    } else {
                        _errorMessage.value = "Cliente no encontrado"
                        _isLoading.value = false
                    }
                } else {
                    _errorMessage.value = "Error al cargar cliente: ${clienteResult.exceptionOrNull()?.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar detalles del cliente: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun limpiarClienteSeleccionado() {
        _clienteSeleccionado.value = null
    }

    fun limpiarError() {
        _errorMessage.value = null
    }
}

