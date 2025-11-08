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

    // Obtiene una entrega simple por su ID
    suspend fun getEntregaSimple(idEntrega: Int): EntregaEntity? {
        return entregaDao.getEntregaById(idEntrega)
    }
    // Cuenta las entregas de un transportista por estado
    suspend fun getCountEntregasPorEstado(transportistaId: Int, estado:
    String): Int {
            return entregaDao.getCountByEstadoParaTransportista(transportistaId, estado)
        }
    // Obtiene los detalles completos de una entrega por su ID
    fun getDetallesPorId(idEntrega: Int): Flow<EntregaConDetalles> {
        return entregaDao.getDetallesPorId(idEntrega)
    }

    // Actualiza una entrega existente en la base de datos
    suspend fun updateEntrega(entrega: EntregaEntity) {
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