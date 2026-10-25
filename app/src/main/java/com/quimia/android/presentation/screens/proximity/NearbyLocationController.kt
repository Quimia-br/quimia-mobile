package com.quimia.android.presentation.screens.proximity

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.CancellationSignal
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

internal class NearbyLocationController(private val context: Context, private val scope: CoroutineScope) {
    var status by mutableStateOf(NearbyLocationStatus.Idle)
        private set
    var location by mutableStateOf<Location?>(null)
        private set
    var revision by mutableIntStateOf(0)
        private set
    private var active = false
    private var request: Job? = null

    fun hasPermission(): Boolean = nearbyLocationPermission(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED,
    )

    fun resume() {
        active = true
        if (hasPermission()) locate()
        else {
            location = null
            if (status == NearbyLocationStatus.Ready || status == NearbyLocationStatus.Loading) status = NearbyLocationStatus.Idle
        }
    }

    fun pause() {
        active = false
        request?.cancel()
        if (status == NearbyLocationStatus.Loading) status = NearbyLocationStatus.Idle
    }

    fun denied(blocked: Boolean) {
        location = null
        status = if (blocked) NearbyLocationStatus.PermissionBlocked else NearbyLocationStatus.PermissionDenied
    }

    fun dismissNotice() { status = NearbyLocationStatus.Idle }

    fun openSettings() {
        val intent = if (status == NearbyLocationStatus.Disabled) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            status = NearbyLocationStatus.Unavailable
        }
    }

    fun locate() {
        if (!active) return
        request?.cancel()
        if (!hasPermission()) { denied(false); return }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) { location = null; status = NearbyLocationStatus.Unavailable; return }
        if (!LocationManagerCompat.isLocationEnabled(manager)) {
            location = null
            status = NearbyLocationStatus.Disabled
            return
        }
        val precise = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val enabled = try { manager.getProviders(true).toSet() } catch (_: SecurityException) { denied(false); return }
        val provider = nearbyLocationProvider(precise, enabled)
        if (provider == null) { location = null; status = NearbyLocationStatus.Unavailable; return }
        status = NearbyLocationStatus.Loading
        request = scope.launch {
            try {
                val fix = withTimeoutOrNull(20_000) {
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val cancellation = CancellationSignal()
                        continuation.invokeOnCancellation { cancellation.cancel() }
                        LocationManagerCompat.getCurrentLocation(manager, provider, cancellation, ContextCompat.getMainExecutor(context)) { result ->
                            if (continuation.isActive) continuation.resume(result)
                        }
                    }
                }
                if (fix != null && NearbyCoordinate(fix.latitude, fix.longitude).isValid()) {
                    location = fix
                    revision++
                    status = NearbyLocationStatus.Ready
                } else {
                    location = null
                    status = NearbyLocationStatus.Unavailable
                }
            } catch (_: SecurityException) {
                denied(false)
            } catch (_: IllegalArgumentException) {
                location = null
                status = NearbyLocationStatus.Unavailable
            }
        }
    }
}

@Composable
internal fun rememberNearbyLocation(): Pair<NearbyLocationController, () -> Unit> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    val controller = remember(context, scope) { NearbyLocationController(context, scope) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (controller.hasPermission()) controller.locate()
        else {
            val activity = context.nearbyActivity()
            val canExplain = activity != null && (
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                )
            controller.denied(blocked = activity != null && !canExplain)
        }
    }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> controller.resume()
                Lifecycle.Event.ON_PAUSE -> controller.pause()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) controller.resume()
        onDispose { owner.lifecycle.removeObserver(observer); controller.pause() }
    }
    val locate: () -> Unit = {
        if (controller.hasPermission()) controller.locate()
        else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    return controller to locate
}

private fun Context.nearbyActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
