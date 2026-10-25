package com.quimia.android.presentation.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Celular compacto",
    group = "Quimia · Dispositivos",
    showBackground = true,
    showSystemUi = true,
    widthDp = 320,
    heightDp = 720,
)
@Preview(
    name = "Celular",
    group = "Quimia · Dispositivos",
    showBackground = true,
    showSystemUi = true,
    widthDp = 393,
    heightDp = 852,
)
@Preview(
    name = "Tablet",
    group = "Quimia · Dispositivos",
    showBackground = true,
    showSystemUi = true,
    widthDp = 800,
    heightDp = 1280,
)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class QuimiaScreenPreviews
