package com.example.pagoflex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.pagoflex.navegation.AppNavigation
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.FormularioViewModel
import com.example.pagoflex.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val formularioViewModel = ViewModelProvider(this, factory)[FormularioViewModel::class.java]

        setContent {
            PagoFlexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        homeViewModel = homeViewModel,
                        formularioViewModel = formularioViewModel
                    )
                }
            }
        }
    }
}