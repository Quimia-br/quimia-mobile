package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Store
import com.composables.icons.lucide.Recycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quimia.android.R
import image.QuimiaImage
import theme.quimiaColorTokens

@Composable
internal fun ProximityMapPreview(points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit, homeCoordinate: NearbyCoordinate? = null) {
    val tokens = quimiaColorTokens()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        QuimiaImage(
            painter = painterResource(R.drawable.proximity_map_figma),
            contentDescription = "Mapa estático de referência, sem localização em tempo real",
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            backgroundColor = tokens.secondary,
        )
        Column(Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuimiaIconButton(icon = Lucide.Target, containerColor = tokens.surfaceBase, contentDescription = "Centralizar na minha localização atual")
            QuimiaIconButton(icon = Lucide.House, containerColor = tokens.surfaceBase, contentDescription = "Centralizar na minha casa")
        }
        if (homeCoordinate != null) Box(Modifier.align(Alignment.Center).size(52.dp).clip(CircleShape)
            .background(tokens.primary).border(3.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Lucide.House, contentDescription = "Minha casa · referência de preview", tint = Color(0xFF2F2F2F), modifier = Modifier.size(28.dp))
        }
        points.forEach { point ->
            Box(
                modifier = Modifier.offset(x = maxWidth * point.x - 36.dp, y = maxHeight * point.y - 36.dp)
                    .size(72.dp).clip(CircleShape)
                    .semantics { contentDescription = point.name + ", " + point.distance }
                    .clickable(role = Role.Button) { onPointClick(point.id) },
                contentAlignment = Alignment.Center,
            ) {
                if (point.id == selectedPointId) Box(Modifier.size(67.dp).background(tokens.primary.copy(alpha = .35f), CircleShape))
                Box(Modifier.size(41.dp).background(tokens.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(if (point.category == NearbyCategory.Shopping) Lucide.Store else Lucide.Recycle,
                        contentDescription = null, tint = theme.LightTokens.foregroundPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Proximidade · casa e GPS", group = "GitHub Screens", widthDp = 393, heightDp = 700)
@Composable
private fun HomeMapPreview() {
    theme.QuimiaTheme(false) {
        ProximityMap(emptyList(), null, {}, homeCoordinate = NearbyCoordinate(-23.55052, -46.63331),
            homeLabel = "Endereço cadastrado · referência de preview")
    }
}
