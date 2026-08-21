package com.luispacheco.repartorouter.driver.ui.rutas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luispacheco.repartorouter.driver.domain.model.Ruta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutasScreen(
    viewModel: RutasViewModel,
    onRutaClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis rutas") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val estado = uiState) {
                is RutasUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is RutasUiState.Exito -> {
                    if (estado.rutas.isEmpty()) {
                        Text(
                            text = "No tienes rutas asignadas por ahora",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        ListaRutas(
                            rutas = estado.rutas,
                            onRutaClick = onRutaClick
                        )
                    }
                }

                is RutasUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${estado.mensaje}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarRutas() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaRutas(
    rutas: List<Ruta>,
    onRutaClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rutas) { ruta ->
            RutaCard(ruta = ruta, onClick = { ruta.id?.let(onRutaClick) })
        }
    }
}

@Composable
private fun RutaCard(
    ruta: Ruta,
    onClick: () -> Unit
) {
    val paradasEntrega = ruta.paradasOrdenadas.filter { !it.esAlmacen }
    val completadas = paradasEntrega.count { it.completada }
    val total = paradasEntrega.size

    val (etiquetaEstado, colorEstado) = when {
        total == 0 -> "Sin paradas" to MaterialTheme.colorScheme.outline
        completadas == 0 -> "Pendiente" to MaterialTheme.colorScheme.error
        completadas == total -> "Completada" to MaterialTheme.colorScheme.primary
        else -> "En curso" to MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = ruta.nombre, style = MaterialTheme.typography.titleMedium)
                AssistChip(
                    onClick = {},
                    label = { Text(etiquetaEstado) },
                    colors = AssistChipDefaults.assistChipColors(labelColor = colorEstado)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Inicio: ${ruta.horaInicio}")
            Text(text = "Paradas: $completadas / $total completadas")
            Text(text = "Distancia: ${ruta.distanciaTotalKm} km")
        }
    }
}