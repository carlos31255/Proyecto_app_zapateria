package com.example.proyectoZapateria.data.repository

// TODO: Imports comentados - PersonaDao y TransportistaConPersona usan tabla persona que ahora es remota
// import com.example.proyectoZapateria.data.local.persona.PersonaDao
// import com.example.proyectoZapateria.data.local.transportista.TransportistaConPersona
import com.example.proyectoZapateria.data.local.transportista.TransportistaDao
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// TransportistaRepository actualizado para NO depender de PersonaDao (ahora Persona es remota)
class TransportistaRepository @Inject constructor(
    private val transportistaDao: TransportistaDao
    // TODO: PersonaDao comentado - ahora se usa PersonaRemoteRepository
    // private val personaDao: PersonaDao
){
    // --- FUNCIONES PARA "MI PERFIL" ---

    // TODO: getPerfilTransportista comentado - usa TransportistaConPersona que depende de tabla persona remota
    // Cuando se implemente el microservicio de transportistas, obtener datos combinados desde la API
    /*
    suspend fun getPerfilTransportista(id: Int): TransportistaConPersona? {
        return transportistaDao.getByIdConPersona(id)
    }
    */

    // Obtiene solo la entidad 'Transportista' (licencia, vehiculo)
    suspend fun getTransportistaSimple(id: Int): TransportistaEntity? {
        return transportistaDao.getById(id)
    }

    // Actualiza los datos de un transportista.
    suspend fun updateTransportista(transportista: TransportistaEntity) {
        transportistaDao.update(transportista)
    }

    // TODO: actualizarPerfilTransportista comentado - usa PersonaDao que ya no existe localmente
    // Cuando se implemente el microservicio, actualizar persona vía API y transportista localmente
    /*
    suspend fun actualizarPerfilTransportista(
        idPersona: Int,
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        licencia: String,
        vehiculo: String
    ): Boolean {
        return try {
            // Actualizar datos de persona
            val persona = personaDao.getById(idPersona)
            if (persona != null) {
                personaDao.update(
                    persona.copy(
                        nombre = nombre,
                        apellido = apellido,
                        email = email,
                        telefono = telefono
                    )
                )
            }

            // Actualizar datos de transportista
            val transportista = transportistaDao.getById(idPersona)
            if (transportista != null) {
                transportistaDao.update(
                    transportista.copy(
                        licencia = licencia,
                        vehiculo = vehiculo
                    )
                )
            }

            true
        } catch (e: Exception) {
            false
        }
    }
    */

    // --- FUNCIONES PARA ADMINISTRACIÓN ---

    // TODO: getAllTransportistasConPersona comentado - usa TransportistaConPersona que depende de tabla persona remota
    /*
    fun getAllTransportistasConPersona(): Flow<List<TransportistaConPersona>> {
        return transportistaDao.getAllConPersona()
    }
    */

    // Inserta un nuevo transportista
    suspend fun insertTransportista(transportista: TransportistaEntity): Long {
        return transportistaDao.insert(transportista)
    }

    // Elimina un transportista
    suspend fun deleteTransportista(transportista: TransportistaEntity) {
        transportistaDao.delete(transportista)
    }

    // Busca un transportista por su número de licencia.
    suspend fun getTransportistaPorLicencia(licencia: String): TransportistaEntity? {
        return transportistaDao.getByLicencia(licencia)
    }

    // Cuenta cuantos transportistas hay en total
    suspend fun getTransportistaCount(): Int {
        return transportistaDao.getCount()
    }

    // Elimina todos los transportistas de la tabla
    suspend fun deleteAllTransportistas() {
        transportistaDao.deleteAll()
    }

    // Obtiene un Flow con la lista de todas las entidades transportista simples
    fun getAllTransportistasSimples(): Flow<List<TransportistaEntity>> {
        return transportistaDao.getAll()
    }
}