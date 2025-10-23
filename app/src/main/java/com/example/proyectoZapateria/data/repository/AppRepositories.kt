package com.example.proyectoZapateria.data.repository

import android.content.Context
import com.example.proyectoZapateria.data.local.database.AppDatabase


//Clase que centraliza todos los repositories de la aplicación.

class AppRepositories(context: Context) {

    // Instancia única de la base de datos
    private val database = AppDatabase.getInstance(context)

    // ===== Repositories de catálogos base =====
    // TODO: Agregar cuando estén creados
    // val regionRepository = RegionRepository(database.regionDao())
    // val comunaRepository = ComunaRepository(database.comunaDao())
    // val rolRepository = RolRepository(database.rolDao())
    // val tallaRepository = TallaRepository(database.tallaDao())
    // val tipoMovimientoRepository = TipoMovimientoRepository(database.tipoMovimientoDao())

    // ===== Repositories de entidades principales =====
    val personaRepository = PersonaRepository(database.personaDao())
    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    // TODO: Agregar cuando estén creados
    // val clienteRepository = ClienteRepository(database.clienteDao())
    // val transportistaRepository = TransportistaRepository(database.transportistaDao())

    // ===== Repositories de productos e inventario =====
    val marcaRepository = MarcaRepository(database.marcaDao())
    val modeloZapatoRepository = ModeloZapatoRepository(database.modeloZapatoDao())
    // TODO: Agregar cuando estén creados
    // val inventarioRepository = InventarioRepository(database.inventarioDao())
    // val movimientoInventarioRepository = MovimientoInventarioRepository(database.movimientoInventarioDao())

    // ===== Repositories de ventas y entregas =====
    // TODO: Agregar cuando estén creados
    // val boletaVentaRepository = BoletaVentaRepository(database.boletaVentaDao())
    // val detalleBoletaRepository = DetalleBoletaRepository(database.detalleBoletaDao())
    // val entregaRepository = EntregaRepository(database.entregaDao())
}

