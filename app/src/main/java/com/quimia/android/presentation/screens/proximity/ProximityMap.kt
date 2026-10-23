package com.quimia.android.presentation.screens.proximity

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.RectF
import android.os.Bundle
import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Target
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

private const val NearbyMapStyle = "https://tiles.openfreemap.org/styles/liberty"
private const val NearbyPointSource = "quimia-nearby-points"
private const val NearbyPointLayer = "quimia-nearby-circles"
private const val NearbySelectedLayer = "quimia-nearby-selected"

@Composable
internal fun ProximityMap(points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit) {
    if (LocalInspectionMode.current) {
        ProximityMapPreview(points, selectedPointId, onPointClick)
        return
    }
    val context = LocalContext.current
    val tokens = quimiaColorTokens()
    val (location, locate) = rememberNearbyLocation()
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var locationStyle by remember { mutableStateOf<Style?>(null) }
    var failed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var attempt by remember { mutableIntStateOf(0) }
    var savedMap by rememberSaveable { mutableStateOf<Bundle?>(null) }
    val latestClick by rememberUpdatedState(onPointClick)
    val view = remember(context) {
        MapLibre.getInstance(context)
        MapView(context, MapLibreMapOptions.createFromAttributes(context).textureMode(true)).apply {
            onCreate(savedMap)
            contentDescription = "Mapa interativo. Arraste para navegar e use dois dedos para alterar o zoom."
        }
    }
    NearbyMapLifecycle(view) { savedMap = Bundle().also(view::onSaveInstanceState) }
    DisposableEffect(view) {
        var disposed = false
        val listener = MapView.OnDidFailLoadingMapListener { failed = true; loading = false }
        view.addOnDidFailLoadingMapListener(listener)
        view.getMapAsync { native ->
            if (disposed) return@getMapAsync
            map = native
            native.uiSettings.apply {
                isLogoEnabled = false
                isAttributionEnabled = true
                attributionGravity = Gravity.TOP or Gravity.LEFT
                val density = context.resources.displayMetrics.density
                setAttributionMargins((16 * density).toInt(), (80 * density).toInt(), 0, 0)
                setCompassMargins((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), 0)
            }
            if (savedMap == null) native.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(nearbyDefaultCoordinate.latitude, nearbyDefaultCoordinate.longitude), 13.5,
            ))
        }
        onDispose { disposed = true; view.removeOnDidFailLoadingMapListener(listener); map = null }
    }
    DisposableEffect(map) {
        val native = map
        val listener = MapLibreMap.OnMapClickListener { coordinate ->
            if (native == null || style == null) false
            else {
                val pixel = native.projection.toScreenLocation(coordinate)
                val radius = 24 * context.resources.displayMetrics.density
                val hit = native.queryRenderedFeatures(RectF(pixel.x - radius, pixel.y - radius, pixel.x + radius, pixel.y + radius), NearbyPointLayer)
                    .firstOrNull()?.getNumberProperty("pointId")?.toInt()
                if (hit != null) latestClick(hit)
                hit != null
            }
        }
        native?.addOnMapClickListener(listener)
        onDispose { native?.removeOnMapClickListener(listener) }
    }
    DisposableEffect(map, attempt) {
        val native = map
        var disposed = false
        if (native != null) {
            loading = true
            failed = false
            style = null
            locationStyle = null
            native.setStyle(Style.Builder().fromUri(NearbyMapStyle)) { loaded ->
                if (!disposed) {
                    loaded.addSource(GeoJsonSource(NearbyPointSource, FeatureCollection.fromFeatures(emptyList())))
                    loaded.addLayer(CircleLayer(NearbySelectedLayer, NearbyPointSource).withProperties(
                        circleRadius(33.5f), circleColor(tokens.primary.toArgb()), circleOpacity(.35f),
                    ).withFilter(eq(get("pointId"), literal(selectedPointId ?: -1))))
                    loaded.addLayer(CircleLayer(NearbyPointLayer, NearbyPointSource).withProperties(
                        circleRadius(10f), circleColor(tokens.primary.toArgb()), circleStrokeColor(android.graphics.Color.WHITE), circleStrokeWidth(2f),
                    ))
                    style = loaded
                    failed = false
                    loading = false
                }
            }
        }
        onDispose { disposed = true }
    }
    LaunchedEffect(map, attempt, loading) {
        if (map != null && loading) {
            delay(20_000)
            if (loading) { failed = true; loading = false }
        }
    }
    LaunchedEffect(style, points, selectedPointId) {
        val loaded = style ?: return@LaunchedEffect
        val features = points.filter { it.coordinate.isValid() }.map { point ->
            Feature.fromGeometry(Point.fromLngLat(point.coordinate.longitude, point.coordinate.latitude)).apply { addNumberProperty("pointId", point.id) }
        }
        loaded.getSourceAs<GeoJsonSource>(NearbyPointSource)?.setGeoJson(FeatureCollection.fromFeatures(features))
        loaded.getLayerAs<CircleLayer>(NearbySelectedLayer)?.setFilter(eq(get("pointId"), literal(selectedPointId ?: -1)))
    }
    LaunchedEffect(style, selectedPointId) {
        val native = map ?: return@LaunchedEffect
        if (style == null) return@LaunchedEffect
        val density = context.resources.displayMetrics.density.toDouble()
        native.moveCamera(CameraUpdateFactory.paddingTo(0.0, 72 * density, 0.0,
            (if (selectedPointId == null) 91 else 245) * density))
        val point = points.firstOrNull { it.id == selectedPointId } ?: return@LaunchedEffect
        native.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(point.coordinate.latitude, point.coordinate.longitude), 14.5))
    }
    LaunchedEffect(style, location.location, location.revision) {
        val native = map ?: return@LaunchedEffect
        val loaded = style ?: return@LaunchedEffect
        try {
            val fix = location.location
            if (fix != null && location.hasPermission()) {
                if (locationStyle !== loaded) {
                    native.locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, loaded)
                        .useDefaultLocationEngine(false).build())
                    locationStyle = loaded
                }
                native.locationComponent.isLocationComponentEnabled = true
                native.locationComponent.forceLocationUpdate(fix)
                native.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fix.latitude, fix.longitude), 15.5))
            } else if (locationStyle === loaded) {
                native.locationComponent.isLocationComponentEnabled = false
            }
        } catch (_: SecurityException) { location.denied(false) }
    }
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
        Surface(Modifier.align(Alignment.TopCenter).padding(top = 80.dp), shape = CircleShape, color = tokens.surfaceBase) {
            Text("Pontos de exemplo", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = QuimiaTextStyles.medium11, color = tokens.textSecondary)
        }
        QuimiaIconButton(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 24.dp),
            icon = Lucide.Target, containerColor = if (location.location != null) tokens.primary else tokens.surfaceBase,
            contentDescription = "Centralizar na minha localização", onClick = locate)
        if (loading) Row(Modifier.align(Alignment.Center).background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(22.dp), color = tokens.primary, strokeWidth = 2.dp)
            Text("Carregando mapa…", style = QuimiaTextStyles.medium14, color = tokens.textPrimary)
        }
        if (failed) Column(Modifier.align(Alignment.Center).padding(24.dp).background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Não foi possível carregar o mapa. Verifique sua conexão.", style = QuimiaTextStyles.medium14, color = tokens.textPrimary)
            QuimiaButton(modifier = Modifier.fillMaxWidth().height(43.dp), text = "Tentar novamente", iconSize = 0, espacamento = 0, onClick = { attempt++ })
        }
        val message = when (location.status) {
            NearbyLocationStatus.Loading -> "Buscando sua localização…"
            NearbyLocationStatus.PermissionDenied -> "Sem permissão de localização. Você ainda pode navegar pelo mapa."
            NearbyLocationStatus.PermissionBlocked -> "Permita a localização nas configurações do app para usar o GPS."
            NearbyLocationStatus.Disabled -> "A localização do aparelho está desativada. Ative para usar o GPS."
            NearbyLocationStatus.Unavailable -> "Não foi possível obter sua localização. Tente novamente pelo botão de GPS."
            else -> null
        }
        if (message != null) Column(Modifier.align(Alignment.TopCenter).padding(start = 24.dp, end = 24.dp, top = 137.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuimiaToast(message = message, type = QuimiaToastType.Info, onDismiss = location::dismissNotice)
            if (location.status == NearbyLocationStatus.PermissionBlocked || location.status == NearbyLocationStatus.Disabled) {
                QuimiaButton(modifier = Modifier.fillMaxWidth().height(43.dp), text = "Abrir configurações", iconSize = 0,
                    espacamento = 0, containerColor = tokens.surfaceBase, onClick = location::openSettings)
            }
        }
        NearbyMapAttribution(Modifier.align(Alignment.BottomCenter).padding(bottom = 77.dp))
    }
}

