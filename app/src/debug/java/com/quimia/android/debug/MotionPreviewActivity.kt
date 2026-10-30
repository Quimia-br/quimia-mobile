package com.quimia.android.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.quimia.android.presentation.screens.catalog.CatalogScreen
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.home.HomeScreen
import com.quimia.android.presentation.screens.kemi.*
import com.quimia.android.presentation.screens.profile.*
import com.quimia.android.presentation.screens.proximity.ProximityScreen
import com.quimia.android.presentation.screens.rooms.*
import com.quimia.android.presentation.screens.shelf.ShelfScreen
import com.quimia.android.presentation.system.HideSystemNavigationBar
import kotlinx.coroutines.delay
import motion.QuimiaNavigationMotion
import theme.LocalQuimiaSkeleton
import theme.QuimiaTheme

/** Offline review surface using the production components. Not included in release builds. */
class MotionPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startingTab = intent.getIntExtra("tab", 0).coerceIn(0, 4)
        val skeleton = intent.getBooleanExtra("skeleton", false)
        val dark = intent.getBooleanExtra("dark", false)
        val loginPreview = intent.getBooleanExtra("login", false)
        val mixturePreview = intent.getBooleanExtra("mixture", false)
        val empty = intent.getBooleanExtra("empty", false)
        setContent {
            QuimiaTheme(darkTheme = dark) {
                HideSystemNavigationBar()
                var tab by remember { mutableIntStateOf(startingTab) }
                var saved by remember { mutableStateOf(if (empty) emptyList<String>() else sampleCatalogProducts.take(4).map { it.id }) }
                var favorites by remember { mutableStateOf(emptyList<String>()) }
                var rooms by remember { mutableStateOf(if (empty) emptyList<QuimiaRoom>() else previewRooms) }
                var profile by remember { mutableStateOf(false) }
                var preferences by remember { mutableStateOf(ProfilePreferences()) }
                var chat by remember { mutableStateOf(false) }
                val controller = remember { KemiController() }
                val navigate: (Int) -> Unit = { tab = it }
                QuimiaNavigationMotion(tab) {
                    CompositionLocalProvider(LocalQuimiaSkeleton provides skeleton) {
                        when {
                            loginPreview -> com.quimia.android.presentation.screens.auth.login.Login1Screen()
                            mixturePreview -> com.quimia.android.presentation.screens.mixtures.MixturesScreen(
                                remember { com.quimia.android.presentation.screens.mixtures.MixtureController() }, saved,
                                onClose = {}, onOpenCatalog = { tab = 1 })
                            chat -> KemiScreen(controller, onClose = { chat = false }, displayName = "Gustavo",
                                responder = { delay(1600); KemiReply.Answer("Esta é uma prévia de movimento. Aqui você pode conferir a entrada da mensagem e a rolagem do chat.") })
                            profile -> ProfileScreen("Gustavo Sousa", "preview@example.com", preferences, { preferences = it }, {}, {},
                                onBack = { profile = false }, birthDate = "20/04/1990")
                            tab == 1 -> CatalogScreen(onMenuItemSelected = navigate, onAiClick = { chat = true },
                                initialSavedProductIds = saved, onSavedProductIdsChange = { saved = it }, initialBannerVisible = false)
                            tab == 2 -> ShelfScreen(saved, { saved = it }, onMenuItemSelected = navigate,
                                favoriteIds = favorites, onFavoriteIdsChange = { favorites = it },
                                onAiClick = { chat = true }, onAddProduct = { tab = 1 })
                            tab == 3 -> ProximityScreen(onMenuItemSelected = navigate)
                            tab == 4 -> RoomsScreen(rooms, { rooms = it }, saved, onMenuItemSelected = navigate,
                                onAiClick = { chat = true }, onOpenCatalog = { tab = 1 })
                            else -> HomeScreen(onMenuItemSelected = navigate, onAvatarClick = { profile = true },
                                onAiClick = { chat = true }, onShelfClick = { tab = 2 }, onMixturesClick = { chat = true }, onFindDropOffClick = { tab = 3 })
                        }
                    }
                }
            }
        }
    }
}
