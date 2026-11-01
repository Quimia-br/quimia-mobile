package com.quimia.android.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Keep only digits in the editable field so backspace never gets stuck on a slash. */
internal object BirthDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val daySeparator = text.length > 2
        val monthSeparator = text.length > 4
        return TransformedText(AnnotatedString(formatBirthDate(text.text)), object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset +
                (if (daySeparator && offset > 2) 1 else 0) + (if (monthSeparator && offset > 4) 1 else 0)
            override fun transformedToOriginal(offset: Int): Int = (offset -
                (if (daySeparator && offset > 2) 1 else 0) - (if (monthSeparator && offset > 5) 1 else 0)).coerceIn(0, text.length)
        })
    }
}
