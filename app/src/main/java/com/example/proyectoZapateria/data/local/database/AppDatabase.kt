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
import kotlinx.coroutines.flow.first

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
        EntregaEntity::class,

        // Carrito
        CartItemEntity::class
    ],
    version = 12,
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

    // === Carrito ===
    abstract fun cartDao(): CartDao

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
                ),
                // Clientes adicionales para pruebas
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Pedro",
                    apellido = "Ramírez",
                    rut = "15678432-1",
                    telefono = "+56912345678",
                    email = "pedro.ramirez@email.cl",
                    idComuna = null,
                    calle = "Calle Los Aromos",
                    numeroPuerta = "567",
                    username = "pedro.ramirez@email.cl",
                    passHash = PasswordHasher.hashPassword("pedro123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Ana",
                    apellido = "Martínez",
                    rut = "18234567-8",
                    telefono = "+56987654321",
                    email = "ana.martinez@email.cl",
                    idComuna = null,
                    calle = "Pasaje Las Flores",
                    numeroPuerta = "123",
                    username = "ana.martinez@email.cl",
                    passHash = PasswordHasher.hashPassword("ana123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Luis",
                    apellido = "Fernández",
                    rut = "19876543-2",
                    telefono = "+56945678901",
                    email = "luis.fernandez@email.cl",
                    idComuna = null,
                    calle = "Av. Italia",
                    numeroPuerta = "890",
                    username = "luis.fernandez@email.cl",
                    passHash = PasswordHasher.hashPassword("luis123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Carmen",
                    apellido = "López",
                    rut = "17345678-9",
                    telefono = "+56956781234",
                    email = "carmen.lopez@email.cl",
                    idComuna = null,
                    calle = "Calle Errázuriz",
                    numeroPuerta = "456",
                    username = "carmen.lopez@email.cl",
                    passHash = PasswordHasher.hashPassword("carmen123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Roberto",
                    apellido = "Silva",
                    rut = "16543210-7",
                    telefono = "+56923456789",
                    email = "roberto.silva@email.cl",
                    idComuna = null,
                    calle = "Av. Providencia",
                    numeroPuerta = "2345",
                    username = "roberto.silva@email.cl",
                    passHash = PasswordHasher.hashPassword("roberto123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Patricia",
                    apellido = "Rojas",
                    rut = "20123456-5",
                    telefono = "+56934567890",
                    email = "patricia.rojas@email.cl",
                    idComuna = null,
                    calle = "Calle San Martín",
                    numeroPuerta = "678",
                    username = "patricia.rojas@email.cl",
                    passHash = PasswordHasher.hashPassword("patricia123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Diego",
                    apellido = "Morales",
                    rut = "19234567-3",
                    telefono = "+56945678012",
                    email = "diego.morales@email.cl",
                    idComuna = null,
                    calle = "Av. Vicuña Mackenna",
                    numeroPuerta = "1567",
                    username = "diego.morales@email.cl",
                    passHash = PasswordHasher.hashPassword("diego123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                ),
                PersonaEntity(
                    idPersona = 0,
                    nombre = "Sofía",
                    apellido = "Vargas",
                    rut = "18765432-0",
                    telefono = "+56956789012",
                    email = "sofia.vargas@email.cl",
                    idComuna = null,
                    calle = "Calle Huérfanos",
                    numeroPuerta = "890",
                    username = "sofia.vargas@email.cl",
                    passHash = PasswordHasher.hashPassword("sofia123"),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                )
            )

            val idsPersonas = mutableListOf<Long>()
            personasIniciales.forEach { persona ->
                val id = personaDao.insert(persona)
                idsPersonas.add(id)
                Log.d("AppDatabase", "Preload: inserted persona id=$id username=${persona.username}")
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

            // Crear cliente para el usuario cliente principal (idPersona[3])
            val clienteEntity = ClienteEntity(
                idPersona = idsPersonas[3].toInt(), // María González
                categoria = "VIP"
            )
            clienteDao.insert(clienteEntity)

            // Crear clientes adicionales (índices 4-11 de idsPersonas)
            val clientesAdicionales = listOf(
                ClienteEntity(idPersona = idsPersonas[4].toInt(), categoria = "regular"), // Pedro
                ClienteEntity(idPersona = idsPersonas[5].toInt(), categoria = "VIP"),     // Ana
                ClienteEntity(idPersona = idsPersonas[6].toInt(), categoria = "regular"), // Luis
                ClienteEntity(idPersona = idsPersonas[7].toInt(), categoria = "premium"), // Carmen
                ClienteEntity(idPersona = idsPersonas[8].toInt(), categoria = "regular"), // Roberto
                ClienteEntity(idPersona = idsPersonas[9].toInt(), categoria = "VIP"),     // Patricia
                ClienteEntity(idPersona = idsPersonas[10].toInt(), categoria = "premium"),// Diego
                ClienteEntity(idPersona = idsPersonas[11].toInt(), categoria = "regular") // Sofía
            )

            clientesAdicionales.forEach { cliente ->
                clienteDao.insert(cliente)
                Log.d("AppDatabase", "Preload: inserted cliente idPersona=${cliente.idPersona} categoria=${cliente.categoria}")
            }

            // Crear boletas de venta de prueba
            val boleta1Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000001",
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 59990,
                    fecha = System.currentTimeMillis()
                )
            )

            val boleta2Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000002",
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 79990,
                    fecha = System.currentTimeMillis()
                )
            )

            val boleta3Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000003",
                    idCliente = idsPersonas[3].toInt(),
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 45990,
                    fecha = System.currentTimeMillis()
                )
            )

            // Boletas para otros clientes
            val boleta4Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000004",
                    idCliente = idsPersonas[4].toInt(), // Pedro
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 89990,
                    fecha = System.currentTimeMillis() - 172800000 // Hace 2 días
                )
            )

            val boleta5Id = boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000005",
                    idCliente = idsPersonas[5].toInt(), // Ana
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 129990,
                    fecha = System.currentTimeMillis() - 259200000 // Hace 3 días
                )
            )

            boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000006",
                    idCliente = idsPersonas[5].toInt(), // Ana - segundo pedido
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 65990,
                    fecha = System.currentTimeMillis() - 86400000 // Hace 1 día
                )
            )

            boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000007",
                    idCliente = idsPersonas[7].toInt(), // Carmen
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 199990,
                    fecha = System.currentTimeMillis() - 432000000 // Hace 5 días
                )
            )

            boletaVentaDao.insert(
                BoletaVentaEntity(
                    idBoleta = 0,
                    numeroBoleta = "B-000008",
                    idCliente = idsPersonas[9].toInt(), // Patricia
                    idVendedor = idsPersonas[1].toInt(),
                    montoTotal = 149990,
                    fecha = System.currentTimeMillis() - 604800000 // Hace 7 días
                )
            )

            Log.d("AppDatabase", "Preload: created ${8} boletas de venta for various clients")

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

            // Marca propia de la aplicación (precarga)
            try {
                val appBrandName = "StepStyle"
                var appMarca = marcaDao.getMarcaByNombre(appBrandName)
                val appMarcaId = if (appMarca == null) {
                    // Insertar y obtener id
                    val id = marcaDao.insertMarca(MarcaEntity(idMarca = 0, nombreMarca = appBrandName, descripcion = "Marca propia StepStyle", estado = "activa"))
                    id.toInt()
                } else {
                    appMarca.idMarca
                }

                // Precargar modelos para la marca propia
                val modeloDao = database.modeloZapatoDao()
                val modelosApp = listOf(
                    ModeloZapatoEntity(idModelo = 0, idMarca = appMarcaId, nombreModelo = "StepStyle Classic", descripcion = "Zapatillas clásicas cómodas", precioUnitario = 39990, imagenUrl = null, estado = "activo"),
                    ModeloZapatoEntity(idModelo = 0, idMarca = appMarcaId, nombreModelo = "StepStyle Runner", descripcion = "Runner ligero para entrenamiento", precioUnitario = 49990, imagenUrl = null, estado = "activo"),
                    ModeloZapatoEntity(idModelo = 0, idMarca = appMarcaId, nombreModelo = "StepStyle Urban", descripcion = "Casual urbano con diseño moderno", precioUnitario = 45990, imagenUrl = null, estado = "activo"),
                    ModeloZapatoEntity(idModelo = 0, idMarca = appMarcaId, nombreModelo = "StepStyle Kids", descripcion = "Zapatillas para niños", precioUnitario = 29990, imagenUrl = null, estado = "activo")
                )

                modelosApp.forEach { modelo ->
                    try {
                        val existeModelo = modeloDao.existeModeloEnMarca(appMarcaId, modelo.nombreModelo)
                        if (existeModelo == 0) {
                            modeloDao.insertModelo(modelo)
                            Log.d("AppDatabase", "Preload: inserted modelo '${modelo.nombreModelo}' for marcaId=$appMarcaId")
                        }
                    } catch (_: Exception) {
                        // Ignorar errores de inserción duplicada y continuar
                    }
                }
                Log.d("AppDatabase", "Preload: marca '$appBrandName' id=$appMarcaId, modelos intentados=${modelosApp.size}")

                // --- Precargar tallas e inventario para los modelos de StepStyle ---
                try {
                    val tallaDao = database.tallaDao()
                    val inventarioDao = database.inventarioDao()

                    val tallasIniciales = listOf("38", "39", "40", "41", "42", "43")
                    val idsTallas = mutableListOf<Int>()
                    tallasIniciales.forEach { numero ->
                        val existe = tallaDao.getByNumero(numero)
                        val id = if (existe == null) {
                            tallaDao.insert(com.example.proyectoZapateria.data.local.talla.TallaEntity(idTalla = 0, numeroTalla = numero)).toInt()
                        } else {
                            existe.idTalla
                        }
                        idsTallas.add(id)
                        Log.d("AppDatabase", "Preload: talla '$numero' id=$id")
                    }

                    // Obtener modelos creados para la marca
                    val modelosCreados = modeloDao.getModelosByMarca(appMarcaId).first()
                    modelosCreados.forEach { modeloCreado ->
                        idsTallas.forEachIndexed { idx, idTalla ->
                            try {
                                val existeInv = inventarioDao.getByModeloYTalla(modeloCreado.idModelo, idTalla)
                                if (existeInv == null) {
                                    // Asignar stock inicial variable por talla (ejemplo)
                                    val stockInicial = when (idx) {
                                        0 -> 5
                                        1 -> 4
                                        2 -> 6
                                        3 -> 3
                                        4 -> 2
                                        else -> 1
                                    }
                                    val invId = inventarioDao.insert(
                                        com.example.proyectoZapateria.data.local.inventario.InventarioEntity(
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
                } catch (_: Exception) {
                    // Si algo falla no interrumpir la precarga
                }
            } catch (e: Exception) {
                // Si algo falla en la precarga de la marca propia, no detener la creación de la DB
            }

            // Agregar detalles de boleta (productos en los pedidos)
            try {
                val detalleBoletaDao = database.detalleBoletaDao()

                // Obtener algunos IDs de inventario para asignar productos a las boletas
                // Nota: Esto es una simplificación. En producción, deberías obtener IDs reales del inventario.
                // Por ahora, asumiremos que existen inventarios con IDs del 1 al 20

                // Boleta 1 (María) - 2 productos
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta1Id.toInt(),
                        idInventario = 1,
                        cantidad = 1,
                        precioUnitario = 39990,
                        subtotal = 39990
                    )
                )
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta1Id.toInt(),
                        idInventario = 2,
                        cantidad = 1,
                        precioUnitario = 20000,
                        subtotal = 20000
                    )
                )

                // Boleta 2 (María) - 1 producto
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta2Id.toInt(),
                        idInventario = 3,
                        cantidad = 2,
                        precioUnitario = 39995,
                        subtotal = 79990
                    )
                )

                // Boleta 3 (María) - 1 producto
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta3Id.toInt(),
                        idInventario = 4,
                        cantidad = 1,
                        precioUnitario = 45990,
                        subtotal = 45990
                    )
                )

                // Boleta 4 (Pedro) - 2 productos
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta4Id.toInt(),
                        idInventario = 5,
                        cantidad = 1,
                        precioUnitario = 49990,
                        subtotal = 49990
                    )
                )
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta4Id.toInt(),
                        idInventario = 6,
                        cantidad = 1,
                        precioUnitario = 40000,
                        subtotal = 40000
                    )
                )

                // Boleta 5 (Ana) - 3 productos
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta5Id.toInt(),
                        idInventario = 7,
                        cantidad = 2,
                        precioUnitario = 44995,
                        subtotal = 89990
                    )
                )
                detalleBoletaDao.insert(
                    com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity(
                        idDetalle = 0,
                        idBoleta = boleta5Id.toInt(),
                        idInventario = 8,
                        cantidad = 1,
                        precioUnitario = 40000,
                        subtotal = 40000
                    )
                )

                Log.d("AppDatabase", "Preload: created DetalleBoletaEntity for testing")
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error creating DetalleBoletaEntity: ${e.message}")
            }
        }
    }
}
