package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DetalleBoletaRepository @Inject constructor(
    private val detalleBoletaDao: DetalleBoletaDao
) {
    // Pasa la llamada al DAO
    fun getProductos(idBoleta: Int): Flow<List<ProductoDetalle>> =
        detalleBoletaDao.getProductosDeBoleta(idBoleta)
}