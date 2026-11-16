package com.example.pagoflex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        TransaccionEntity::class,
        CompromisoEntity::class,
        PagoEntity::class,
        IncidenciaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun compromisoDao(): CompromisoDao
    abstract fun pagoDao(): PagoDao
    abstract fun incidenciaDao(): IncidenciaDao

    suspend fun precargarDatosIniciales() {
        if (usuarioDao().contar() > 0) return
        withTransaction {
            val usuarios = DatosIniciales.usuarios().map { it.copy(id = usuarioDao().insertar(it).toInt()) }
            val clienteId = usuarios.first { it.rol == RolUsuario.CLIENTE }.id
            val compromisos = DatosIniciales.compromisos(clienteId)
                .map { it.copy(id = compromisoDao().insertar(it).toInt()) }
            pagoDao().insertarTodos(DatosIniciales.pagos(compromisos))
            incidenciaDao().insertarTodas(DatosIniciales.incidencias())
        }
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

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) = precargar()

            private fun precargar() {
                seedScope.launch { INSTANCE?.precargarDatosIniciales() }
            }
        }
    }
}
