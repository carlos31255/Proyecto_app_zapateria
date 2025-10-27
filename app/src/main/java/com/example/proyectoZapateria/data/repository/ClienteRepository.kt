package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.cliente.ClienteConPersona
import com.example.proyectoZapateria.data.local.cliente.ClienteDao
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import kotlinx.coroutines.flow.Flow

class ClienteRepository(private val clienteDao: ClienteDao) {

    suspend fun getByIdConPersona(id: Int): ClienteConPersona? {
        return clienteDao.getByIdConPersona(id)
    }

    @Suppress("unused")
    suspend fun getByRut(rut: String): ClienteConPersona? {
        return clienteDao.getByRut(rut)
    }

    fun getAll(): Flow<List<ClienteEntity>> = clienteDao.getAll()

    fun getAllConPersona(): Flow<List<ClienteConPersona>> = clienteDao.getAllConPersona()

    suspend fun insert(cliente: ClienteEntity): Long {
        return clienteDao.insert(cliente)
    }

}

