package com.example.proyectoZapateria.data.repository.remote

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
            val response = clienteApi.obtenerTodosLosClientes()
            if (response.isSuccessful) {
                val clientes = response.body() ?: emptyList()
                Result.success(clientes)
            } else {
                val msg = "Error ${response.code()}: ${response.message()} - body=${try { response.errorBody()?.string() } catch (_: Exception) { "<err>" }}"
                Log.e("ClienteRemoteRepo", "obtenerTodos: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al obtener clientes: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener cliente por ID
    suspend fun obtenerClientePorId(idPersona: Long): Result<ClienteDTO?> {
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
            val response = clienteApi.obtenerClientesPorCategoria(categoria)
            if (response.isSuccessful) {
                val clientes = response.body() ?: emptyList()
                Result.success(clientes)
            } else {
                val msg = "Error ${response.code()}: ${response.message()} - body=${try { response.errorBody()?.string() } catch (_: Exception) { "<err>" }}"
                Log.e("ClienteRemoteRepo", "obtenerPorCategoria: $msg")
                Result.failure(Exception(msg))
            }
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
    suspend fun actualizarCategoria(idPersona: Long, nuevaCategoria: String): Result<ClienteDTO?> {
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
    suspend fun eliminarCliente(idPersona: Long): Result<Boolean> {
        return try {
            val response = clienteApi.eliminarCliente(idPersona)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("ClienteRemoteRepo", "Error al eliminar cliente: ${e.message}", e)
            Result.failure(e)
        }
    }
}