@Composable
private fun NearbyMapAttribution(modifier: Modifier) {
    val tokens = quimiaColorTokens()
    val uri = LocalUriHandler.current
    Row(modifier.background(tokens.surfaceBase.copy(alpha = .94f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("© OpenFreeMap" to "https://openfreemap.org/", "© OpenMapTiles" to "https://openmaptiles.org/", "© OpenStreetMap" to "https://www.openstreetmap.org/copyright").forEach { (label, url) ->
            Text(label, Modifier.clickable { uri.openUri(url) }, style = QuimiaTextStyles.medium11.copy(fontSize = 9.sp, lineHeight = 11.sp), color = tokens.textPrimary)
        }
    }
}

@Composable
private fun NearbyMapLifecycle(view: MapView, save: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val context = LocalContext.current.applicationContext
    val latestSave by rememberUpdatedState(save)
    DisposableEffect(view, owner) {
        var started = false
        var resumed = false
        fun sync() {
            val state = owner.lifecycle.currentState
            if (state.isAtLeast(Lifecycle.State.STARTED) && !started) { view.onStart(); started = true }
            if (state.isAtLeast(Lifecycle.State.RESUMED) && !resumed) { view.onResume(); resumed = true }
            if (!state.isAtLeast(Lifecycle.State.RESUMED) && resumed) { latestSave(); view.onPause(); resumed = false }
            if (!state.isAtLeast(Lifecycle.State.STARTED) && started) { view.onStop(); started = false }
        }
        val observer = LifecycleEventObserver { _, _ -> sync() }
        val memory = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            @Deprecated("Legacy lifecycle callback required by the native map SDK")
            override fun onLowMemory() { view.onLowMemory() }
        }
        owner.lifecycle.addObserver(observer)
        context.registerComponentCallbacks(memory)
        sync()
        onDispose {
            owner.lifecycle.removeObserver(observer)
            context.unregisterComponentCallbacks(memory)
            latestSave()
            if (resumed) view.onPause()
            if (started) view.onStop()
            view.onDestroy()
        }
    }
}
