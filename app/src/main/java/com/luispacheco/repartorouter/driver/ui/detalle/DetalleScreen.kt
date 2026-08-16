package com.luispacheco.repartorouter.driver.ui.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luispacheco.repartorouter.driver.domain.model.Parada
import com.luispacheco.repartorouter.driver.domain.model.Ruta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de ruta") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val estado = uiState) {
                is DetalleUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DetalleUiState.Exito -> {
                    DetalleContenido(ruta = estado.ruta)
                }

                is DetalleUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${estado.mensaje}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarRuta() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleContenido(ruta: Ruta) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = ruta.nombre, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Inicio: ${ruta.horaInicio}")
                    ruta.horaFinEstimada?.let {
                        Text(text = "Fin estimado: $it")
                    }
                    Text(text = "Distancia total: ${ruta.distanciaTotalKm} km")
                    Text(text = "Paradas: ${ruta.paradasOrdenadas.size}")
                }
            }
        }

        item {
            Text(
                text = "Paradas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(ruta.paradasOrdenadas) { parada ->
            ParadaCard(parada = parada)
        }
    }
}

@Composable
private fun ParadaCard(parada: Parada) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${parada.numero}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = parada.nombre, style = MaterialTheme.typography.bodyLarge)
                Text(text = parada.direccion, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Horario: ${parada.horaApertura} - ${parada.horaCierre}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}