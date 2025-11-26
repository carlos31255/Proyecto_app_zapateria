package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDetalleUi
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DetalleBoletaRemoteRepository @Inject constructor(
    private val ventasRemoteRepository: VentasRemoteRepository,
    private val inventarioRemoteRepository: InventarioRemoteRepository
) {
    // Cache para marcas y productos
    private val marcaCache = mutableMapOf<Long, String>()
    private val productoCache = mutableMapOf<Long, ProductoDTO?>()
    private val inventarioCache = mutableMapOf<Long, com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO?>()

    fun getProductos(idBoleta: Long): Flow<List<ProductoDetalleUi>> = flow {
        val productosUi = try {
            val res = ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta)
            if (res.isSuccess) {
                val detalles: List<DetalleBoletaDTO> = res.getOrNull() ?: emptyList()
                detalles.map { d ->
                    // Normalizar talla
                    var tallaVal = d.talla?.trim()
                    if (tallaVal.isNullOrBlank() || tallaVal.equals("null", ignoreCase = true)) {
                        tallaVal = "-"
                        try {
                            if (d.inventarioId > 0L) {
                                val inv = inventarioCache.getOrPut(d.inventarioId) {
                                    inventarioRemoteRepository.getInventarioById(d.inventarioId).getOrNull()
                                }
                                if (inv != null && !inv.talla.isNullOrBlank()) {
                                    tallaVal = inv.talla.trim()
                                }
                            }
                        } catch (e: Exception) {
                            // Ignorar error
                        }
                    }

                    // Resolver producto y marca
                    var marcaName = "Desconocida"
                    var productoDto: ProductoDTO? = null
                    try {
                        val invId = d.inventarioId
                        if (invId > 0L) {
                            val inv = inventarioCache.getOrPut(invId) {
                                inventarioRemoteRepository.getInventarioById(invId).getOrNull()
                            }
                            val productoId = inv?.productoId
                            if (productoId != null) {
                                productoDto = productoCache.getOrPut(productoId) {
                                    inventarioRemoteRepository.getProductoById(productoId).getOrNull()
                                }
                                val marcaId = productoDto?.marcaId
                                if (marcaId != null) {
                                    marcaName = marcaCache.getOrPut(marcaId) {
                                        inventarioRemoteRepository.getMarcaById(marcaId).getOrNull()?.nombre ?: "Desconocida"
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar error
                    }

                    ProductoDetalleUi(
                        producto = productoDto ?: ProductoDTO(id = null, nombre = d.nombreProducto ?: "-", marcaId = -1L, descripcion = null, precioUnitario = 0, imagenUrl = null),
                        cantidad = d.cantidad,
                        talla = tallaVal ?: "-",
                        marcaName = if (marcaName.isBlank()) "Desconocida" else marcaName
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        emit(productosUi)
    }

    fun getProductosPorNumeroBoleta(numeroBoleta: String): Flow<List<ProductoDetalleUi>> = flow {
        emit(emptyList())
    }
}
