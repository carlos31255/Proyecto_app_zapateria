package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import retrofit2.Response
import retrofit2.http.*

interface ClienteApiService {
    // ========== ENDPOINTS DE CLIENTES ==========
    // Obtener todos los clientes
    @GET("api/clientes")
    suspend fun obtenerTodosLosClientes(): List<ClienteDTO>

    // Obtener cliente por ID de persona
    @GET("api/clientes/{idPersona}")
    suspend fun obtenerClientePorId(@Path("idPersona") idPersona: Int): Response<ClienteDTO>

    // Obtener clientes por categoría (VIP, premium, regular)
    @GET("api/clientes/categoria/{categoria}")
    suspend fun obtenerClientesPorCategoria(@Path("categoria") categoria: String): List<ClienteDTO>

    // Crear nuevo cliente
    @POST("api/clientes")
    suspend fun crearCliente(@Body clienteDTO: ClienteDTO): Response<ClienteDTO>

    // Actualizar categoría de cliente
    @PUT("api/clientes/{idPersona}/categoria")
    suspend fun actualizarCategoria(
        @Path("idPersona") idPersona: Int,
        @Query("nuevaCategoria") nuevaCategoria: String
    ): Response<ClienteDTO>

    // Desactivar cliente (borrado lógico - marca como inactivo)
    @DELETE("api/clientes/{idPersona}")
    suspend fun eliminarCliente(@Path("idPersona") idPersona: Int): Response<Void>

}

