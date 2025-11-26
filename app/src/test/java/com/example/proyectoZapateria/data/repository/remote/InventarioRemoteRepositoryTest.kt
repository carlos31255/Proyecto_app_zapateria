package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class InventarioRemoteRepositoryTest {

    private val inventarioApi = mockk<InventarioApiService>()
    private val productoApi = mockk<ProductoApiService>()
    private val repository = InventarioRemoteRepository(inventarioApi, productoApi)

    @Test
    fun getMarcas_retorna_lista_valida() = runBlocking {
        // Mock de datos
        val sample = listOf(
            MarcaDTO(1L, "Nike", null),
            MarcaDTO(2L, "Adidas", null)
        )

        // Comportamiento exitoso (200 OK)
        coEvery { productoApi.obtenerTodasLasMarcas() } returns Response.success(sample)

        val result = repository.getMarcas()

        // Validaciones
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("Nike", result.getOrNull()!![0].nombre)
    }

    @Test
    fun getModeloById_retorna_objeto_valido() = runBlocking {
        val sample = ProductoDTO(
            id = 10L,
            nombre = "Zapatilla Test",
            marcaId = 1L,
            descripcion = "Descripción test",
            precioUnitario = 99,
            imagenUrl = "img.jpg"
        )

        coEvery { productoApi.obtenerProductoPorId(10L) } returns Response.success(sample)

        val result = repository.getModeloById(10L)

        assertTrue(result.isSuccess)
        assertEquals("Zapatilla Test", result.getOrNull()!!.nombre)
    }

    @Test
    fun crearModelo_retorna_exito() = runBlocking {
        val input = ProductoDTO(
            id = null,
            nombre = "Nuevo",
            marcaId = 1L,
            descripcion = null,
            precioUnitario = 50,
            imagenUrl = null
        )
        val output = ProductoDTO(
            id = 50L,
            nombre = "Nuevo",
            marcaId = 1L,
            descripcion = null,
            precioUnitario = 50,
            imagenUrl = null
        )

        coEvery { productoApi.crearProducto(input) } returns Response.success(output)

        val result = repository.crearModelo(input)

        assertTrue(result.isSuccess)
        assertEquals(50L, result.getOrNull()!!.id)
    }

    @Test
    fun getInventarioPorModeloLogged_retorna_lista_en_primer_intento() = runBlocking {
        // Probamos el caso ideal donde la primera ruta funciona
        val sample = listOf(
            InventarioDTO(
                id = 1L,
                productoId = 100L,
                nombre = "StepStyle Classic",
                talla = "38",
                cantidad = 10,
                stockMinimo = 1,
                modeloId = null,
                tallaId = null
            )
        )

        coEvery { inventarioApi.obtenerInventarioPorModelo(100L) } returns Response.success(sample)

        val result = repository.getInventarioPorModeloLogged(100L)

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!![0].cantidad)
    }

    @Test
    fun getTallasPorProducto_retorna_lista_especifica_correctamente() = runBlocking {
        // Probamos el caso ideal donde existe una lista específica para el producto
        val sample = listOf(
            TallaDTO(1L, "40", null),
            TallaDTO(2L, "41", null)
        )

        coEvery { productoApi.obtenerTallasPorProducto(55L) } returns Response.success(sample)

        val result = repository.getTallasPorProducto(55L)

        assertTrue(result.isSuccess)
        // `TallaDTO` expone el campo `valor` que contiene el número de la talla
        assertEquals("40", result.getOrNull()!![0].valor)
    }

    @Test
    fun obtenerImagenProducto_retorna_bytes_validos() = runBlocking {
        // Simulamos un array de bytes (imagen)
        val dummyBytes = byteArrayOf(1, 2, 3)
        val responseBody = dummyBytes.toResponseBody()

        coEvery { productoApi.obtenerImagenProducto(10L) } returns Response.success(responseBody)

        val result = repository.obtenerImagenProducto(10L)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun eliminarModelo_retorna_exito_204() = runBlocking {
        // Caso especial: Eliminación exitosa suele devolver 204 No Content
        coEvery { productoApi.eliminarProducto(99L) } returns Response.success<Void>(204, null)

        val result = repository.eliminarModelo(99L)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }
}
