package com.example.pagoflex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompromisoDao {

    @Query("SELECT * FROM compromisos ORDER BY fechaVencimiento ASC")
    fun observarTodos(): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE usuarioId = :usuarioId ORDER BY fechaVencimiento ASC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): CompromisoEntity?

    @Query("SELECT folio FROM compromisos")
    suspend fun obtenerFolios(): List<String>

    @Query("UPDATE compromisos SET estado = 'PAGADO' WHERE id = :id")
    suspend fun marcarComoPagado(id: Int)

    @Query("UPDATE compromisos SET estado = 'VENCIDO' WHERE estado = 'PENDIENTE' AND fechaVencimiento < :inicioDelDia")
    suspend fun marcarVencidos(inicioDelDia: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(compromiso: CompromisoEntity): Long
}
