package com.example.proyectoZapateria.di

import android.content.Context
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.local.inventario.InventarioDao
import com.example.proyectoZapateria.data.local.talla.TallaDao
import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.MarcaRepository
import com.example.proyectoZapateria.data.repository.ModeloZapatoRepository
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.data.repository.TallaRepository
import com.example.proyectoZapateria.data.repository.TransportistaRepository
import com.example.proyectoZapateria.data.repository.ClienteRemoteRepository
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ========== Database ==========

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    // ========== DAOs ==========


    // Provee el EntregaDao
    @Provides
    @Singleton
    fun provideEntregaDao(database: AppDatabase): EntregaDao {
        return database.entregaDao()
    }

    // Provee el DetalleBoletaDao
    @Provides
    @Singleton
    fun provideDetalleBoletaDao(database: AppDatabase): DetalleBoletaDao {
        return database.detalleBoletaDao()
    }

    // Provee el MarcaDao
    @Provides
    @Singleton
    fun provideMarcaDao(database: AppDatabase): MarcaDao {
        return database.marcaDao()
    }

    // Provee el ModeloZapatoDao
    @Provides
    @Singleton
    fun provideModeloZapatoDao(database: AppDatabase): ModeloZapatoDao {
        return database.modeloZapatoDao()
    }

    // Provee el TallaDao
    @Provides
    @Singleton
    fun provideTallaDao(database: AppDatabase): TallaDao {
        return database.tallaDao()
    }

    // Provee el TransportistaDao
    @Provides
    @Singleton
    fun provideTransportistaDao(database: AppDatabase): TransportistaDao {
        return database.transportistaDao()
    }

    // Provee el InventarioDao
    @Provides
    @Singleton
    fun provideInventarioDao(database: AppDatabase): InventarioDao {
        return database.inventarioDao()
    }

    // Provee el BoletaVentaDao
    @Provides
    @Singleton
    fun provideBoletaVentaDao(database: AppDatabase): BoletaVentaDao {
        return database.boletaVentaDao()
    }

    // Provee el CartDao
    @Provides
    @Singleton
    fun provideCartDao(database: AppDatabase): CartDao {
        return database.cartDao()
    }

    // ========== Preferences ==========

    // NOTE: SessionPreferences tiene un constructor @Inject con @ApplicationContext,
    // por lo que no es necesario (y provoca binding duplicado) proveerlo manualmente aquí.
    // Si prefieres mantener un proveedor explícito, elimina el @Inject constructor de la clase.

    // ========== Repositories ==========

    // Provee el AuthRepository (ahora usa remoto)
    @Provides
    @Singleton
    fun provideAuthRepository(
        personaRemoteRepository: com.example.proyectoZapateria.data.repository.PersonaRemoteRepository,
        usuarioRemoteRepository: com.example.proyectoZapateria.data.repository.UsuarioRemoteRepository,
        rolRemoteRepository: com.example.proyectoZapateria.data.repository.RolRemoteRepository
    ): AuthRepository {
        return AuthRepository(personaRemoteRepository, usuarioRemoteRepository, rolRemoteRepository)
    }



    // Provee el DetalleBoletaRepository
    @Provides
    @Singleton
    fun provideDetalleBoletaRepository(
        detalleBoletaDao: DetalleBoletaDao,
        boletaVentaDao: BoletaVentaDao
    ): DetalleBoletaRepository {
        return DetalleBoletaRepository(detalleBoletaDao, boletaVentaDao)
    }

    // Provee el MarcaRepository
    @Provides
    @Singleton
    fun provideMarcaRepository(marcaDao: MarcaDao): MarcaRepository {
        return MarcaRepository(marcaDao)
    }

    // Provee el ModeloZapatoRepository
    @Provides
    @Singleton
    fun provideModeloZapatoRepository(modeloZapatoDao: ModeloZapatoDao): ModeloZapatoRepository {
        return ModeloZapatoRepository(modeloZapatoDao)
    }

    // Provee el ProductoRepository
    @Provides
    @Singleton
    fun provideProductoRepository(
        modeloZapatoDao: ModeloZapatoDao,
        marcaDao: MarcaDao,
        tallaDao: TallaDao,
        inventarioDao: InventarioDao
    ): ProductoRepository {
        return ProductoRepository(modeloZapatoDao, marcaDao, tallaDao, inventarioDao)
    }

    // Provee el TallaRepository
    @Provides
    @Singleton
    fun provideTallaRepository(tallaDao: TallaDao): TallaRepository {
        return TallaRepository(tallaDao)
    }

    // Provee el TransportistaRepository
    @Provides
    @Singleton
    fun provideTransportistaRepository(
        transportistaDao: TransportistaDao
    ): TransportistaRepository {
        return TransportistaRepository(transportistaDao)
    }

    // Provee el InventarioRepository
    @Provides
    @Singleton
    fun provideInventarioRepository(inventarioDao: InventarioDao): InventarioRepository {
        return InventarioRepository(inventarioDao)
    }


    // Provee el CartRepository
    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao, inventarioRepository: InventarioRepository, tallaRepository: TallaRepository): CartRepository {
        return CartRepository(cartDao, inventarioRepository, tallaRepository)
    }

    // ========== Remote Repositories ==========

    // Provee el ClienteRemoteRepository (usa microservicio REST en lugar de SQLite)
    @Provides
    @Singleton
    fun provideClienteRemoteRepository(clienteApiService: ClienteApiService): ClienteRemoteRepository {
        return ClienteRemoteRepository(clienteApiService)
    }
}