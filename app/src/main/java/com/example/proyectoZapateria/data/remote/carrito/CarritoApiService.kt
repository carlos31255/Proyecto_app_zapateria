package com.example.proyectoZapateria.data.remote.carrito

import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import retrofit2.Response
import retrofit2.http.*

interface CarritoApiService {

    @GET("carrito/{clienteId}")
    suspend fun getCart(@Path("clienteId") clienteId: Long): Response<List<CartItemResponse>>

    // El backend devuelve la lista consolidada en POST /carrito
    @POST("carrito")
    suspend fun addOrUpdate(@Body req: CartItemRequest): Response<List<CartItemResponse>>

    @GET("carrito/{clienteId}/count")
    suspend fun count(@Path("clienteId") clienteId: Long): Response<Int>

    @DELETE("carrito/{clienteId}/items/{id}")
    suspend fun deleteItem(@Path("clienteId") clienteId: Long, @Path("id") id: Long): Response<Void>

    @DELETE("carrito/{clienteId}")
    suspend fun clear(@Path("clienteId") clienteId: Long): Response<Void>

    @GET("carrito/{clienteId}/item")
    suspend fun getItem(@Path("clienteId") clienteId: Long, @Query("modeloId") modeloId: Long, @Query("talla") talla: String): Response<CartItemResponse>

    // El backend también soporta POST /carrito para agregar y devuelve el carrito completo
    @POST("carrito")
    suspend fun addToCart(@Query("clienteId") clienteId: Long? = null, @Body req: CartItemRequest): Response<List<CartItemResponse>>
}
