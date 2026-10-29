package com.quimia.android.presentation.components

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.CachePolicy

internal fun profilePhotoRequest(context: Context, userId: String, url: String, animate: Boolean = true): ImageRequest =
    ImageRequest.Builder(context.applicationContext).data(url).size(256)
        .memoryCacheKey("profile:$userId:$url").diskCacheKey(url)
        .memoryCachePolicy(CachePolicy.ENABLED).diskCachePolicy(CachePolicy.ENABLED)
        .crossfade(if (animate) motion.QuimiaMotion.ImageFade else 0).build()

internal fun prefetchProfilePhoto(context: Context, userId: String, url: String?) {
    if (userId.isNotBlank() && url?.startsWith("https://") == true)
        context.imageLoader.enqueue(profilePhotoRequest(context, userId, url))
}

@Composable
internal fun rememberProfilePhoto(userId: String, url: String?): coil.compose.AsyncImagePainter {
    val context = LocalContext.current
    val animate = motion.LocalQuimiaMotionEnabled.current
    val request = remember(context, userId, url, animate) {
        url?.takeIf { it.startsWith("https://") }?.let { profilePhotoRequest(context, userId, it, animate) }
    }
    return rememberAsyncImagePainter(request)
}
