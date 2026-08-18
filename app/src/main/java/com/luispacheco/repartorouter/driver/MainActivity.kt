package com.luispacheco.repartorouter.driver

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.RetrofitClient
import com.luispacheco.repartorouter.driver.data.repository.AuthRepositoryImpl
import com.luispacheco.repartorouter.driver.data.repository.RutaRepositoryImpl
import com.luispacheco.repartorouter.driver.domain.repository.AuthRepository
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleScreen
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleViewModel
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleViewModelFactory
import com.luispacheco.repartorouter.driver.ui.login.LoginScreen
import com.luispacheco.repartorouter.driver.ui.login.LoginViewModel
import com.luispacheco.repartorouter.driver.ui.login.LoginViewModelFactory
import com.luispacheco.repartorouter.driver.ui.rutas.RutasScreen
import com.luispacheco.repartorouter.driver.ui.rutas.RutasViewModel
import com.luispacheco.repartorouter.driver.ui.rutas.RutasViewModelFactory
import com.luispacheco.repartorouter.driver.ui.theme.RepartoRouterDriverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rutaIdDesdeNotificacion = intent.getStringExtra("rutaId")

        setContent {
            RepartoRouterDriverTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* concedido o no, seguimos igual */ }
                    )

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    val tokenManager = TokenManager(applicationContext)

                    val rutaRepository: RutaRepository =
                        RutaRepositoryImpl(RetrofitClient.rutaApiService)

                    val authRepository: AuthRepository =
                        AuthRepositoryImpl(RetrofitClient.authApiService, RetrofitClient.rutaApiService, tokenManager)

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            val factory = LoginViewModelFactory(authRepository)
                            val loginViewModel: LoginViewModel = viewModel(factory = factory)

                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginExitoso = {
                                    if (rutaIdDesdeNotificacion != null) {
                                        navController.navigate("detalle/$rutaIdDesdeNotificacion") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("rutas") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("rutas") {
                            val factory = RutasViewModelFactory(rutaRepository)
                            val rutasViewModel: RutasViewModel = viewModel(factory = factory)

                            RutasScreen(
                                viewModel = rutasViewModel,
                                onRutaClick = { rutaId ->
                                    navController.navigate("detalle/$rutaId")
                                }
                            )
                        }

                        composable(
                            route = "detalle/{rutaId}",
                            arguments = listOf(navArgument("rutaId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val rutaId = backStackEntry.arguments?.getLong("rutaId") ?: 0L
                            val factory = DetalleViewModelFactory(rutaRepository, rutaId)
                            val detalleViewModel: DetalleViewModel = viewModel(factory = factory)

                            DetalleScreen(
                                viewModel = detalleViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}