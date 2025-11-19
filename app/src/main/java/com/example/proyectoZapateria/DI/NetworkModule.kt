package com.example.proyectoZapateria.di

import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
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
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsuarioRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OtrosRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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

    // --- HELPER ---
    private fun buildRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ======================================================
    //  RETROFIT CONSTRUCTOR (Solo uno activo por ahora)
    // ======================================================

    @Provides
    @Singleton
    @UsuarioRetrofit // <--- Creamos el Retrofit con esta etiqueta
    fun provideRetrofitUsuarios(okHttpClient: OkHttpClient): Retrofit {
        // URL hardcodeada como la tenías para destrabar la compilación
        return buildRetrofit("https://t4ld1ws9-8083.brs.devtunnels.ms/", okHttpClient)
    }

    // ======================================================
    //  CONSUMIDORES (Todos usan @UsuarioRetrofit para compilar)
    // ======================================================

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

    // === AQUÍ ESTABA EL ERROR ANTES ===
    // Le agregamos @UsuarioRetrofit para que use la instancia que SÍ existe.

    @Provides
    @Singleton
    fun provideVentasApiService(@UsuarioRetrofit retrofit: Retrofit): VentasApiService {
        return retrofit.create(VentasApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideEntregasApiService(@UsuarioRetrofit retrofit: Retrofit): EntregasApiService {
        return retrofit.create(EntregasApiService::class.java)
    }
}