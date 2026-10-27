package com.quimia.android.presentation.screens.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.quimia.android.presentation.preview.QuimiaScreenPreviews
import theme.QuimiaTheme

@Composable
private fun ErrorPreview(kind: GenericErrorKind) { QuimiaTheme(false) { GenericErrorScreen(kind, {}, {}) } }

@QuimiaScreenPreviews
@Preview(name = "Figma · Algo deu errado", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun UnknownErrorPreview() = ErrorPreview(GenericErrorKind.Unknown)

@QuimiaScreenPreviews
@Preview(name = "Figma · Sem conexão", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun OfflineErrorPreview() = ErrorPreview(GenericErrorKind.Offline)

@QuimiaScreenPreviews
@Preview(name = "Figma · Serviço indisponível", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun ServiceUnavailablePreview() = ErrorPreview(GenericErrorKind.ServiceUnavailable)

@QuimiaScreenPreviews
@Preview(name = "Figma · Sessão expirada", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun SessionExpiredPreview() = ErrorPreview(GenericErrorKind.SessionExpired)

@QuimiaScreenPreviews
@Preview(name = "Figma · Acesso não permitido", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun AccessDeniedPreview() = ErrorPreview(GenericErrorKind.AccessDenied)

@QuimiaScreenPreviews
@Preview(name = "Figma · Conteúdo indisponível", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun ContentUnavailablePreview() = ErrorPreview(GenericErrorKind.ContentUnavailable)

@QuimiaScreenPreviews
@Preview(name = "Figma · Tempo esgotado", group = "Erros genéricos", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun TimeoutErrorPreview() = ErrorPreview(GenericErrorKind.Timeout)
