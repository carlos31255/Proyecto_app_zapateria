package com.example.proyectoZapateria.data.remote.geografia

import com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO
import com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO
import retrofit2.Response
import retrofit2.http.*

interface GeografiaApiService {

    // Ciudades
    @GET("geografia/ciudades")
    suspend fun obtenerTodasLasCiudades(): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>>

    @GET("geografia/ciudades/{id}")
    suspend fun obtenerCiudadPorId(@Path("id") id: Long): Response<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>

    @GET("geografia/ciudades/region/{regionId}")
    suspend fun obtenerCiudadesPorRegion(@Path("regionId") regionId: Long): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>>

    // Comunas
    @GET("geografia/comunas")
    suspend fun obtenerTodasLasComunas(): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>

    @GET("geografia/comunas/{id}")
    suspend fun obtenerComunaPorId(@Path("id") id: Long): Response<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>

    @GET("geografia/comunas/region/{regionId}")
    suspend fun obtenerComunasPorRegion(@Path("regionId") regionId: Long): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>

    @GET("geografia/comunas/ciudad/{ciudadId}")
    suspend fun obtenerComunasPorCiudad(@Path("ciudadId") ciudadId: Long): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>

    @GET("geografia/comunas/region/{regionId}/ciudad/{ciudadId}")
    suspend fun obtenerComunasPorRegionYCiudad(
        @Path("regionId") regionId: Long,
        @Path("ciudadId") ciudadId: Long
    ): Response<List<ComunaDTO>>

    @GET("geografia/comunas/buscar")
    suspend fun buscarComunasPorNombre(@Query("nombre") nombre: String): Response<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>

    // Regiones
    @GET("geografia/regiones")
    suspend fun obtenerTodasLasRegiones(): Response<List<RegionDTO>>

    @GET("geografia/regiones/{id}")
    suspend fun obtenerRegionPorId(@Path("id") id: Long): Response<RegionDTO>

    @GET("geografia/regiones/codigo/{codigo}")
    suspend fun obtenerRegionPorCodigo(@Path("codigo") codigo: String): Response<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>

    @POST("geografia/ciudades/crear")
    suspend fun crearCiudad(@Body ciudad: com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>

    @POST("geografia/comunas/crear")
    suspend fun crearComuna(@Body comuna: com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>

    @POST("geografia/regiones/crear")
    suspend fun crearRegion(@Body region: com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>

    @PUT("geografia/ciudades/{id}")
    suspend fun actualizarCiudad(@Path("id") id: Long, @Body ciudad: com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>

    @PUT("geografia/comunas/{id}")
    suspend fun actualizarComuna(@Path("id") id: Long, @Body comuna: com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>

    @PUT("geografia/regiones/{id}")
    suspend fun actualizarRegion(@Path("id") id: Long, @Body region: com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO): Response<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>

    @DELETE("geografia/ciudades/eliminar/{id}")
    suspend fun eliminarCiudad(@Path("id") id: Long): Response<Void>

    @DELETE("geografia/comunas/eliminar/{id}")
    suspend fun eliminarComuna(@Path("id") id: Long): Response<Void>

    @DELETE("geografia/regiones/eliminar/{id}")
    suspend fun eliminarRegion(@Path("id") id: Long): Response<Void>
}
