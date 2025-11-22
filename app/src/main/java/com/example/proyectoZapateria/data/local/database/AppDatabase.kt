package com.example.proyectoZapateria.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.local.cart.CartItemEntity

@Database(
    entities = [
        CartItemEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "zapateria_client.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // para limpiar las tablas de productos/inventario que ya no se usan.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                Log.d("AppDatabase", "Instancia de base de datos local (Carrito) creada")
                instance
            }
        }
    }
}
