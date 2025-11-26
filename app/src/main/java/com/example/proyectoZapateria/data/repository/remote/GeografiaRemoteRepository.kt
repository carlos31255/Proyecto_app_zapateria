package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.geografia.GeografiaApiService
import com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO
import com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO
import com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeografiaRemoteRepository @Inject constructor(
    private val api: GeografiaApiService
) {
    suspend fun obtenerTodasLasCiudades(): Result<List<CiudadDTO>> {
        return try {
            val response = api.obtenerTodasLasCiudades()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${'$'}{response.code()}: ${'$'}{response.message()}"))
        } catch (e: Exception) {
            Log.e("GeografiaRemoteRepo", "Error al obtener ciudades: ${'$'}{e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerCiudadesPorRegion(regionId: Long): Result<List<CiudadDTO>> {
        return try {
            val response = api.obtenerCiudadesPorRegion(regionId)
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${'$'}{response.code()}: ${'$'}{response.message()}"))
        } catch (e: Exception) {
            Log.e("GeografiaRemoteRepo", "Error al obtener ciudades por region: ${'$'}{e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerTodasLasComunas(): Result<List<ComunaDTO>> {
        return try {
            val response = api.obtenerTodasLasComunas()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${'$'}{response.code()}: ${'$'}{response.message()}"))
        } catch (e: Exception) {
            Log.e("GeografiaRemoteRepo", "Error al obtener comunas: ${'$'}{e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerComunasPorCiudad(ciudadId: Long): Result<List<ComunaDTO>> {
        return try {
            val response = api.obtenerComunasPorCiudad(ciudadId)
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${'$'}{response.code()}: ${'$'}{response.message()}"))
        } catch (e: Exception) {
            Log.e("GeografiaRemoteRepo", "Error al obtener comunas por ciudad: ${'$'}{e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerTodasLasRegiones(): Result<List<RegionDTO>> {
        return try {
            val response = api.obtenerTodasLasRegiones()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${'$'}{response.code()}: ${'$'}{response.message()}"))
        } catch (e: Exception) {
            Log.e("GeografiaRemoteRepo", "Error al obtener regiones: ${'$'}{e.message}", e)
            Result.failure(e)
        }
    }
}
