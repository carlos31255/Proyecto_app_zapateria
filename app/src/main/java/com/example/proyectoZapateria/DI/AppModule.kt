package com.example.proyectoZapateria.DI

import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    // Provee el EntregaDao
    @Provides
    @Singleton
    fun provideEntregaDao(database: AppDatabase): EntregaDao {
        return database.entregaDao() // (Asegúrate que tu AppDatabase tiene esta función)
    }

    // Provee el EntregaRepository
    @Provides
    @Singleton
    fun provideEntregaRepository(entregaDao: EntregaDao): EntregaRepository {
        return EntregaRepository(entregaDao)
    }

    // Provee el DetalleBoletaDao
    @Provides
    @Singleton
    fun provideDetalleBoletaDao(database: AppDatabase): DetalleBoletaDao {
        return database.detalleBoletaDao() // (Asegúrate que tu AppDatabase tiene esta función)
    }

    // Provee el DetalleBoletaRepository
    @Provides
    @Singleton
    fun provideDetalleBoletaRepository(detalleBoletaDao: DetalleBoletaDao): DetalleBoletaRepository {
        return DetalleBoletaRepository(detalleBoletaDao)
    }
}