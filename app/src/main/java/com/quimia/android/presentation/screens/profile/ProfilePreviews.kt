package com.quimia.android.presentation.screens.profile

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import com.quimia.android.R
import com.quimia.android.presentation.preview.QuimiaScreenPreviews
import theme.QuimiaTheme

@Preview(name = "Figma · Perfil", group = "Figma", widthDp = 360, heightDp = 800)
@QuimiaScreenPreviews
@Preview(name = "Tablet deitado", group = "Quimia · Dispositivos", widthDp = 1340, heightDp = 800)
@Composable
private fun ProfileFigmaPreview() {
    QuimiaTheme {
        ProfileScreen("Fernando Pessoa", "fernandopessoa@email.com", ProfilePreferences(dyslexicFont = true),
            {}, {}, {}, {}, painterResource(R.drawable.profile_avatar_figma))
    }
}

@Preview(name = "Perfil interativo", group = "Perfil", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileInteractivePreview() {
    var preferences by remember { mutableStateOf(ProfilePreferences()) }
    var name by remember { mutableStateOf("Fernando Pessoa") }
    ProfilePreviewTheme(preferences) {
        ProfileScreen(name, "fernandopessoa@email.com", preferences, { preferences = it }, { name = it }, {}, {},
            painterResource(R.drawable.profile_avatar_figma))
    }
}

@Preview(name = "Escuro", group = "Acessibilidade", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileDarkPreview() = ProfileAccessibilityPreview(ProfilePreferences(theme = ProfileTheme.Dark))

@Preview(name = "Alto contraste", group = "Acessibilidade", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileContrastPreview() = ProfileAccessibilityPreview(ProfilePreferences(theme = ProfileTheme.HighContrast))

@Preview(name = "Texto 140% e OpenDyslexic", group = "Acessibilidade", widthDp = 320, heightDp = 720)
@Composable
private fun ProfileDyslexicPreview() = ProfileAccessibilityPreview(ProfilePreferences(textSizeStep = 2, dyslexicFont = true))

@Preview(name = "Editar perfil", group = "Perfil", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileEditPreview() {
    QuimiaTheme {
        ProfileScreen("Fernando Pessoa", "fernandopessoa@email.com", ProfilePreferences(), {}, {}, {}, {},
            painterResource(R.drawable.profile_avatar_figma), initialEditVisible = true)
    }
}

@Composable
private fun ProfileAccessibilityPreview(preferences: ProfilePreferences) {
    ProfilePreviewTheme(preferences) {
        ProfileScreen("Fernando Pessoa", "fernandopessoa@email.com", preferences, {}, {}, {}, {},
            painterResource(R.drawable.profile_avatar_figma))
    }
}

@Composable
private fun ProfilePreviewTheme(preferences: ProfilePreferences, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * preferences.textScale)) {
        QuimiaTheme(darkTheme = preferences.theme.usesDarkColors(false), highContrast = preferences.theme == ProfileTheme.HighContrast,
            useDyslexicFont = preferences.dyslexicFont, content = content)
    }
}
