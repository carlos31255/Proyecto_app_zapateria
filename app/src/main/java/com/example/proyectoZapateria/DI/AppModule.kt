package com.example.proyectoZapateria.di

import android.content.Context
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.RolRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.DetalleBoletaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ModeloZapatoRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
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
    // DAOs (DATA ACCESS OBJECTS)
    // =============================================================================================


    // Solo mantenemos CartDao porque el carrito vive en el teléfono hasta el checkout
    @Provides
    @Singleton
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao()

    // NOTA: Si usas otros DAOs locales para caché (ej. UsuarioDao), agrégalos aquí.
    // Por ahora, asumimos que Inventario, Productos y Ventas son 100% remotos.

    // =============================================================================================
    // REPOSITORIES
    // =============================================================================================

    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepository(cartDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        personaRemoteRepository: PersonaRemoteRepository,
        usuarioRemoteRepository: UsuarioRemoteRepository,
        rolRemoteRepository: RolRemoteRepository,
        sessionPreferences: SessionPreferences
    ): AuthRepository {
        return AuthRepository(personaRemoteRepository, usuarioRemoteRepository, rolRemoteRepository, sessionPreferences)
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
    fun provideModeloZapatoRemoteRepository(
        inventarioRemoteRepository: InventarioRemoteRepository
    ): ModeloZapatoRemoteRepository {
        return ModeloZapatoRemoteRepository(inventarioRemoteRepository)
    }
}
