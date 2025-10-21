package com.example.proyectoZapateria.data.local.database

import androidx.room.Database
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import com.example.proyectoZapateria.data.local.comuna.ComunaEntity
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity
import com.example.proyectoZapateria.data.local.entrega.EntregaEntity
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.movimientoinventario.MovimientoInventarioEntity
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.region.RegionEntity
import com.example.proyectoZapateria.data.local.rol.RolEntity
import com.example.proyectoZapateria.data.local.talla.TallaEntity
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity

@Database(
    entities = [
        // Catálogos base
        RegionEntity::class,
        ComunaEntity::class,
        RolEntity::class,
        MarcaEntity::class,
        TallaEntity::class,
        TipoMovimientoEntity::class,

        // Entidades principales
        PersonaEntity::class,
        ClienteEntity::class,
        UsuarioEntity::class,
        TransportistaEntity::class,

        // Productos e inventario
        ModeloZapatoEntity::class,
        InventarioEntity::class,
        MovimientoInventarioEntity::class,

        // Ventas y entregas
        BoletaVentaEntity::class,
        DetalleBoletaEntity::class,
        EntregaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    // Dao de las entidades

}
