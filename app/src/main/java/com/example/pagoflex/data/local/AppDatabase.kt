package com.example.pagoflex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransaccionEntity::class,
        CompromisoEntity::class,
        PagoEntity::class,
        IncidenciaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transaccionDao(): TransaccionDao
    abstract fun compromisoDao(): CompromisoDao
    abstract fun pagoDao(): PagoDao
    abstract fun incidenciaDao(): IncidenciaDao

    suspend fun precargarDatosIniciales() {
        if (compromisoDao().contar() > 0) return
        compromisoDao().insertarTodos(DatosIniciales.compromisos())
        pagoDao().insertarTodos(DatosIniciales.pagos())
        incidenciaDao().insertarTodas(DatosIniciales.incidencias())
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pagoflex_db_v2"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private object SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) = precargar()

            // Destructive migrations recreate the tables without calling onCreate.
            override fun onDestructiveMigration(db: SupportSQLiteDatabase) = precargar()

            private fun precargar() {
                seedScope.launch { INSTANCE?.precargarDatosIniciales() }
            }
        }
    }
}
