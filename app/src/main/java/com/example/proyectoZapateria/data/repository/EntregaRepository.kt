package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EntregaRepository @Inject constructor (
    private val entregaDao: EntregaDao
){
    // Insertar una nueva entrega
    suspend fun insertEntrega(entrega: EntregaEntity): Long {
        return entregaDao.insert(entrega)
    }

    // Flow que obtiene todas las entregas con detalles
    fun getEntregasPorTransportista(transportistaId: Int): Flow<List<EntregaConDetalles>> {
        return entregaDao.getEntregasConDetalles(transportistaId)
    }

    // Obtiene los detalles completos de una entrega por su ID
    fun getDetallesPorId(idEntrega: Int): Flow<EntregaConDetalles> {
        return entregaDao.getDetallesPorId(idEntrega)
    }

    // Obtiene una entrega por ID de boleta
    suspend fun getEntregaPorBoleta(idBoleta: Int): EntregaEntity? {
        return entregaDao.getByBoleta(idBoleta)
    }

    // Obtiene una entrega por su ID
    suspend fun getEntregaById(idEntrega: Int): EntregaEntity? {
        return entregaDao.getEntregaById(idEntrega)
    }

    // Actualiza una entrega
    suspend fun actualizarEntrega(entrega: EntregaEntity) {
        entregaDao.updateEntrega(entrega)
    }

    // Confirma la entrega, actualizando su estado, fecha y observación
    suspend fun confirmarEntrega(idEntrega: Int, observacion: String?): Boolean {
        return try {
            val fechaEntrega = System.currentTimeMillis()
            entregaDao.confirmarEntrega(idEntrega, fechaEntrega, observacion)
            true
        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }
}