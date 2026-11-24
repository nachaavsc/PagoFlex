package com.example.pagoflex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND clave = :clave LIMIT 1")
    suspend fun autenticar(correo: String, clave: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre ASC")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("UPDATE usuarios SET clave = :nuevaClave WHERE id = :id")
    suspend fun actualizarClave(id: Int, nuevaClave: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    @Query("DELETE FROM usuarios WHERE correo = :correo AND id != :id")
    suspend fun eliminarDuplicadoPorCorreo(correo: String, id: Int)

    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)
}
