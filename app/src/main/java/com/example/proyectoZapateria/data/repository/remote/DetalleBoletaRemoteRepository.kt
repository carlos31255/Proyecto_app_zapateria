package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DetalleBoletaRemoteRepository @Inject constructor(
    private val ventasRemoteRepository: VentasRemoteRepository
) {
    // Obtener detalles de una boleta desde el servicio remoto y mapear a ProductoDetalle
    fun getProductos(idBoleta: Long): Flow<List<ProductoDetalle>> = flow {
        try {
            val res = ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta)
            if (res.isSuccess) {
                val detalles: List<DetalleBoletaDTO> = res.getOrNull() ?: emptyList()
                val productos = detalles.map { d ->
                    ProductoDetalle(
                        nombreZapato = d.nombreProducto ?: "",
                        talla = d.talla ?: "",
                        cantidad = d.cantidad,
                        marca = "" // Marca no provista en DetalleBoletaDTO
                    )
                }
                emit(productos)
            } else {
                Log.w("DetalleBoletaRepo", "Error al obtener detalles remotos: ${res.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("DetalleBoletaRepo", "Exception al obtener detalles remotos: ${e.message}")
            emit(emptyList())
        }
    }

    // Obtener por número de boleta: no existe endpoint directo, devolver vacío y loguear
    fun getProductosPorNumeroBoleta(numeroBoleta: String): Flow<List<ProductoDetalle>> = flow {
        Log.w("DetalleBoletaRepo", "getProductosPorNumeroBoleta: no implementado en backend (numero=$numeroBoleta)")
        emit(emptyList())
    }
}
