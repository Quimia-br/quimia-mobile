package com.quimia.android.presentation.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import motion.quimiaClickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.quimia.android.BuildConfig
import com.quimia.android.presentation.screens.proximity.NearbyCoordinate
import com.quimia.android.presentation.screens.proximity.NearbyMapLifecycle
import com.quimia.android.presentation.screens.proximity.nearbyMarkerBitmap
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

/** The point is always supplied by the authenticated API; no example coordinates. */
@Composable
internal fun HomePointMap(pointId: Int, name: String, coordinate: NearbyCoordinate?, onUnavailable: () -> Unit) {
    val tokens = quimiaColorTokens()
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth().height(76.dp).border(1.dp, tokens.secondaryPressed, shape).clip(shape), contentAlignment = Alignment.Center) {
        if (theme.LocalQuimiaSkeleton.current) {
            com.quimia.android.presentation.components.SkeletonBlock(Modifier.fillMaxSize(), 16)
        } else if (coordinate == null || !coordinate.isValid() || !BuildConfig.GOOGLE_MAPS_CONFIGURED || LocalInspectionMode.current) {
            androidx.compose.material3.TextButton(onClick = onUnavailable) {
                Text(if (coordinate == null) "Ver ponto salvo" else "Abrir mapa", style = QuimiaTextStyles.medium12, color = tokens.foregroundPrimary)
            }
        } else key(pointId) {
            val context = LocalContext.current
            val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var saved by rememberSaveable { mutableStateOf<android.os.Bundle?>(null) }
            var zoomed by rememberSaveable { mutableStateOf(false) }
            var map by remember { mutableStateOf<GoogleMap?>(null) }
            val latestName by rememberUpdatedState(name)
            val compact = motion.LocalQuimiaRenderingBudget.current == motion.QuimiaRenderingBudget.Compact
            val view = remember(context, owner, compact) { MapView(context, GoogleMapOptions().mapType(GoogleMap.MAP_TYPE_HYBRID).liteMode(compact)).apply { onCreate(saved) } }
            val icon = rememberVectorPainter(Lucide.Recycle)
            val marker = remember(tokens.primary, icon) {
                nearbyMarkerBitmap(tokens.primary.toArgb(), context.resources.displayMetrics.density, false, icon)
            }
            NearbyMapLifecycle(view) { saved = android.os.Bundle().also(view::onSaveInstanceState) }
            DisposableEffect(view) {
                var disposed = false
                view.getMapAsync { native ->
                    if (!disposed) {
                        native.uiSettings.apply {
                            setAllGesturesEnabled(false); isMapToolbarEnabled = false; isCompassEnabled = false
                            isZoomControlsEnabled = false
                        }
                        val toggleZoom = {
                            zoomed = !zoomed
                            val update = CameraUpdateFactory.newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), if (zoomed) 18f else 14.5f)
                            if (compact) native.moveCamera(update) else native.animateCamera(update)
                        }
                        native.setOnMapClickListener { toggleZoom() }
                        native.setOnMarkerClickListener { toggleZoom(); true }
                        map = native
                    }
                }
                onDispose { disposed = true; map?.setOnMapClickListener(null); map?.setOnMarkerClickListener(null); map = null }
            }
            DisposableEffect(map, coordinate, marker) {
                val native = map
                val target = LatLng(coordinate.latitude, coordinate.longitude)
                val pin = native?.addMarker(MarkerOptions().position(target).title(latestName).anchor(.5f, .5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(marker)))
                native?.moveCamera(CameraUpdateFactory.newLatLngZoom(target, if (zoomed) 18f else 14.5f))
                onDispose { pin?.remove() }
            }
            AndroidView(factory = { view }, modifier = Modifier.fillMaxSize(), update = {
                it.contentDescription = "Último descarte salvo: $name. Toque para aproximar ou afastar o mapa."
            })
            Box(Modifier.fillMaxSize().quimiaClickable(role = Role.Button, onClickLabel = "Alterar zoom do ponto salvo") {
                zoomed = !zoomed
                val update = CameraUpdateFactory.newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), if (zoomed) 18f else 14.5f)
                if (compact) map?.moveCamera(update) else map?.animateCamera(update)
            }.semantics {
                contentDescription = "Mapa do último descarte salvo: $name"
                stateDescription = if (zoomed) "Aproximado" else "Visão da região"
            })
        }
    }
}
