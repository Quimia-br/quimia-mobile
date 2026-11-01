package com.quimia.android.presentation.screens.proximity

import com.quimia.android.data.proximity.ApiPoint

/** A saved Home destination may be outside the currently loaded page of nearby points. */
internal fun nearbyVisiblePoints(points: List<ApiPoint>, saved: List<ApiPoint>, selectedId: Int?, type: String): List<ApiPoint> {
    val visible = points.filter { it.tipo == type }.distinctBy { it.id }
    val selected = saved.firstOrNull { it.id == selectedId && it.tipo == type } ?: return visible
    return if (visible.any { it.id == selected.id }) visible.map { if (it.id == selected.id) selected else it }
    else visible + selected
}
