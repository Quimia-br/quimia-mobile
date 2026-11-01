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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import motion.quimiaClickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.quimia.android.BuildConfig
import com.quimia.android.presentation.screens.proximity.NearbyCoordinate
import com.quimia.android.presentation.screens.proximity.NearbyMapLifecycle
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

/** The point is always supplied by the authenticated API; no example coordinates. */
@Composable
internal fun HomePointMap(pointId: Int, name: String, coordinate: NearbyCoordinate?, onOpen: () -> Unit) {
    val tokens = quimiaColorTokens()
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth().height(112.dp).border(1.dp, tokens.secondaryPressed, shape).clip(shape), contentAlignment = Alignment.Center) {
        if (theme.LocalQuimiaSkeleton.current) {
            com.quimia.android.presentation.components.SkeletonBlock(Modifier.fillMaxSize(), 16)
        } else if (coordinate == null || !coordinate.isValid() || !BuildConfig.GOOGLE_MAPS_CONFIGURED || LocalInspectionMode.current) {
            Text("Ver ponto salvo", style = QuimiaTextStyles.medium12, color = tokens.foregroundPrimary)
        } else key(pointId) {
            val context = LocalContext.current
            val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var saved by rememberSaveable { mutableStateOf<android.os.Bundle?>(null) }
            var map by remember { mutableStateOf<GoogleMap?>(null) }
            val open by rememberUpdatedState(onOpen)
            val compact = motion.LocalQuimiaRenderingBudget.current == motion.QuimiaRenderingBudget.Compact
            val view = remember(context, owner, compact) { MapView(context, GoogleMapOptions().mapType(GoogleMap.MAP_TYPE_HYBRID).liteMode(compact)).apply { onCreate(saved) } }
            NearbyMapLifecycle(view) { saved = android.os.Bundle().also(view::onSaveInstanceState) }
            DisposableEffect(view) {
                var disposed = false
                view.getMapAsync { native ->
                    if (!disposed) {
                        native.uiSettings.apply {
                            setAllGesturesEnabled(false); isMapToolbarEnabled = false; isCompassEnabled = false
                            isZoomControlsEnabled = false
                        }
                        native.setOnMapClickListener { open() }
                        map = native
                    }
                }
                onDispose { disposed = true; map?.setOnMapClickListener(null); map = null }
            }
            LaunchedEffect(map, coordinate) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), 16f))
            }
            AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().clearAndSetSemantics { })
        }
        if (!theme.LocalQuimiaSkeleton.current) Box(Modifier.fillMaxSize()
            .quimiaClickable(role = Role.Button, onClickLabel = "Abrir ponto salvo em Proximidade", onClick = onOpen)
            .semantics { contentDescription = "Mapa do último descarte salvo: $name" })
    }
}
