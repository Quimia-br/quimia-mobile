package preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Celular compacto",
    group = "Quimia · Dispositivos",
    showBackground = true,
    backgroundColor = 0xFFE6E6E6,
    widthDp = 320,
    heightDp = 720,
)
@Preview(
    name = "Celular",
    group = "Quimia · Dispositivos",
    showBackground = true,
    backgroundColor = 0xFFE6E6E6,
    widthDp = 393,
    heightDp = 852,
)
@Preview(
    name = "Tablet",
    group = "Quimia · Dispositivos",
    showBackground = true,
    backgroundColor = 0xFFE6E6E6,
    widthDp = 800,
    heightDp = 1280,
)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class QuimiaDevicePreviews
