package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin

@Composable
internal fun ProximityMapPreview(points: List<NearbyPoint>, selectedPointId: Int?, onPointClick: (Int) -> Unit) {
    val roads = listOf(
        listOf(.0f to .18f, .36f to .34f, .72f to .10f, 1f to .28f),
        listOf(.0f to .57f, .30f to .43f, .63f to .62f, 1f to .47f),
        listOf(.10f to 1f, .34f to .73f, .43f to .42f, .34f to .0f),
        listOf(.73f to 1f, .63f to .67f, .82f to .35f, .72f to .0f),
    )
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            roads.forEachIndexed { index, pointsOnRoad ->
                val path = Path()
                pointsOnRoad.forEachIndexed { pointIndex, (x, y) ->
                    val offset = Offset(size.width * x, size.height * y)
                    if (pointIndex == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
                }
                drawPath(path, color = if (index % 2 == 0) Color.White else Color(0xFFD2D5CE), style = Stroke(width = if (index % 2 == 0) 15f else 8f, cap = StrokeCap.Round))
            }
            drawCircle(Color(0xFFB9DCC8).copy(alpha = .7f), size.minDimension * .12f, Offset(size.width * .78f, size.height * .76f))
            drawCircle(Color(0xFFCDE4D7).copy(alpha = .72f), size.minDimension * .10f, Offset(size.width * .15f, size.height * .82f))
        }
        points.forEach { point ->
            val selected = selectedPointId == point.id
            Icon(
                imageVector = Lucide.MapPin, contentDescription = "${point.name}, ${point.distance}",
                tint = if (selected) Color(0xFFFF2B6B) else Color(0xFF55D8E2),
                modifier = Modifier.offset(x = maxWidth * point.x, y = maxHeight * point.y)
                    .size(if (selected) 40.dp else 34.dp)
                    .background(Color.White, CircleShape).padding(7.dp)
                    .clickable { onPointClick(point.id) },
            )
        }
    }
}
