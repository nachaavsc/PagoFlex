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

    @Query(
        """
        SELECT pagos.* FROM pagos
        INNER JOIN compromisos ON compromisos.id = pagos.compromisoId
        WHERE compromisos.usuarioId = :usuarioId
        ORDER BY pagos.fechaPago DESC
        """
    )
    fun observarHistorialPorUsuario(usuarioId: Int): Flow<List<PagoEntity>>

    @Query(
        """
        SELECT * FROM pagos
        WHERE fechaPago >= :desde AND (:organizacion IS NULL OR organizacion = :organizacion)
        ORDER BY fechaPago DESC
        """
    )
    fun observarFiltrados(desde: Long, organizacion: String?): Flow<List<PagoEntity>>

    @Query("SELECT DISTINCT organizacion FROM pagos ORDER BY organizacion ASC")
    fun observarOrganizaciones(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(pago: PagoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pagos: List<PagoEntity>)
}
