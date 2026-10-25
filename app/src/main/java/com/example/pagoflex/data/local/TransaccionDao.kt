package com.example.pagoflex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {
    @Query("SELECT * FROM transacciones ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<TransaccionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTransaccion(transaccion: TransaccionEntity)
}