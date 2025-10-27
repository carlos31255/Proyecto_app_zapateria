package com.example.proyectoZapateria.di

import android.content.Context
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.local.persona.PersonaDao
import com.example.proyectoZapateria.data.local.usuario.UsuarioDao
import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.MarcaRepository
import com.example.proyectoZapateria.data.repository.ModeloZapatoRepository
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.data.repository.TransportistaRepository
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.TallaRepository
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

    // Provee el PersonaDao
    @Provides
    @Singleton
    fun providePersonaDao(database: AppDatabase): PersonaDao {
        return database.personaDao()
    }

    // Provee el UsuarioDao
    @Provides
    @Singleton
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao {
        return database.usuarioDao()
    }

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
    fun provideTallaDao(database: AppDatabase): com.example.proyectoZapateria.data.local.talla.TallaDao {
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
    fun provideInventarioDao(database: AppDatabase): com.example.proyectoZapateria.data.local.inventario.InventarioDao {
        return database.inventarioDao()
    }

    // Provee el BoletaVentaDao
    @Provides
    @Singleton
    fun provideBoletaVentaDao(database: AppDatabase): com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao {
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

    // Provee el PersonaRepository
    @Provides
    @Singleton
    fun providePersonaRepository(personaDao: PersonaDao): PersonaRepository {
        return PersonaRepository(personaDao)
    }

    // Provee el UsuarioRepository
    @Provides
    @Singleton
    fun provideUsuarioRepository(usuarioDao: UsuarioDao): UsuarioRepository {
        return UsuarioRepository(usuarioDao)
    }

    // Provee el AuthRepository
    @Provides
    @Singleton
    fun provideAuthRepository(
        personaDao: PersonaDao,
        usuarioDao: UsuarioDao
    ): AuthRepository {
        return AuthRepository(personaDao, usuarioDao)
    }

    // Provee el EntregaRepository
    @Provides
    @Singleton
    fun provideEntregaRepository(entregaDao: EntregaDao): EntregaRepository {
        return EntregaRepository(entregaDao)
    }


    // Provee el DetalleBoletaRepository
    @Provides
    @Singleton
    fun provideDetalleBoletaRepository(detalleBoletaDao: DetalleBoletaDao): DetalleBoletaRepository {
        return DetalleBoletaRepository(detalleBoletaDao)
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
        marcaDao: MarcaDao
    ): ProductoRepository {
        return ProductoRepository(modeloZapatoDao, marcaDao)
    }

    // Provee el TallaRepository
    @Provides
    @Singleton
    fun provideTallaRepository(tallaDao: com.example.proyectoZapateria.data.local.talla.TallaDao): com.example.proyectoZapateria.data.repository.TallaRepository {
        return com.example.proyectoZapateria.data.repository.TallaRepository(tallaDao)
    }

    // Provee el TransportistaRepository
    @Provides
    @Singleton
    fun provideTransportistaRepository(transportistaDao: TransportistaDao): TransportistaRepository {
        return TransportistaRepository(transportistaDao)
    }

    // Provee el InventarioRepository
    @Provides
    @Singleton
    fun provideInventarioRepository(inventarioDao: com.example.proyectoZapateria.data.local.inventario.InventarioDao): com.example.proyectoZapateria.data.repository.InventarioRepository {
        return com.example.proyectoZapateria.data.repository.InventarioRepository(inventarioDao)
    }

    // Provee el ClienteRepository (usar AppDatabase para evitar problemas de binding directo de ClienteDao)
    @Provides
    @Singleton
    fun provideClienteRepository(database: AppDatabase): com.example.proyectoZapateria.data.repository.ClienteRepository{
        return com.example.proyectoZapateria.data.repository.ClienteRepository(database.clienteDao())
    }

    // Provee el CartRepository
    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao, inventarioRepository: InventarioRepository, tallaRepository: TallaRepository): CartRepository {
        return CartRepository(cartDao, inventarioRepository, tallaRepository)
    }
}