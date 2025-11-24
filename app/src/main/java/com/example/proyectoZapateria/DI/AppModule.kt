package com.example.proyectoZapateria.di

import android.content.Context
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.RolRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.DetalleBoletaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ProductoRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.repository.remote.CartRemoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // =============================================================================================
    // DATABASE CONFIGURATION
    // =============================================================================================

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    // =============================================================================================
    // REPOSITORIES
    // =============================================================================================

    @Provides
    @Singleton
    fun provideCartRemoteRepository(carritoApiService: com.example.proyectoZapateria.data.remote.carrito.CarritoApiService): com.example.proyectoZapateria.data.repository.remote.CartRemoteRepository {
        return com.example.proyectoZapateria.data.repository.remote.CartRemoteRepository(carritoApiService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        personaRemoteRepository: PersonaRemoteRepository,
        usuarioRemoteRepository: UsuarioRemoteRepository,
        rolRemoteRepository: RolRemoteRepository,
        sessionPreferences: SessionPreferences
    ): AuthRemoteRepository {
        return AuthRemoteRepository(personaRemoteRepository, usuarioRemoteRepository, rolRemoteRepository, sessionPreferences)
    }

    @Provides
    @Singleton
    fun provideClienteRemoteRepository(clienteApiService: ClienteApiService): ClienteRemoteRepository {
        return ClienteRemoteRepository(clienteApiService)
    }

    @Provides
    @Singleton
    fun provideDetalleBoletaRepository(
        ventasRemoteRepository: VentasRemoteRepository
    ): DetalleBoletaRemoteRepository {
        return DetalleBoletaRemoteRepository(ventasRemoteRepository)
    }

    @Provides
    @Singleton
    fun provideProductoRemoteRepository(
        inventarioRemoteRepository: InventarioRemoteRepository
    ): ProductoRemoteRepository {
        return ProductoRemoteRepository(inventarioRemoteRepository)
    }
}
