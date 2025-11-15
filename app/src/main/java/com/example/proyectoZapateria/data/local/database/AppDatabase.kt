package com.example.proyectoZapateria.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import com.example.proyectoZapateria.data.local.comuna.ComunaDao
import com.example.proyectoZapateria.data.local.comuna.ComunaEntity
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaEntity
import com.example.proyectoZapateria.data.local.inventario.InventarioDao
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.movimientoinventario.MovimientoInventarioDao
import com.example.proyectoZapateria.data.local.movimientoinventario.MovimientoInventarioEntity
import com.example.proyectoZapateria.data.local.region.RegionDao
import com.example.proyectoZapateria.data.local.region.RegionEntity
import com.example.proyectoZapateria.data.local.talla.TallaDao
import com.example.proyectoZapateria.data.local.talla.TallaEntity
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoDao
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Database(
    entities = [
        // Catálogos base
        RegionEntity::class,
        ComunaEntity::class,
        MarcaEntity::class,
        TallaEntity::class,
        TipoMovimientoEntity::class,

        // Entidades principales (solo Transportista - Persona, Usuario, Rol, Cliente ahora son remotos)
        TransportistaEntity::class,

        // Productos e inventario
        ModeloZapatoEntity::class,
        InventarioEntity::class,
        MovimientoInventarioEntity::class,

        // Ventas y entregas
        BoletaVentaEntity::class,
        DetalleBoletaEntity::class,
        EntregaEntity::class,

        // Carrito
        CartItemEntity::class
    ],
    version = 14, // Incrementamos la versión por el cambio de schema
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // === Catálogos base ===
    // Daos que rara vez cambian pero son necesarios
    abstract fun regionDao(): RegionDao
    abstract fun comunaDao(): ComunaDao
    abstract fun tallaDao(): TallaDao
    abstract fun tipoMovimientoDao(): TipoMovimientoDao

    // === Entidades principales ===
    // PersonaDao, ClienteDao, UsuarioDao y RolDao ahora se manejan con microservicio
    abstract fun transportistaDao(): TransportistaDao

    // === Productos e inventario ===
    abstract fun marcaDao(): MarcaDao
    abstract fun modeloZapatoDao(): ModeloZapatoDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun movimientoInventarioDao(): MovimientoInventarioDao

    // === Ventas y entregas ===
    abstract fun boletaVentaDao(): BoletaVentaDao
    abstract fun detalleBoletaDao(): DetalleBoletaDao
    abstract fun entregaDao(): EntregaDao

    // === Carrito ===
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Señal pública que indica si la precarga inicial terminó
        private val _preloadComplete = MutableStateFlow(false)
        val preloadComplete = _preloadComplete.asStateFlow()

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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "onCreate callback - iniciando precarga de datos")
                            // Lanzamos una corrutina en IO para insertar datos iniciales
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    Log.d("AppDatabase", "Iniciando preloadData...")
                                    preloadData(getInstance(context))
                                    Log.d("AppDatabase", "preloadData completado exitosamente")
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "Error en preloadData: ${e.message}", e)
                                } finally {
                                    // Asegurarse de que la señal se marque aunque algo falle
                                    _preloadComplete.value = true
                                    Log.d("AppDatabase", "Precarga marcada como completa")
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("AppDatabase", "onOpen callback - base de datos abierta")
                            // Si la DB ya existe, marcar precarga como completa
                            if (!_preloadComplete.value) {
                                _preloadComplete.value = true
                            }
                        }
                    })
                    // En entorno educativo, si cambias versión sin migraciones, destruye y recrea
                    .fallbackToDestructiveMigration()
                    // Permitir consultas en el hilo principal solo para debugging (eliminar en producción)
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                Log.d("AppDatabase", "Instancia de base de datos creada")
                instance
            }
        }

        // Función para precargar datos iniciales en la base de datos
        // Solo precarga tablas locales: Marcas, Modelos, Tallas e Inventario
        // Las tablas de Persona, Usuario, Rol y Cliente ahora se manejan con microservicios
        private suspend fun preloadData(database: AppDatabase) {
            val marcaDao = database.marcaDao()
            val modeloDao = database.modeloZapatoDao()
            val tallaDao = database.tallaDao()
            val inventarioDao = database.inventarioDao()

            try {
                // Marca propia de la aplicación
                val appBrandName = "StepStyle"
                val appMarca = marcaDao.getMarcaByNombre(appBrandName)
                val appMarcaId = if (appMarca == null) {
                    val id = marcaDao.insertMarca(
                        MarcaEntity(
                            idMarca = 0,
                            nombreMarca = appBrandName,
                            descripcion = "Marca propia StepStyle",
                            estado = "activa"
                        )
                    )
                    id.toInt()
                } else {
                    appMarca.idMarca
                }

                // Precargar modelos para la marca propia
                val modelosApp = listOf(
                    ModeloZapatoEntity(
                        idModelo = 0,
                        idMarca = appMarcaId,
                        nombreModelo = "StepStyle Classic",
                        descripcion = "Zapatillas clásicas cómodas",
                        precioUnitario = 39990,
                        imagenUrl = "classic",
                        estado = "activo"
                    ),
                    ModeloZapatoEntity(
                        idModelo = 0,
                        idMarca = appMarcaId,
                        nombreModelo = "StepStyle Runner",
                        descripcion = "Runner ligero para entrenamiento",
                        precioUnitario = 49990,
                        imagenUrl = "runner",
                        estado = "activo"
                    ),
                    ModeloZapatoEntity(
                        idModelo = 0,
                        idMarca = appMarcaId,
                        nombreModelo = "StepStyle Urban",
                        descripcion = "Casual urbano con diseño moderno",
                        precioUnitario = 45990,
                        imagenUrl = "urban",
                        estado = "activo"
                    ),
                    ModeloZapatoEntity(
                        idModelo = 0,
                        idMarca = appMarcaId,
                        nombreModelo = "StepStyle Kids",
                        descripcion = "Zapatillas para niños",
                        precioUnitario = 29990,
                        imagenUrl = "kids",
                        estado = "activo"
                    )
                )

                modelosApp.forEach { modelo ->
                    try {
                        val existeModelo = modeloDao.existeModeloEnMarca(appMarcaId, modelo.nombreModelo)
                        if (existeModelo == 0) {
                            modeloDao.insertModelo(modelo)
                            Log.d("AppDatabase", "Preload: inserted modelo '${modelo.nombreModelo}' for marcaId=$appMarcaId")
                        }
                    } catch (_: Exception) {
                        // Ignorar errores de inserción duplicada
                    }
                }
                Log.d("AppDatabase", "Preload: marca '$appBrandName' id=$appMarcaId, modelos intentados=${modelosApp.size}")

                // Precargar tallas e inventario para los modelos de StepStyle
                val tallasIniciales = listOf("38", "39", "40", "41", "42", "43")
                val idsTallas = mutableListOf<Int>()
                tallasIniciales.forEach { numero ->
                    val existe = tallaDao.getByNumero(numero)
                    val id = if (existe == null) {
                        tallaDao.insert(TallaEntity(idTalla = 0, numeroTalla = numero)).toInt()
                    } else {
                        existe.idTalla
                    }
                    idsTallas.add(id)
                    Log.d("AppDatabase", "Preload: talla '$numero' id=$id")
                }

                // Obtener modelos creados para la marca y crear inventario
                val modelosCreados = modeloDao.getModelosByMarca(appMarcaId).first()
                modelosCreados.forEach { modeloCreado ->
                    idsTallas.forEachIndexed { idx, idTalla ->
                        try {
                            val existeInv = inventarioDao.getByModeloYTalla(modeloCreado.idModelo, idTalla)
                            if (existeInv == null) {
                                // Asignar stock inicial variable por talla
                                val stockInicial = when (idx) {
                                    0 -> 5
                                    1 -> 4
                                    2 -> 6
                                    3 -> 3
                                    4 -> 2
                                    else -> 1
                                }
                                val invId = inventarioDao.insert(
                                    InventarioEntity(
                                        idInventario = 0,
                                        idModelo = modeloCreado.idModelo,
                                        idTalla = idTalla,
                                        stockActual = stockInicial
                                    )
                                )
                                Log.d("AppDatabase", "Preload: inventario creado id=$invId modelo=${modeloCreado.nombreModelo} tallaId=$idTalla stock=$stockInicial")
                            }
                        } catch (_: Exception) {
                            // Ignorar errores y continuar
                        }
                    }
                }

                Log.d("AppDatabase", "preloadData completado exitosamente")
                Log.d("AppDatabase", "Precarga marcada como completa")
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error en preloadData: ${e.message}", e)
                // Asegurarse de que la señal se marque aunque algo falle
                Log.d("AppDatabase", "Precarga marcada como completa")
            } finally {
                _preloadComplete.value = true
            }
        }
    }
}
