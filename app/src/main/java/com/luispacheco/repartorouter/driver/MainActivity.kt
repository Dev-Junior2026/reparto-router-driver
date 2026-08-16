package com.luispacheco.repartorouter.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luispacheco.repartorouter.driver.data.remote.RetrofitClient
import com.luispacheco.repartorouter.driver.data.repository.RutaRepositoryImpl
import com.luispacheco.repartorouter.driver.ui.rutas.RutasScreen
import com.luispacheco.repartorouter.driver.ui.rutas.RutasViewModel
import com.luispacheco.repartorouter.driver.ui.rutas.RutasViewModelFactory
import com.luispacheco.repartorouter.driver.ui.theme.RepartoRouterDriverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepartoRouterDriverTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val rutaRepository = RutaRepositoryImpl(RetrofitClient.rutaApiService)
                    val factory = RutasViewModelFactory(rutaRepository)
                    val rutasViewModel: RutasViewModel = viewModel(factory = factory)

                    RutasScreen(
                        viewModel = rutasViewModel,
                        onRutaClick = { rutaId ->
                            // Pantalla de detalle: siguiente sesión
                        }
                    )
                }
            }
        }
    }
}