package com.quimia.android.presentation.screens.auth.registration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.quimia.android.R
import com.quimia.android.presentation.components.BirthDateField
import com.quimia.android.presentation.screens.auth.*
import com.quimia.android.presentation.screens.profile.profileInitials
import input.QuimiaInput
import theme.QuimiaTheme
import title.QuimiaMainTitle

@Composable
fun GoogleDetailsScreen(profile: GoogleOnboarding, onChange: (String, String) -> Unit,
    onContinue: () -> Unit, onLogin: () -> Unit, previewPhoto: Painter? = null) {
    val photo = rememberAsyncImagePainter(profile.photoUrl)
    AuthScreenLayout(bottomPadding = 50.dp, bodyVerticalPadding = 0.dp, footerHorizontalPadding = 35.5.dp, footer = {
        AuthPrimaryButton("Continuar", onContinue, enabled = profile.valid)
        Spacer(Modifier.height(8.dp))
        AuthFooterLink("Já tem cadastro? ", "Faça login aqui", onLogin)
    }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuimiaAvatar(size = 45.dp, photo = previewPhoto ?: photo.takeUnless { profile.photoUrl == null || it.state is AsyncImagePainter.State.Error },
                initials = profileInitials(profile.name), contentDescription = "Foto da conta Google")
            QuimiaMainTitle(title = "Autenticado com\ngoogle")
        }
        Spacer(Modifier.height(31.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            QuimiaInput(profile.name, { onChange(it.take(80), profile.birthDate) }, label = "Nome", placeholder = "Seu nome",
                error = authNameError(profile.name).takeIf { profile.name.isNotEmpty() })
            BirthDateField(profile.birthDate, { onChange(profile.name, it) }, error = authBirthDateError(profile.birthDate).takeIf { profile.birthDate.isNotEmpty() })
        }
    }
}

@Preview(name = "Figma · Cadastro Google", group = "Autenticação", widthDp = 393, heightDp = 852, showSystemUi = true)
@Composable
fun GoogleDetailsPreview() {
    QuimiaTheme {
        GoogleDetailsScreen(GoogleOnboarding("figma", "gabriela@example.invalid", "Gabriela Benfica", "20/04/2010"),
            { _, _ -> }, {}, {}, previewPhoto = painterResource(R.drawable.google_avatar_figma))
    }
}

@Preview(name = "Escuro · Cadastro Google", group = "Autenticação", widthDp = 393, heightDp = 852, showSystemUi = true)
@Composable
fun GoogleDetailsDarkPreview() {
    QuimiaTheme(darkTheme = true) {
        GoogleDetailsScreen(GoogleOnboarding("figma", "gabriela@example.invalid", "Gabriela Benfica", "20/04/2010"),
            { _, _ -> }, {}, {}, previewPhoto = painterResource(R.drawable.google_avatar_figma))
    }
}
