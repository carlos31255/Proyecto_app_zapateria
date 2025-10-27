package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.cliente.ClienteConPersona
import com.example.proyectoZapateria.data.local.cliente.ClienteDao
import kotlinx.coroutines.flow.Flow

class ClienteRepository(private val clienteDao: ClienteDao) {

    suspend fun getByIdConPersona(id: Int): ClienteConPersona? {
        return clienteDao.getByIdConPersona(id)
    }

    suspend fun getByRut(rut: String): ClienteConPersona? {
        return clienteDao.getByRut(rut)
    }

    fun getAll(): Flow<List<com.example.proyectoZapateria.data.local.cliente.ClienteEntity>> = clienteDao.getAll()

}

