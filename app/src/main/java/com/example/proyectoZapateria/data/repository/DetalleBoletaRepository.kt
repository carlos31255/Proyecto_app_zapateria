package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DetalleBoletaRepository @Inject constructor(
    private val detalleBoletaDao: DetalleBoletaDao
) {
    // Pasa la llamada al DAO por id de boleta (int)
    fun getProductos(idBoleta: Int): Flow<List<ProductoDetalle>> =
        detalleBoletaDao.getProductosDeBoleta(idBoleta)

    // Nueva sobrecarga: obtener productos por numero de boleta (String)
    fun getProductosPorNumeroBoleta(numeroBoleta: String): Flow<List<ProductoDetalle>> =
        detalleBoletaDao.getProductosDeBoletaPorNumero(numeroBoleta)

    // Nuevo: insertar detalle (retorna id)
    suspend fun insertDetalle(detalle: DetalleBoletaEntity): Long = detalleBoletaDao.insert(detalle)
}