package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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
internal fun ProximityMapPreview(points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit) {
    val tokens = quimiaColorTokens()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        QuimiaImage(
            painter = painterResource(R.drawable.proximity_map_figma),
            contentDescription = "Mapa estático de referência, sem localização em tempo real",
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            backgroundColor = tokens.secondary,
        )
        points.forEach { point ->
            Box(
                modifier = Modifier.offset(x = maxWidth * point.x - 36.dp, y = maxHeight * point.y - 36.dp)
                    .size(72.dp).clip(CircleShape)
                    .semantics { contentDescription = point.name + ", " + point.distance }
                    .clickable(role = Role.Button) { onPointClick(point.id) },
                contentAlignment = Alignment.Center,
            ) {
                if (point.id == selectedPointId) Box(Modifier.size(67.dp).background(tokens.primary.copy(alpha = .35f), CircleShape))
                Box(Modifier.size(20.dp).background(tokens.primary, CircleShape).border(2.dp, Color.White, CircleShape))
            }
        }
    }
}
