package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.transportista.TransportistaConPersona
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import kotlinx.coroutines.flow.Flow

class TransportistaRepository (
    private val transportistaDao: TransportistaDao
){
    // --- FUNCIONES PARA "MI PERFIL" ---

    // Obtiene el perfil completo (con datos de Persona) de UN transportista
    suspend fun getPerfilTransportista(id: Int): TransportistaConPersona? {
        return transportistaDao.getByIdConPersona(id)
    }

    // Obtiene solo la entidad 'Transportista' (licencia, vehiculo)
    suspend fun getTransportistaSimple(id: Int): TransportistaEntity? {
        return transportistaDao.getById(id)
    }

    // Actualiza los datos de un transportista.
    suspend fun updateTransportista(transportista: TransportistaEntity) {
        transportistaDao.update(transportista)
    }
    // --- FUNCIONES PARA ADMINISTRACIÓN ---

    // Obtiene un Flow con la lista de TODOS los transportistas (con datos de Persona)
    fun getAllTransportistasConPersona(): Flow<List<TransportistaConPersona>> {
        return transportistaDao.getAllConPersona()
    }

    // Busca transportistas por nombre, apellido o username
    fun searchTransportistas(query: String): Flow<List<TransportistaConPersona>> {
        return transportistaDao.searchTransportistas(query)
    }

    // Obtiene transportistas filtrados por estado (activo/inactivo)
    fun getTransportistasPorEstado(estado: String): Flow<List<TransportistaConPersona>> {
        return transportistaDao.getByEstadoConPersona(estado)
    }

    // Inserta un nuevo transportista
    suspend fun insertTransportista(transportista: TransportistaEntity): Long {
        return transportistaDao.insert(transportista)
    }

    // Elimina un transportista
    suspend fun deleteTransportista(transportista: TransportistaEntity) {
        transportistaDao.delete(transportista)
    }

    // Busca un transportista por su número de licencia.
    suspend fun getTransportistaPorLicencia(licencia: String): TransportistaEntity? {
        return transportistaDao.getByLicencia(licencia)
    }

    // Cuenta cuantos transportistas hay en total
    suspend fun getTransportistaCount(): Int {
        return transportistaDao.getCount()
    }

    // Elimina todos los transportistas de la tabla
    suspend fun deleteAllTransportistas() {
        transportistaDao.deleteAll()
    }

    // Obtiene un Flow con la lista de todas las entidades transportista simples
    fun getAllTransportistasSimples(): Flow<List<TransportistaEntity>> {
        return transportistaDao.getAll()
    }
}