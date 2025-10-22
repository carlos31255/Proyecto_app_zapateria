package com.example.proyectoZapateria.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
abstract class AppDatabase : RoomDatabase() {

    // === Catálogos base ===
    // Daos que rara vez cambian pero son necesarios
    abstract fun regionDao(): com.example.proyectoZapateria.data.local.region.RegionDao
    abstract fun comunaDao(): com.example.proyectoZapateria.data.local.comuna.ComunaDao
    abstract fun rolDao(): com.example.proyectoZapateria.data.local.rol.RolDao
    abstract fun tallaDao(): com.example.proyectoZapateria.data.local.talla.TallaDao
    abstract fun tipoMovimientoDao(): com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoDao

    // === Entidades principales ===
    abstract fun personaDao(): com.example.proyectoZapateria.data.local.persona.PersonaDao
    abstract fun clienteDao(): com.example.proyectoZapateria.data.local.cliente.ClienteDao
    abstract fun usuarioDao(): com.example.proyectoZapateria.data.local.usuario.UsuarioDao
    abstract fun transportistaDao(): com.example.proyectoZapateria.data.local.transportista.TransportistaDao

    // === Productos e inventario ===
    abstract fun marcaDao(): com.example.proyectoZapateria.data.local.marca.MarcaDao
    abstract fun modeloZapatoDao(): com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
    abstract fun inventarioDao(): com.example.proyectoZapateria.data.local.inventario.InventarioDao
    abstract fun movimientoInventarioDao(): com.example.proyectoZapateria.data.local.movimientoinventario.MovimientoInventarioDao

    // === Ventas y entregas ===
    abstract fun boletaVentaDao(): com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
    abstract fun detalleBoletaDao(): com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
    abstract fun entregaDao(): com.example.proyectoZapateria.data.local.entrega.EntregaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Nombre del archivo de base de datos
        private const val DB_NAME = "zapateria.db"

        // Obtiene la instancia única de la base de datos
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Construimos la DB con callback de precarga
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // Callback para ejecutar cuando la DB se crea por primera vez
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Lanzamos una corrutina en IO para insertar datos iniciales
                            CoroutineScope(Dispatchers.IO).launch {
                                preloadData(getInstance(context))
                            }
                        }
                    })
                    // En entorno educativo, si cambias versión sin migraciones, destruye y recrea
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // Función para precargar datos iniciales en la base de datos
        private suspend fun preloadData(database: AppDatabase) {
            // TODO: Implementar precarga de datos iniciales
            // Ejemplo: roles, regiones, comunas, tipos de movimiento, etc.
        }
    }
}
