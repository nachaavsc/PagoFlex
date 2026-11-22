package com.example.pagoflex.data.remote

import retrofit2.Response
import retrofit2.http.*

interface PagoFlexApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<Any>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Response<Any>

    @POST("api/auth/registro")
    suspend fun registro(@Body body: Map<String, String>): Response<Any>
}