package com.example.pagoflex.navegation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pagoflex.ui.screens.*
import com.example.pagoflex.viewmodel.CompromisosViewModel
import com.example.pagoflex.viewmodel.FormularioViewModel
import com.example.pagoflex.viewmodel.HistorialViewModel
import com.example.pagoflex.viewmodel.HomeViewModel
import com.example.pagoflex.viewmodel.IncidenciasViewModel

@Composable
fun AppNavigation(
    factory: ViewModelProvider.Factory,
    homeViewModel: HomeViewModel,
    formularioViewModel: FormularioViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
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

        composable("formulario") {
            FormularioScreen(
                viewModel = formularioViewModel,
                onNavegarHome = { navController.popBackStack() }
            )
        }

        composable("perfil") {
            PerfilScreen(
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