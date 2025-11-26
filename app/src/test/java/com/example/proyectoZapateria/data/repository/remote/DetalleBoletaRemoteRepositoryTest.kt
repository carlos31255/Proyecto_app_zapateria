package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DetalleBoletaRemoteRepositoryTest {

    private val ventasRemoteRepository = mockk<VentasRemoteRepository>()
    private val inventarioRemoteRepository = mockk<InventarioRemoteRepository>()
    private val repository = DetalleBoletaRemoteRepository(ventasRemoteRepository, inventarioRemoteRepository)

    @Test
    fun getProductos_retorna_lista_mapeada_correctamente() = runBlocking {
        // Mock de datos
        val idBoleta = 1L
        val sample = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Nike Air Max",
                talla = "42",
                cantidad = 2,
                precioUnitario = 50000,
                subtotal = 100000
            ),
            DetalleBoletaDTO(
                id = 2L,
                boletaId = idBoleta,
                inventarioId = 11L,
                nombreProducto = "Adidas Ultraboost",
                talla = "43",
                cantidad = 1,
                precioUnitario = 80000,
                subtotal = 80000
            )
        )

        // Comportamiento exitoso: ventas devuelve la lista de detalles
        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(sample)

        // No necesitamos que inventario devuelva nada para este caso (se usan valores directos del detalle)

        val result = repository.getProductos(idBoleta).first()

        // Validaciones
        assertEquals(2, result.size)

        // Verificación de mapeo del primer elemento: producto.nombre proviene de nombreProducto
        assertEquals("Nike Air Max", result[0].producto?.nombre)
        assertEquals("42", result[0].talla)
        assertEquals(2, result[0].cantidad)
        assertEquals("Desconocida", result[0].marcaName) // Marca por defecto si no se resuelve

        // Verificación del segundo elemento
        assertEquals("Adidas Ultraboost", result[1].producto?.nombre)
        assertEquals("43", result[1].talla)
    }

    @Test
    fun getProductos_maneja_campos_nulos_correctamente() = runBlocking {
        // Mock de datos con nulos (caso borde válido)
        val idBoleta = 5L
        val sample = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = null,
                talla = null,
                cantidad = 1,
                precioUnitario = 50000,
                subtotal = 50000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(sample)

        val result = repository.getProductos(idBoleta).first()

        // Validaciones de robustez (null -> '-' según implementación)
        assertEquals(1, result.size)
        assertEquals("-", result[0].producto?.nombre) // nombreProducto null se normaliza a "-"
        assertEquals("-", result[0].talla) // talla null se normaliza a "-"
    }

    @Test
    fun getProductosPorNumeroBoleta_retorna_lista_vacia_por_defecto() = runBlocking {
        // Comportamiento esperado actual (sin implementación remota aún)
        val numeroBoleta = "BOL-2024-001"

        val result = repository.getProductosPorNumeroBoleta(numeroBoleta).first()

        // Validaciones
        assertTrue(result.isEmpty())
    }
}
