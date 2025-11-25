package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DetalleBoletaRemoteRepository @Inject constructor(
    private val ventasRemoteRepository: VentasRemoteRepository,
    private val inventarioRemoteRepository: InventarioRemoteRepository
) {
    // Obtener detalles de una boleta desde el servicio remoto y mapear a ProductoDetalle
    fun getProductos(idBoleta: Long): Flow<List<ProductoDetalle>> = flow {
        val productos = try {
            val res = ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta)
            if (res.isSuccess) {
                val detalles: List<DetalleBoletaDTO> = res.getOrNull() ?: emptyList()
                detalles.map { d ->
                    // Determinar talla: preferir la que venga en el detalle, si no, intentar obtener desde inventario
                    var tallaVal = d.talla?.trim()
                    if (tallaVal.isNullOrBlank() || tallaVal.equals("null", ignoreCase = true)) {
                        tallaVal = ""
                        try {
                            if (d.inventarioId > 0L) {
                                val invRes = inventarioRemoteRepository.getInventarioById(d.inventarioId)
                                val inv = invRes.getOrNull()
                                if (inv != null && !inv.talla.isNullOrBlank()) {
                                    tallaVal = inv.talla.trim()
                                    Log.d("DetalleBoletaRepo", "Resolved talla from inventario ${d.inventarioId} => $tallaVal for boletaId=$idBoleta")
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("DetalleBoletaRepo", "Error obteniendo inventario ${d.inventarioId} para talla: ${e.message}")
                        }
                    }

                    // Intentar resolver marca consultando inventario/modelo si no está presente
                    var marcaName = ""
                    try {
                        // Si tenemos inventarioId, obtener inventario -> productoId -> producto -> marcaId -> marca
                        val invId = d.inventarioId
                        if (invId > 0L) {
                            val invRes = inventarioRemoteRepository.getInventarioById(invId)
                            val inv = invRes.getOrNull()
                            val modeloKey = inv?.modeloId ?: inv?.productoId
                            if (modeloKey != null) {
                                val modeloRes = inventarioRemoteRepository.getModeloById(modeloKey)
                                val modelo = modeloRes.getOrNull()
                                val marcaId = modelo?.marcaId
                                if (marcaId != null) {
                                    val marcaRes = inventarioRemoteRepository.getMarcaById(marcaId)
                                    val marcaDto = marcaRes.getOrNull()
                                    marcaName = marcaDto?.nombre ?: ""
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("DetalleBoletaRepo", "Error resolviendo marca para inventario ${d.inventarioId}: ${e.message}")
                    }

                    if (marcaName.isBlank()) marcaName = "Desconocida"
                    if (tallaVal.isNullOrBlank()) tallaVal = "-"

                    Log.d("DetalleBoletaRepo", "Mapped detalle: inventarioId=${d.inventarioId} nombre='${d.nombreProducto}' talla='${tallaVal}' marca='${marcaName}' cantidad=${d.cantidad}")

                    ProductoDetalle(
                        nombreZapato = d.nombreProducto ?: "",
                        talla = tallaVal,
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

    // Obtener por número de boleta: no existe endpoint directo, devolver vacío y loguear
    fun getProductosPorNumeroBoleta(numeroBoleta: String): Flow<List<ProductoDetalle>> = flow {
        Log.w("DetalleBoletaRepo", "getProductosPorNumeroBoleta: no implementado en backend (numero=$numeroBoleta)")
        emit(emptyList())
    }
}
