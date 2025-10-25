package com.example.proyectoZapateria.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.cliente.ClienteDao
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
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
import com.example.proyectoZapateria.data.local.persona.PersonaDao
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.region.RegionDao
import com.example.proyectoZapateria.data.local.region.RegionEntity
import com.example.proyectoZapateria.data.local.rol.RolDao
import com.example.proyectoZapateria.data.local.rol.RolEntity
import com.example.proyectoZapateria.data.local.talla.TallaDao
import com.example.proyectoZapateria.data.local.talla.TallaEntity
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoDao
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioDao
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import com.example.proyectoZapateria.utils.PasswordHasher
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
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // === Catálogos base ===
    // Daos que rara vez cambian pero son necesarios
    abstract fun regionDao(): RegionDao
    abstract fun comunaDao(): ComunaDao
    abstract fun rolDao(): RolDao
    abstract fun tallaDao(): TallaDao
    abstract fun tipoMovimientoDao(): TipoMovimientoDao

    // === Entidades principales ===
    abstract fun personaDao(): PersonaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun usuarioDao(): UsuarioDao
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
            val rolDao = database.rolDao()
            val personaDao = database.personaDao()
            val usuarioDao = database.usuarioDao()
            val marcaDao = database.marcaDao()
            val transportistaDao = database.transportistaDao()
            val clienteDao = database.clienteDao()
            val boletaVentaDao = database.boletaVentaDao()
            val entregaDao = database.entregaDao()

            // Roles predefinidos para una zapatería pequeña
            // Usamos IDs fijos (1, 2, 3, 4) para garantizar consistencia
            val rolesIniciales = listOf(
                RolEntity(
                    idRol = 1,
                    nombreRol = "Administrador",
                    descripcion = "Encargado principal: acceso total al sistema, gestión de usuarios, inventario y configuración"
                ),
                RolEntity(
                    idRol = 2,
                    nombreRol = "Vendedor",
                    descripcion = "Personal de ventas: gestión de ventas, clientes y boletas de venta"
                ),
                RolEntity(
                    idRol = 3,
                    nombreRol = "Transportista",
                    descripcion = "Personal de entregas: gestión de despachos y seguimiento de pedidos"
                ),
                RolEntity(
                    idRol = 4,
                    nombreRol = "Cliente",
                    descripcion = "Usuario cliente: puede ver catálogo, realizar pedidos y gestionar su perfil"
                )
            )

            // Insertar roles con IDs específicos
            rolesIniciales.forEach { rol ->
                rolDao.insert(rol)
            }

            // Personas iniciales para cada rol
            val personasIniciales = listOf(
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Admin",
                    apellido = "Sistema",
                    rut = "11111111-1",
                    telefono = "+56911111111",
                    email = "admin@zapateria.cl",
                    idComuna = null,
                    calle = null,
                    numeroPuerta = null,
                    username = "admin@zapateria.cl",
                    passHash = PasswordHasher.hashPassword("admin123!"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Carlos",
                    apellido = "Vendedor",
                    rut = "22222222-2",
                    telefono = "+56922222222",
                    email = "vend@zapa.cl",
                    idComuna = null,
                    calle = null,
                    numeroPuerta = null,
                    username = "vend@zapa.cl",
                    passHash = PasswordHasher.hashPassword("vend123!"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Juan",
                    apellido = "Transportista",
                    rut = "33333333-3",
                    telefono = "+56933333333",
                    email = "tra@zapa.cl",
                    idComuna = null,
                    calle = null,
                    numeroPuerta = null,
                    username = "tra@zapa.cl",
                    passHash = PasswordHasher.hashPassword("tra123!"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "María",
                    apellido = "González",
                    rut = "44444444-4",
                    telefono = "+56944444444",
                    email = "cli@zapa.cl",
                    idComuna = null,
                    calle = "Av. Libertador Bernardo O'Higgins",
                    numeroPuerta = "1234",
                    username = "cli@zapa.cl",
                    passHash = PasswordHasher.hashPassword("cli123!"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                )
            )

            val idsPersonas = mutableListOf<Long>()
            personasIniciales.forEach { persona ->
                val id = personaDao.insert(persona)
                idsPersonas.add(id)
            }

            // Usuarios asociados a cada rol
            val usuariosIniciales = listOf(
                UsuarioEntity(
                    idPersona = idsPersonas[0].toInt(),
                    idRol = 1 // Administrador
                ),
                UsuarioEntity(
                    idPersona = idsPersonas[1].toInt(),
                    idRol = 2 // Vendedor
                ),
                UsuarioEntity(
                    idPersona = idsPersonas[2].toInt(),
                    idRol = 3 // Transportista
                ),
                UsuarioEntity(
                    idPersona = idsPersonas[3].toInt(),
                    idRol = 4 // Cliente
                )
            )

            usuariosIniciales.forEach { usuario ->
                usuarioDao.insert(usuario)
            }

            // Crear transportista para el usuario transportista
            val transportistaEntity = TransportistaEntity(
                idPersona = idsPersonas[2].toInt(), // Juan Transportista
                licencia = "A12345678",
                vehiculo = "ABCD12"
            )
            transportistaDao.insert(transportistaEntity)

            // Crear cliente para el usuario cliente
            val clienteEntity = ClienteEntity(
                idPersona = idsPersonas[3].toInt(), // María González
                categoria = "regular"
            )
            clienteDao.insert(clienteEntity)

            // Crear boletas de venta de prueba
            val boleta1Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 59990,
                    fecha = System.currentTimeMillis()
                )
            )

            val boleta2Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 79990,
                    fecha = System.currentTimeMillis()
                )
            )

            val boleta3Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 45990,
                    fecha = System.currentTimeMillis()
                )
            )

            // Crear entregas de prueba asignadas al transportista
            entregaDao.insert(
                EntregaEntity(
                    idEntrega = 0,
                    idBoleta = boleta1Id.toInt(),
                    idTransportista = idsPersonas[2].toInt(),
                    estadoEntrega = "pendiente",
                    fechaAsignacion = System.currentTimeMillis(),
                    fechaEntrega = null,
                    observacion = "Entregar en horario de oficina"
                )
            )

            entregaDao.insert(
                EntregaEntity(
                    idEntrega = 0,
                    idBoleta = boleta2Id.toInt(),
                    idTransportista = idsPersonas[2].toInt(),
                    estadoEntrega = "pendiente",
                    fechaAsignacion = System.currentTimeMillis(),
                    fechaEntrega = null,
                    observacion = "Tocar el timbre"
                )
            )

            entregaDao.insert(
                EntregaEntity(
                    idEntrega = 0,
                    idBoleta = boleta3Id.toInt(),
                    idTransportista = idsPersonas[2].toInt(),
                    estadoEntrega = "completada",
                    fechaAsignacion = System.currentTimeMillis() - 86400000, // Hace 1 día
                    fechaEntrega = System.currentTimeMillis() - 43200000, // Hace 12 horas
                    observacion = "Entrega exitosa"
                )
            )

            // Marcas predefinidas para productos
            val marcasIniciales = listOf(
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Nike",
                    descripcion = "Marca deportiva líder mundial",
                    estado = "activa"
                ),
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Adidas",
                    descripcion = "Calzado deportivo de alta calidad",
                    estado = "activa"
                ),
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Puma",
                    descripcion = "Estilo y rendimiento deportivo",
                    estado = "activa"
                ),
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Reebok",
                    descripcion = "Innovación en calzado deportivo",
                    estado = "activa"
                ),
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Converse",
                    descripcion = "Estilo clásico y casual",
                    estado = "activa"
                ),
                MarcaEntity(
                    idMarca = 0,
                    nombreMarca = "Vans",
                    descripcion = "Cultura urbana y skateboarding",
                    estado = "activa"
                )
            )

            // Insertar marcas solo si no existen (evitar error UNIQUE constraint)
            marcasIniciales.forEach { marca ->
                try {
                    val existe = marcaDao.existeMarcaConNombre(marca.nombreMarca)
                    if (existe == 0) {
                        marcaDao.insertMarca(marca)
                    }
                } catch (e: Exception) {
                    // Si falla por duplicado, ignoramos y continuamos
                }
            }
        }
    }
}
