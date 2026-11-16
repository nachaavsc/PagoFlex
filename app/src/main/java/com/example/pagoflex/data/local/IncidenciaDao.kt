package com.example.pagoflex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenciaDao {

    @Query("SELECT * FROM incidencias ORDER BY fechaReporte DESC")
    fun observarTodas(): Flow<List<IncidenciaEntity>>

    @Query("SELECT * FROM incidencias WHERE usuarioId = :usuarioId ORDER BY fechaReporte DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<IncidenciaEntity>>

    @Query("SELECT COUNT(*) FROM incidencias")
    suspend fun contar(): Int

    @Query("UPDATE incidencias SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(incidencia: IncidenciaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(incidencias: List<IncidenciaEntity>)
}
