package com.example.pagoflex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {

    @Query("SELECT * FROM pagos ORDER BY fechaPago DESC")
    fun observarTodos(): Flow<List<PagoEntity>>

    /** Filters by period start (epoch millis) and, when [empresa] is null, by every company. */
    @Query(
        """
        SELECT * FROM pagos
        WHERE fechaPago >= :desde AND (:empresa IS NULL OR empresa = :empresa)
        ORDER BY fechaPago DESC
        """
    )
    fun observarFiltrados(desde: Long, empresa: String?): Flow<List<PagoEntity>>

    @Query("SELECT DISTINCT empresa FROM pagos ORDER BY empresa ASC")
    fun observarEmpresas(): Flow<List<String>>

    @Query("SELECT * FROM pagos WHERE estado = :estado ORDER BY fechaPago DESC")
    suspend fun obtenerPorEstado(estado: String): List<PagoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pago: PagoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pagos: List<PagoEntity>)
}
