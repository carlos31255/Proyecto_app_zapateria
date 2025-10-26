package com.example.proyectoZapateria.data.local.entrega

data class EntregaConDetalles (
    // De EntregaEntity
    val idEntrega: Int,
    val estadoEntrega: String,
    val fechaAsignacion: Long, // Asignacion de la entrega

    val fechaEntrega: Long?, // Timestamp en milisegundos, null si no se ha completado
    val observacion: String?, // Observación del transportista


    // De BoletaVentaEntity
    val numeroBoleta: Int, // para el "numero orden"

    // De PersonaEntity
    val clienteNombre: String,
    val calle: String?,
    val numeroPuerta: String?
){
    // Funcion auxiliar para formatear la ui y manejar nulos
    fun getDireccionCompleta(): String {
        val calleVal = if (calle.isNullOrBlank()) "Calle no especificada" else calle
        val numVal = if (numeroPuerta.isNullOrBlank()) "" else " #$numeroPuerta"
        return "$calleVal$numVal"
    }

    //Funcion axuiliar para que muestre el numero de orden formateado
    fun getNumeroOrdenFormateado(): String {
        return "#ORD-${numeroBoleta}"
    }
}

data class EntregaUiState(
    val entregas: List<EntregaConDetalles> = emptyList(),
    val pendientesCount: Int = 0,
    val completadasCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)