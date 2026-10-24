package com.quimia.android.presentation.screens.proximity

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Target
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.quimia.android.BuildConfig
import kotlinx.coroutines.delay
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType
import kotlin.math.roundToInt

@Composable
internal fun ProximityMap(
    points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit,
    bottomContentInset: Dp = if (selectedPointId == null) 91.dp else 253.dp,
) {
    if (LocalInspectionMode.current) {
        ProximityMapPreview(points, selectedPointId, onPointClick)
        return
    }
    val context = LocalContext.current
    val servicesAvailable = remember(context) {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
    if (!BuildConfig.GOOGLE_MAPS_CONFIGURED || !servicesAvailable) {
        NearbyMapNotice(
            message = when {
                !servicesAvailable -> "O Google Maps precisa do Google Play Services atualizado neste aparelho."
                BuildConfig.DEBUG -> "Configure MAPS_API_KEY no local.properties e compile novamente para visualizar o Google Maps."
                else -> "Mapa indisponível no momento."
            },
        )
        return
    }
    var attempt by remember { mutableIntStateOf(0) }
    var savedMap by rememberSaveable { mutableStateOf<Bundle?>(null) }
    key(attempt) {
        NearbyGoogleMap(points, selectedPointId, onPointClick, bottomContentInset, savedMap,
            onSaveState = { savedMap = it }, onRetry = { attempt++ })
    }
}

@Composable
private fun NearbyGoogleMap(
    points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit,
    bottomContentInset: Dp, savedMap: Bundle?, onSaveState: (Bundle) -> Unit, onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val tokens = quimiaColorTokens()
    val density = context.resources.displayMetrics.density
    val (location, locate) = rememberNearbyLocation()
    val source = remember { NearbyGoogleLocationSource() }
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    val latestClick by rememberUpdatedState(onPointClick)
    val view = remember(context, owner) {
        MapView(context, GoogleMapOptions().mapType(GoogleMap.MAP_TYPE_HYBRID)).apply {
            onCreate(savedMap)
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val radius = 39f * density
                    outline.setRoundRect(0, 0, view.width, view.height + radius.roundToInt(), radius)
                }
            }
            addOnLayoutChangeListener { child, _, _, _, _, _, _, _, _ -> child.invalidateOutline() }
            contentDescription = "Google Maps por satélite. Arraste para navegar e use dois dedos para alterar o zoom."
        }
    }
    NearbyMapLifecycle(view) { onSaveState(Bundle().also(view::onSaveInstanceState)) }
    DisposableEffect(view) {
        var disposed = false
        view.getMapAsync { native ->
            if (disposed) return@getMapAsync
            native.mapType = GoogleMap.MAP_TYPE_HYBRID
            native.uiSettings.apply {
                isMyLocationButtonEnabled = false
                isZoomControlsEnabled = false
                isMapToolbarEnabled = false
                isCompassEnabled = false
            }
            native.setLocationSource(source)
            native.setOnMapLoadedCallback { if (!disposed) { loading = false; failed = false } }
            native.setOnMarkerClickListener { marker ->
                (marker.tag as? Int)?.let { latestClick(it) }
                true
            }
            if (savedMap == null) native.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(nearbyDefaultCoordinate.latitude, nearbyDefaultCoordinate.longitude), 13.5f,
            ))
            map = native
        }
        onDispose {
            disposed = true
            map?.setOnMarkerClickListener(null)
            map?.setOnMapLoadedCallback(null)
            map = null
        }
    }
    LaunchedEffect(view) {
        delay(20_000)
        if (loading) { failed = true; loading = false }
    }
    val primary = tokens.primary.toArgb()
    DisposableEffect(map, points, selectedPointId, primary, density) {
        val native = map
        val markers = if (native == null) emptyList() else points.filter { it.coordinate.isValid() }.mapNotNull { point ->
            native.addMarker(MarkerOptions()
                .position(LatLng(point.coordinate.latitude, point.coordinate.longitude))
                .title(point.name).snippet(point.address).anchor(.5f, .5f)
                .zIndex(if (point.id == selectedPointId) 2f else 1f)
                .icon(BitmapDescriptorFactory.fromBitmap(nearbyMarkerBitmap(primary, density, point.id == selectedPointId))))
                ?.apply { tag = point.id }
        }
        onDispose { markers.forEach { it.remove() } }
    }
    LaunchedEffect(map, bottomContentInset) {
        map?.setPadding(0, (72 * density).roundToInt(), 0, (bottomContentInset.value * density).roundToInt())
    }
    LaunchedEffect(map, selectedPointId) {
        val point = points.firstOrNull { it.id == selectedPointId } ?: return@LaunchedEffect
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(point.coordinate.latitude, point.coordinate.longitude), 14.5f))
    }
    LaunchedEffect(map, location.location, location.revision) {
        val native = map ?: return@LaunchedEffect
        try {
            val fix = location.location?.takeIf { location.hasPermission() }
            source.update(fix)
            native.isMyLocationEnabled = fix != null
            if (fix != null) native.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fix.latitude, fix.longitude), 15.5f))
        } catch (_: SecurityException) { source.update(null); location.denied(false) }
    }
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
        QuimiaIconButton(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 24.dp),
            icon = Lucide.Target, containerColor = if (location.location != null) tokens.primary else tokens.surfaceBase,
            contentDescription = "Centralizar na minha localização", onClick = locate)
        if (loading) Row(Modifier.align(Alignment.Center).background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(22.dp), color = tokens.primary, strokeWidth = 2.dp)
            Text("Carregando mapa…", style = QuimiaTextStyles.medium14, color = tokens.textPrimary)
        }
        if (failed) NearbyMapNotice(
            message = "Não foi possível carregar o mapa. Verifique a conexão e a configuração da chave do Google Maps.",
            onRetry = onRetry,
        )
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
    }
}

@Composable
private fun NearbyMapNotice(message: String, onRetry: (() -> Unit)? = null) {
    val tokens = quimiaColorTokens()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp).background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = QuimiaTextStyles.medium14, color = tokens.textPrimary)
            if (onRetry != null) QuimiaButton(modifier = Modifier.fillMaxWidth().height(43.dp), text = "Tentar novamente",
                iconSize = 0, espacamento = 0, onClick = onRetry)
        }
    }
}

private fun nearbyMarkerBitmap(color: Int, density: Float, selected: Boolean): Bitmap {
    val size = (67 * density).roundToInt().coerceAtLeast(1)
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val center = size / 2f
        if (selected) {
            paint.color = color
            paint.alpha = 89
            canvas.drawCircle(center, center, 33.5f * density, paint)
        }
        paint.color = android.graphics.Color.WHITE
        paint.alpha = 255
        canvas.drawCircle(center, center, 10f * density, paint)
        paint.color = color
        canvas.drawCircle(center, center, 8f * density, paint)
    }
}

private class NearbyGoogleLocationSource : LocationSource {
    private var listener: LocationSource.OnLocationChangedListener? = null
    private var latest: Location? = null
    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
        latest?.let(listener::onLocationChanged)
    }
    override fun deactivate() { listener = null }
    fun update(location: Location?) {
        latest = location
        location?.let { listener?.onLocationChanged(it) }
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
