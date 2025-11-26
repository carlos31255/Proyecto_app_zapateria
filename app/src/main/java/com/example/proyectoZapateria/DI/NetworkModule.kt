package com.example.proyectoZapateria.di

import com.example.proyectoZapateria.data.remote.carrito.CarritoApiService
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
import com.example.proyectoZapateria.data.remote.ventas.ReportesVentasApiService
import com.example.proyectoZapateria.data.remote.reportes.ReportesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier


// Anotaciones para identificar los Retrofit
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsuarioRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InventarioRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EntregasRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VentasRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeografiaRetrofit


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Base URL pública para inventario (se usa en UI al cargar imágenes desde endpoint /inventario/productos/{id}/imagen)
    const val INVENTARIO_BASE_URL = "https://t4ld1ws9-8082.brs.devtunnels.ms/"

     // 1. Provee el Interceptor de Logging
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // 2. Provee el Cliente OkHttp
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // --- HELPER para no repetir código ---
    private fun buildRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ======================================================
    //  RETROFIT CONSTRUCTOR
    // ======================================================

    @Provides
    @Singleton
    @GeografiaRetrofit
    fun provideRetrofitGeografia(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit("https://t4ld1ws9-8081.brs.devtunnels.ms/", okHttpClient)
    }

    @Provides
    @Singleton
    @InventarioRetrofit
    fun provideRetrofitInventario(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit("https://t4ld1ws9-8082.brs.devtunnels.ms/", okHttpClient)
    }

    @Provides
    @Singleton
    @UsuarioRetrofit
    fun provideRetrofitUsuarios(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit("https://t4ld1ws9-8083.brs.devtunnels.ms/", okHttpClient)
    }

    @Provides
    @Singleton
    @VentasRetrofit
    fun provideRetrofitVentas(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit("https://t4ld1ws9-8084.brs.devtunnels.ms/", okHttpClient)
    }

    @Provides
    @Singleton
    @EntregasRetrofit
    fun provideRetrofitEntregas(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit("https://t4ld1ws9-8085.brs.devtunnels.ms/", okHttpClient)
    }

    // ======================================================
    //  CONSUMIDORES (Todos usan @UsuarioRetrofit para compilar)
    // ======================================================
    //USUARIOS
    @Provides
    @Singleton
    fun provideUsuarioApiService(@UsuarioRetrofit retrofit: Retrofit): UsuarioApiService {
        return retrofit.create(UsuarioApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClienteApiService(@UsuarioRetrofit retrofit: Retrofit): ClienteApiService {
        return retrofit.create(ClienteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRolApiService(@UsuarioRetrofit retrofit: Retrofit): RolApiService {
        return retrofit.create(RolApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePersonaApiService(@UsuarioRetrofit retrofit: Retrofit): PersonaApiService {
        return retrofit.create(PersonaApiService::class.java)
    }

    // Transportista API (usuarios)
    @Provides
    @Singleton
    fun provideTransportistaApiService(@UsuarioRetrofit retrofit: Retrofit): com.example.proyectoZapateria.data.remote.usuario.TransportistaApiService {
        return retrofit.create(com.example.proyectoZapateria.data.remote.usuario.TransportistaApiService::class.java)
    }


    // VENTAS
    @Provides
    @Singleton
    fun provideVentasApiService(@VentasRetrofit retrofit: Retrofit): VentasApiService {
        return retrofit.create(VentasApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReportesVentasApiService(@VentasRetrofit retrofit: Retrofit): ReportesVentasApiService {
        return retrofit.create(ReportesVentasApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCarritoApiService(@VentasRetrofit retrofit: Retrofit): CarritoApiService {
        return retrofit.create(CarritoApiService::class.java)
    }

    //ENTREGAS

    @Provides
    @Singleton
    fun provideEntregasApiService(@EntregasRetrofit retrofit: Retrofit): EntregasApiService {
        return retrofit.create(EntregasApiService::class.java)
    }

    //INVENTARIO

    @Provides
    @Singleton
    fun provideInventarioApiService(@InventarioRetrofit retrofit: Retrofit): InventarioApiService {
        return retrofit.create(InventarioApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProductoApiService(@InventarioRetrofit retrofit: Retrofit): ProductoApiService {
        return retrofit.create(ProductoApiService::class.java)
    }

    // REPORTES
    @Provides
    @Singleton
    fun provideReportesApiService(@InventarioRetrofit retrofit: Retrofit): ReportesApiService {
        return retrofit.create(ReportesApiService::class.java)
    }


    // GEOGRAFÍA
    @Provides
    @Singleton
    fun provideGeografiaApiService(@GeografiaRetrofit retrofit: Retrofit): com.example.proyectoZapateria.data.remote.geografia.GeografiaApiService {
        return retrofit.create(com.example.proyectoZapateria.data.remote.geografia.GeografiaApiService::class.java)
    }
}