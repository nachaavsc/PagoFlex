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

    @Query("SELECT * FROM compromisos WHERE estado = :estado ORDER BY fechaVencimiento ASC")
    fun observarPorEstado(estado: String): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE estado != 'PAGADO' ORDER BY fechaVencimiento ASC")
    fun observarNoPagados(): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE usuarioId = :usuarioId ORDER BY fechaVencimiento ASC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<CompromisoEntity>>

    @Query(
        """
        SELECT * FROM compromisos
        WHERE usuarioId = :usuarioId AND estado != 'PAGADO'
        ORDER BY fechaVencimiento ASC
        """
    )
    fun observarNoPagadosPorUsuario(usuarioId: Int): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): CompromisoEntity?

    @Query("SELECT * FROM compromisos WHERE folio = :folio LIMIT 1")
    suspend fun obtenerPorFolio(folio: String): CompromisoEntity?

    @Query("SELECT COUNT(*) FROM compromisos")
    suspend fun contar(): Int

    @Query("UPDATE compromisos SET estado = 'PAGADO' WHERE id = :id")
    suspend fun marcarComoPagado(id: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(compromiso: CompromisoEntity): Long
}
