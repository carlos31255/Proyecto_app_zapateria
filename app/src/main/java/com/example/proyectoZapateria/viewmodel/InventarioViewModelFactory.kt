package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectoZapateria.data.repository.ProductoRepository

/**
 * Factory para crear InventarioViewModel con sus dependencias inyectadas.
 */
class InventarioViewModelFactory(
    private val productoRepository: ProductoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            return InventarioViewModel(productoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

