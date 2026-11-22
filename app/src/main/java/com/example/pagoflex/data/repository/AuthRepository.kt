package com.example.pagoflex.data.repository

import com.example.pagoflex.data.remote.RetrofitClient
import com.example.pagoflex.model.LoginResponse

class AuthRepository {

    suspend fun login(correo: String, clave: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.api.login(mapOf("correo" to correo, "clave" to clave))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body() as? Map<*, *>
                if (body != null) {
                    val loginResponse = LoginResponse(
                        id = (body["id"] as? Double)?.toLong() ?: 0L,
                        usuarioId = (body["usuarioId"] as? Double)?.toLong() ?: 0L,
                        correo = body["correo"] as? String ?: "",
                        rol = body["rol"] as? String ?: "CLIENTE"
                    )
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun registro(nombre: String, correo: String, clave: String, telefono: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.api.registro(
                mapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "clave" to clave,
                    "telefono" to telefono
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body() as? Map<*, *>
                if (body != null) {
                    val loginResponse = LoginResponse(
                        id = (body["id"] as? Double)?.toLong() ?: 0L,
                        usuarioId = (body["usuarioId"] as? Double)?.toLong() ?: 0L,
                        correo = body["correo"] as? String ?: "",
                        rol = body["rol"] as? String ?: "CLIENTE"
                    )
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("El correo ya está registrado o hubo un error"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}