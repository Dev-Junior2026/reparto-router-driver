package com.luispacheco.repartorouter.driver.ui.mapa

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.luispacheco.repartorouter.driver.domain.model.EstadoParada
import com.luispacheco.repartorouter.driver.domain.model.Parada
import com.luispacheco.repartorouter.driver.domain.model.Ruta
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleUiState
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Reutiliza DetalleViewModel (mismo rutaId, mismos datos ya cargados por la app)
 * para no duplicar la llamada de red ni el estado de carga/error.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaRutaScreen(
    viewModel: DetalleViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de la ruta") },
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DetalleUiState.Exito -> {
                    MapaConMarcadores(ruta = estado.ruta)
                }

                is DetalleUiState.Error -> {
                    Text(
                        text = "Error: ${estado.mensaje}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapaConMarcadores(ruta: Ruta) {
    val context = LocalContext.current
    var paradaSeleccionada by remember { mutableStateOf<Parada?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val paradasConCoords = mutableListOf<Pair<Int, Parada>>()
                ruta.paradasOrdenadas.forEachIndexed { index, parada ->
                    if (parada.latitud != 0.0 || parada.longitud != 0.0) {
                        paradasConCoords.add((index + 1) to parada)
                    }
                }

                paradasConCoords.forEach { (numero, parada) ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(parada.latitud, parada.longitud)
                    marker.title = parada.nombre
                    marker.snippet = parada.direccion

                    val colorMarcador = when {
                        parada.esAlmacen -> Color.parseColor("#1565C0") // azul = almacén
                        parada.estado == EstadoParada.ENTREGADO -> Color.parseColor("#2E7D32") // verde
                        parada.estado == EstadoParada.RECHAZADO -> Color.parseColor("#C62828") // rojo
                        else -> Color.parseColor("#F9A825") // ámbar = pendiente
                    }
                    marker.icon = crearIconoNumerado(context, colorMarcador, numero)

                    marker.setOnMarkerClickListener { _, _ ->
                        paradaSeleccionada = parada
                        true
                    }

                    overlays.add(marker)
                }

                if (paradasConCoords.isNotEmpty()) {
                    controller.setZoom(12.0)
                    controller.setCenter(
                        GeoPoint(paradasConCoords.first().second.latitud, paradasConCoords.first().second.longitud)
                    )
                }
            }
        }
    )

    paradaSeleccionada?.let { parada ->
        FichaParadaDialog(
            parada = parada,
            onIrHastaAqui = {
                abrirNavegacion(context, parada)
                paradaSeleccionada = null
            },
            onCerrar = { paradaSeleccionada = null }
        )
    }
}

@Composable
private fun FichaParadaDialog(
    parada: Parada,
    onIrHastaAqui: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(parada.nombre) },
        text = {
            Column {
                Text(parada.direccion)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = "Horario: ${parada.horaApertura} - ${parada.horaCierre}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!parada.observaciones.isNullOrBlank()) {
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = parada.observaciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onIrHastaAqui) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text("Ir hasta aquí")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar")
            }
        }
    )
}

/** Genera un icono de chincheta circular de color sólido con el número de la parada dentro. */
private fun crearIconoNumerado(
    context: android.content.Context,
    color: Int,
    numero: Int
): android.graphics.drawable.Drawable {
    val tamano = 72
    val bitmap = android.graphics.Bitmap.createBitmap(tamano, tamano, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paintCirculo = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(tamano / 2f, tamano / 2f, tamano / 2f - 3, paintCirculo)

    val paintBorde = android.graphics.Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(tamano / 2f, tamano / 2f, tamano / 2f - 3, paintBorde)

    val paintTexto = android.graphics.Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
        textSize = 28f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textoY = tamano / 2f - (paintTexto.descent() + paintTexto.ascent()) / 2f
    canvas.drawText(numero.toString(), tamano / 2f, textoY, paintTexto)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
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
        val uriGenerico = Uri.parse("geo:${parada.latitud},${parada.longitud}?q=${parada.latitud},${parada.longitud}(${parada.nombre})")
        val intentGenerico = Intent(Intent.ACTION_VIEW, uriGenerico)
        context.startActivity(intentGenerico)
    }
}