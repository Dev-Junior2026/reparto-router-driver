package com.luispacheco.repartorouter.driver.ui.mapa

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.luispacheco.repartorouter.driver.domain.model.EstadoParada
import com.luispacheco.repartorouter.driver.domain.model.Ruta
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleUiState
import com.luispacheco.repartorouter.driver.ui.detalle.DetalleViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.unit.dp

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

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val paradasConCoords = ruta.paradasOrdenadas.filter {
                    it.latitud != 0.0 || it.longitud != 0.0
                }

                paradasConCoords.forEach { parada ->
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
                    marker.icon = crearIconoColor(context, colorMarcador)

                    overlays.add(marker)
                }

                if (paradasConCoords.isNotEmpty()) {
                    controller.setZoom(12.0)
                    controller.setCenter(
                        GeoPoint(paradasConCoords.first().latitud, paradasConCoords.first().longitud)
                    )
                }
            }
        }
    )
}

/** Genera un icono de chincheta circular de color sólido, sin necesitar recursos gráficos externos. */
private fun crearIconoColor(context: android.content.Context, color: Int): android.graphics.drawable.Drawable {
    val tamano = 48
    val bitmap = android.graphics.Bitmap.createBitmap(tamano, tamano, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(tamano / 2f, tamano / 2f, tamano / 2f - 2, paint)
    paint.color = Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawCircle(tamano / 2f, tamano / 2f, tamano / 2f - 2, paint)
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}