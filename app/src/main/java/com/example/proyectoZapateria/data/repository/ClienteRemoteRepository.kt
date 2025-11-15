package com.example.proyectoZapateria.data.repository

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRemoteRepository @Inject constructor(
    private val clienteApi: ClienteApiService
) {


    // Obtener todos los clientes
    suspend fun obtenerTodosLosClientes(): Result<List<ClienteDTO>> {
        return try {
            val clientes = clienteApi.obtenerTodosLosClientes()
            Result.success(clientes)
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al obtener clientes: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener cliente por ID
    suspend fun obtenerClientePorId(idPersona: Int): Result<ClienteDTO?> {
        return try {
            val response = clienteApi.obtenerClientePorId(idPersona)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al obtener cliente por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener clientes por categoría
    suspend fun obtenerClientesPorCategoria(categoria: String): Result<List<ClienteDTO>> {
        return try {
            val clientes = clienteApi.obtenerClientesPorCategoria(categoria)
            Result.success(clientes)
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al obtener clientes por categoría: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nuevo cliente
    suspend fun crearCliente(clienteDTO: ClienteDTO): Result<ClienteDTO?> {
        return try {
            val response = clienteApi.crearCliente(clienteDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al crear cliente: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar categoría de cliente
    suspend fun actualizarCategoria(idPersona: Int, nuevaCategoria: String): Result<ClienteDTO?> {
        return try {
            val response = clienteApi.actualizarCategoria(idPersona, nuevaCategoria)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al actualizar categoría: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Desactivar cliente
    suspend fun eliminarCliente(idPersona: Int): Result<Boolean> {
        return try {
            val response = clienteApi.eliminarCliente(idPersona)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al eliminar cliente: ${e.message}", e)
            Result.failure(e)
        }
    }
}

