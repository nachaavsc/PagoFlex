package com.example.pagoflex.navegation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.ui.screens.*
import com.example.pagoflex.viewmodel.CambiarClaveViewModel
import com.example.pagoflex.viewmodel.CompromisosViewModel
import com.example.pagoflex.viewmodel.FormularioViewModel
import com.example.pagoflex.viewmodel.HistorialViewModel
import com.example.pagoflex.viewmodel.HomeViewModel
import com.example.pagoflex.viewmodel.IncidenciasViewModel
import com.example.pagoflex.viewmodel.LoginViewModel
import com.example.pagoflex.viewmodel.PerfilViewModel
import com.example.pagoflex.viewmodel.RecuperarClaveViewModel
import com.example.pagoflex.viewmodel.RegistroViewModel

@Composable
fun AppNavigation(
    factory: ViewModelProvider.Factory,
    homeViewModel: HomeViewModel,
    formularioViewModel: FormularioViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel<LoginViewModel>(factory = factory),
                onIngresoExitoso = { rol -> navController.entrarComo(rol) },
                onCrearCuenta = { navController.navigate("registro") },
                onRecuperarClave = { navController.navigate("recuperarClave") }
            )
        }

        composable("registro") {
            RegistroScreen(
                viewModel = viewModel<RegistroViewModel>(factory = factory),
                onRegistroExitoso = { rol -> navController.entrarComo(rol) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("recuperarClave") {
            RecuperarClaveScreen(
                viewModel = viewModel<RecuperarClaveViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavegarFormulario = { navController.navigate("formulario") },
                onNavegarPerfil = { navController.navigate("perfil") },
                onNavegarCompromisos = { navController.navigate("compromisos") },
                onNavegarHistorial = { navController.navigate("historial") },
                onNavegarIncidencias = { navController.navigate("incidencias") }
            )
        }

        composable("gestor") {
            CompromisosScreen(
                viewModel = viewModel<CompromisosViewModel>(factory = factory),
                titulo = "Compromisos de clientes",
                textoVolver = "Perfil",
                onVolver = { navController.navigate("perfil") }
            )
        }

        composable("formulario") {
            FormularioScreen(
                viewModel = formularioViewModel,
                onNavegarHome = { navController.popBackStack() }
            )
        }

        composable("perfil") {
            PerfilScreen(
                viewModel = viewModel<PerfilViewModel>(factory = factory),
                onVolver = { navController.popBackStack() },
                onEditarPerfil = { navController.navigate("editarPerfil") },
                onCambiarClave = { navController.navigate("cambiarClave") },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable("editarPerfil") {
            EditarPerfilScreen(
                viewModel = viewModel<PerfilViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }

        composable("cambiarClave") {
            CambiarClaveScreen(
                viewModel = viewModel<CambiarClaveViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }

        composable("compromisos") {
            CompromisosScreen(
                viewModel = viewModel<CompromisosViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }

        composable("historial") {
            HistorialPagosScreen(
                viewModel = viewModel<HistorialViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }

        composable("incidencias") {
            ReporteIncidenciasScreen(
                viewModel = viewModel<IncidenciasViewModel>(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

private fun NavHostController.entrarComo(rol: String) {
    val destino = if (rol == RolUsuario.GESTOR) "gestor" else "home"
    navigate(destino) {
        popUpTo("login") { inclusive = true }
    }
}
