package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import retrofit2.Response
import retrofit2.http.*

interface ClienteApiService {
    // ========== ENDPOINTS DE CLIENTES ==========
    // Obtener todos los clientes
    @GET("clientes")
    suspend fun obtenerTodosLosClientes(): Response<List<ClienteDTO>>

    // Obtener cliente por ID de persona
    @GET("clientes/{idPersona}")
    suspend fun obtenerClientePorId(@Path("idPersona") idPersona: Long): Response<ClienteDTO>

    // Obtener clientes por categoría (VIP, premium, regular)
    @GET("clientes/categoria/{categoria}")
    suspend fun obtenerClientesPorCategoria(@Path("categoria") categoria: String): Response<List<ClienteDTO>>

    // Crear nuevo cliente
    @POST("clientes")
    suspend fun crearCliente(@Body clienteDTO: ClienteDTO): Response<ClienteDTO>

    // Actualizar categoría de cliente
    @PUT("clientes/{idPersona}/categoria")
    suspend fun actualizarCategoria(
        @Path("idPersona") idPersona: Long,
        @Query("nuevaCategoria") nuevaCategoria: String
    ): Response<ClienteDTO>

    // Desactivar cliente (borrado lógico - marca como inactivo)
    @DELETE("clientes/{idPersona}")
    suspend fun eliminarCliente(@Path("idPersona") idPersona: Long): Response<Void>

}
