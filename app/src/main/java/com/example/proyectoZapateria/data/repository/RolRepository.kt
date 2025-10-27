package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.rol.RolDao
import com.example.proyectoZapateria.data.local.rol.RolEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RolRepository @Inject constructor(
    private val rolDao: RolDao
) {
    fun getAllRoles(): Flow<List<RolEntity>> = rolDao.getAll()

    suspend fun getRolById(id: Int): RolEntity? = rolDao.getById(id)

    suspend fun getRolByNombre(nombre: String): RolEntity? = rolDao.getByNombre(nombre)
}

