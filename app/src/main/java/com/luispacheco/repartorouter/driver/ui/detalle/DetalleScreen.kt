package com.luispacheco.repartorouter.driver.ui.detalle

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
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
                    DetalleContenido(
                        ruta = estado.ruta,
                        onToggleCompletada = viewModel::toggleParadaCompletada
                    )
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
private fun DetalleContenido(
    ruta: Ruta,
    onToggleCompletada: (paradaId: Long, nuevoEstado: Boolean) -> Unit
) {
    val paradasEntrega = ruta.paradasOrdenadas.filter { !it.esAlmacen }
    val completadas = paradasEntrega.count { it.completada }

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
                    Text(text = "Paradas: $completadas / ${paradasEntrega.size} completadas")
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
            ParadaCard(
                parada = parada,
                onToggleCompletada = onToggleCompletada
            )
        }
    }
}

@Composable
private fun ParadaCard(
    parada: Parada,
    onToggleCompletada: (paradaId: Long, nuevoEstado: Boolean) -> Unit
) {
    val context = LocalContext.current
    val estiloTexto = if (parada.completada) {
        TextDecoration.LineThrough
    } else {
        TextDecoration.None
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (parada.completada) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // El almacén no se marca como entrega completada, solo se navega
            if (!parada.esAlmacen) {
                Checkbox(
                    checked = parada.completada,
                    onCheckedChange = { marcada ->
                        parada.id?.let { onToggleCompletada(it, marcada) }
                    }
                )
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            Text(
                text = "${parada.numero}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parada.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = estiloTexto
                )
                Text(
                    text = parada.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = estiloTexto
                )
                Text(
                    text = "Horario: ${parada.horaApertura} - ${parada.horaCierre}",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = estiloTexto
                )
            }
            IconButton(onClick = { abrirNavegacion(context, parada) }) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Ir hasta ${parada.nombre}"
                )
            }
        }
    }
}

/**
 * Abre Google Maps en modo navegación hacia las coordenadas de la parada.
 * Si Google Maps no está instalado, cae a la app de mapas genérica del sistema (geo:).
 */
private fun abrirNavegacion(context: android.content.Context, parada: Parada) {
    val uriGoogleMaps = Uri.parse("google.navigation:q=${parada.latitud},${parada.longitud}")
    val intentGoogleMaps = Intent(Intent.ACTION_VIEW, uriGoogleMaps).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(intentGoogleMaps)
    } catch (e: ActivityNotFoundException) {
        // Google Maps no instalado: usamos el intent genérico geo:, que deja elegir app al usuario
        val uriGenerico = Uri.parse("geo:${parada.latitud},${parada.longitud}?q=${parada.latitud},${parada.longitud}(${parada.nombre})")
        val intentGenerico = Intent(Intent.ACTION_VIEW, uriGenerico)
        context.startActivity(intentGenerico)
    }
}