package com.example.pagoflex.data.remote

import com.example.pagoflex.model.LoginResponse
import retrofit2.Response
import retrofit2.http.*

interface PagoFlexApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<LoginResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Response<UsuarioRemoto>

    @POST("api/auth/registro")
    suspend fun registro(@Body body: Map<String, String>): Response<LoginResponse>

    @PUT("api/auth/cambiar-clave")
    suspend fun cambiarClave(@Body body: Map<String, String>): Response<LoginResponse>

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioRemoto>>

    @GET("api/usuarios/correo/{correo}")
    suspend fun obtenerUsuarioPorCorreo(@Path("correo") correo: String): Response<UsuarioRemoto>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body usuario: UsuarioRemoto): Response<UsuarioRemoto>

    @GET("api/compromisos")
    suspend fun listarCompromisos(): Response<List<CompromisoRemoto>>

    @GET("api/compromisos/usuario/{usuarioId}")
    suspend fun listarCompromisosPorUsuario(@Path("usuarioId") usuarioId: Long): Response<List<CompromisoRemoto>>

    @POST("api/compromisos")
    suspend fun crearCompromiso(@Body compromiso: CompromisoRemoto): Response<CompromisoRemoto>

    @POST("api/compromisos/{id}/pagos")
    suspend fun registrarPago(@Path("id") compromisoId: Long, @Body pago: PagoRemoto): Response<PagoRemoto>

    @GET("api/compromisos/{id}/pagos")
    suspend fun listarPagos(@Path("id") compromisoId: Long): Response<List<PagoRemoto>>

    @GET("api/incidencias")
    suspend fun listarIncidencias(): Response<List<IncidenciaRemota>>

    @GET("api/incidencias/usuario/{usuarioId}")
    suspend fun listarIncidenciasPorUsuario(@Path("usuarioId") usuarioId: Long): Response<List<IncidenciaRemota>>

    @POST("api/incidencias")
    suspend fun crearIncidencia(@Body incidencia: IncidenciaRemota): Response<IncidenciaRemota>

    @PUT("api/incidencias/{id}/estado")
    suspend fun actualizarEstadoIncidencia(@Path("id") id: Long, @Body body: Map<String, String>): Response<IncidenciaRemota>
}
