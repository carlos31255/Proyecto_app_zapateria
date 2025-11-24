package com.example.proyectoZapateria.data.remote.carrito

import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import retrofit2.Response
import retrofit2.http.*

interface CarritoApiService {

    @GET("api/carrito/{clienteId}")
    suspend fun getCart(@Path("clienteId") clienteId: Long): Response<List<CartItemResponse>>

    @POST("api/carrito")
    suspend fun addOrUpdate(@Body req: CartItemRequest): Response<Long>

    // Variante que incluye clienteId como query param (backend acepta clienteId por query para mayor robustez)
    @POST("api/carrito")
    suspend fun addOrUpdate(@Query("clienteId") clienteId: Long, @Body req: CartItemRequest): Response<Long>

    @GET("api/carrito/{clienteId}/count")
    suspend fun count(@Path("clienteId") clienteId: Long): Response<Int>

    @DELETE("api/carrito/{clienteId}/items/{id}")
    suspend fun deleteItem(@Path("clienteId") clienteId: Long, @Path("id") id: Long): Response<Void>

    @DELETE("api/carrito/{clienteId}")
    suspend fun clear(@Path("clienteId") clienteId: Long): Response<Void>

    @GET("api/carrito/{clienteId}/item")
    suspend fun getItem(@Path("clienteId") clienteId: Long, @Query("modeloId") modeloId: Long, @Query("talla") talla: String): Response<CartItemResponse>
}
