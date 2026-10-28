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

    @Query("SELECT * FROM compromisos WHERE empresa = :empresa ORDER BY fechaVencimiento ASC")
    fun observarPorEmpresa(empresa: String): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE estado != :estadoExcluido ORDER BY fechaVencimiento ASC")
    fun observarExcluyendoEstado(estadoExcluido: String): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE folio = :folio LIMIT 1")
    suspend fun obtenerPorFolio(folio: String): CompromisoEntity?

    @Query("SELECT COUNT(*) FROM compromisos")
    suspend fun contar(): Int

    @Query("UPDATE compromisos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(compromisos: List<CompromisoEntity>)
}
