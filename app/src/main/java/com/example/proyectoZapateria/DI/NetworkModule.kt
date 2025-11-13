package com.example.proyectoZapateria.di

import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
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
    private const val BASE_URL = "http://10.0.2.2:8083/"

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

    // 5. Provee tu API Service
    @Provides
    @Singleton
    fun provideUsuarioApiService(retrofit: Retrofit): UsuarioApiService {
        return retrofit.create(UsuarioApiService::class.java)
    }

    // Puedes agregar más @Provides para otros microservicios (Ej: ProductoApiService)
}
