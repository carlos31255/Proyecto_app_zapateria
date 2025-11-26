package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
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

    fun getProductos(idBoleta: Long): Flow<List<ProductoDetalle>> = flow {
        val productos = try {
            val res = ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta)
            if (res.isSuccess) {
                val detalles: List<DetalleBoletaDTO> = res.getOrNull() ?: emptyList()
                detalles.map { d ->
                    // Talla: preferir la del detalle, si no, buscar en inventario
                    var tallaVal = d.talla?.trim()
                    if (tallaVal.isNullOrBlank() || tallaVal.equals("null", ignoreCase = true)) {
                        tallaVal = ""
                        try {
                            if (d.inventarioId > 0L) {
                                val inv = inventarioCache.getOrPut(d.inventarioId) {
                                    inventarioRemoteRepository.getInventarioById(d.inventarioId).getOrNull()
                                }
                                if (inv != null && inv.talla.isNotBlank()) {
                                    tallaVal = inv.talla.trim()
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("DetalleBoletaRepo", "Error obteniendo inventario ${d.inventarioId} para talla: ${e.message}")
                        }
                    }

                    // Marca: buscar en producto
                    var marcaName = ""
                    try {
                        val invId = d.inventarioId
                        if (invId > 0L) {
                            val inv = inventarioCache.getOrPut(invId) {
                                inventarioRemoteRepository.getInventarioById(invId).getOrNull()
                            }
                            val productoId = inv?.productoId
                            if (productoId != null) {
                                val producto = productoCache.getOrPut(productoId) {
                                    inventarioRemoteRepository.getProductoById(productoId).getOrNull()
                                }
                                val marcaId = producto?.marcaId
                                if (marcaId != null) {
                                    marcaName = marcaCache.getOrPut(marcaId) {
                                        inventarioRemoteRepository.getMarcaById(marcaId).getOrNull()?.nombre ?: ""
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("DetalleBoletaRepo", "Error resolviendo marca para inventario ${d.inventarioId}: ${e.message}")
                    }

                    if (marcaName.isBlank()) marcaName = "Desconocida"
                    val finalTalla = if (tallaVal.isBlank()) "-" else tallaVal

                    ProductoDetalle(
                        nombreZapato = d.nombreProducto ?: "",
                        talla = finalTalla,
                        cantidad = d.cantidad,
                        marca = marcaName
                    )
                }
            } else {
                Log.w("DetalleBoletaRepo", "Error al obtener detalles remotos: ${res.exceptionOrNull()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DetalleBoletaRepo", "Exception al obtener detalles remotos: ${e.message}")
            emptyList()
        }
        emit(productos)
    }

    fun getProductosPorNumeroBoleta(numeroBoleta: String): Flow<List<ProductoDetalle>> = flow {
        Log.w("DetalleBoletaRepo", "getProductosPorNumeroBoleta: no implementado en backend (numero=$numeroBoleta)")
        emit(emptyList())
    }
}
