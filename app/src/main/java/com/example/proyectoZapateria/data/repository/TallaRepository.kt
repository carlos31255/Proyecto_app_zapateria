package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.talla.TallaDao
import com.example.proyectoZapateria.data.local.talla.TallaEntity

class TallaRepository(private val tallaDao: TallaDao) {

    // funcion para buscar una talla por su id
    suspend fun getById(id: Int): TallaEntity? = tallaDao.getById(id)
    //funcion para obtener todas las tallas
    fun getAll() = tallaDao.getAll()
    // Funcion para obtener una talla por su numero
    suspend fun getByNumero(numero: String): TallaEntity? = tallaDao.getByNumero(numero)
}

