package com.quimia.android.presentation.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

internal fun recognizedVoiceText(successful: Boolean, results: List<String>?): String? =
    if (successful) results?.firstOrNull { it.isNotBlank() }?.trim() else null

@Composable
internal fun rememberVoiceInput(
    prompt: String = "Diga o que deseja pesquisar",
    onUnavailable: (() -> Unit)? = null,
    onResult: (String) -> Unit,
): () -> Unit {
    if (LocalInspectionMode.current) return {}

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val latestResult by rememberUpdatedState(onResult)
    val latestUnavailable by rememberUpdatedState(onUnavailable)
    val latestPrompt by rememberUpdatedState(prompt)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        recognizedVoiceText(result.resultCode == Activity.RESULT_OK,
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))?.let(latestResult)
    }

    return remember(launcher, context, focusManager, keyboard) {
        {
            fun unavailable() {
                latestUnavailable?.invoke() ?: Toast.makeText(context,
                    "Entrada por voz indisponível neste aparelho", Toast.LENGTH_LONG).show()
            }
            focusManager.clearFocus()
            keyboard?.hide()
            try {
                launcher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, latestPrompt)
                })
            } catch (_: ActivityNotFoundException) {
                unavailable()
            } catch (_: SecurityException) {
                unavailable()
            }
        }
    }
}
