package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectoZapateria.data.repository.ProductoRepository

/**
 * Factory para crear ProductoViewModel con sus dependencias inyectadas.
 */
class ProductoViewModelFactory(
    private val productoRepository: ProductoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            return ProductoViewModel(productoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

