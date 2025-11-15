package com.example.proyectoZapateria.di

import com.example.proyectoZapateria.BuildConfig
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // URL del microservicio de usuarios
    // Emulador: 10.0.2.2 apunta al localhost de tu PC
    // Dispositivo físico: cambiar por tu IP local (ej: 192.168.1.5)
    private const val BASE_URL = BuildConfig.BASE_URL_USUARIOS

    // TODO: Una vez que compile, cambiar a: BuildConfig.BASE_URL_USUARIOS

    // 2. Provee el Interceptor de Logging
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Nivel BODY para ver todo
        }
    }

    // 3. Provee el Cliente OkHttp
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // 4. Provee la instancia de Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ==== SERVICIOS DE API ====
    @Provides
    @Singleton
    fun provideUsuarioApiService(retrofit: Retrofit): UsuarioApiService {
        return retrofit.create(UsuarioApiService::class.java)
    }

    // Provee el API Service de Clientes
    @Provides
    @Singleton
    fun provideClienteApiService(retrofit: Retrofit): ClienteApiService {
        return retrofit.create(ClienteApiService::class.java)
    }

    // Provee el API Service de Roles
    @Provides
    @Singleton
    fun provideRolApiService(retrofit: Retrofit): RolApiService {
        return retrofit.create(RolApiService::class.java)
    }

    // Provee el API Service de Personas
    @Provides
    @Singleton
    fun providePersonaApiService(retrofit: Retrofit): PersonaApiService {
        return retrofit.create(PersonaApiService::class.java)
    }

    // Provee el API Service de Ventas
    @Provides
    @Singleton
    fun provideVentasApiService(retrofit: Retrofit): VentasApiService {
        return retrofit.create(VentasApiService::class.java)
    }

    // Provee el API Service de Entregas
    @Provides
    @Singleton
    fun provideEntregasApiService(retrofit: Retrofit): EntregasApiService {
        return retrofit.create(EntregasApiService::class.java)
    }

    // Puedes agregar más @Provides para otros microservicios (Ej: ProductoApiService)
}
