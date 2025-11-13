package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import javax.inject.Inject


class ClienteRemoteRepository @Inject constructor(
    private val apiService: UsuarioApiService
) {


    // Obtener todos los clientes
    suspend fun obtenerTodosLosClientes(): Result<List<ClienteDTO>> {
        return try {
            val clientes = apiService.obtenerTodosLosClientes()
            Result.success(clientes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener cliente por ID
    suspend fun obtenerClientePorId(idPersona: Int): Result<ClienteDTO?> {
        return try {
            val response = apiService.obtenerClientePorId(idPersona)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener clientes por categoría
    suspend fun obtenerClientesPorCategoria(categoria: String): Result<List<ClienteDTO>> {
        return try {
            val clientes = apiService.obtenerClientesPorCategoria(categoria)
            Result.success(clientes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear nuevo cliente
    suspend fun crearCliente(clienteDTO: ClienteDTO): Result<ClienteDTO?> {
        return try {
            val response = apiService.crearCliente(clienteDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar categoría de cliente
    suspend fun actualizarCategoria(idPersona: Int, nuevaCategoria: String): Result<ClienteDTO?> {
        return try {
            val response = apiService.actualizarCategoria(idPersona, nuevaCategoria)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar cliente
    suspend fun eliminarCliente(idPersona: Int): Result<Boolean> {
        return try {
            val response = apiService.eliminarCliente(idPersona)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

