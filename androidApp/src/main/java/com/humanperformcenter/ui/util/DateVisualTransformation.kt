package com.humanperformcenter.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // quitamos todo lo que no sea dígito
        val digits = text.text.filter { it.isDigit() }
        // construimos dd/MM/yyyy con insert de ‘/’
        val dd = digits.take(2)
        val mm = digits.drop(2).take(2)
        val yyyy = digits.drop(4).take(4)
        val formatted = buildString {
            append(dd)
            if (digits.length >= 3) append("/")
            append(mm)
            if (digits.length >= 5) append("/")
            append(yyyy)
        }
        // OffsetMapping para que el cursor avance bien
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = when {
                offset <= 2 -> offset
                offset <= 4 -> offset + 1
                offset <= 8 -> offset + 2
                else -> formatted.length
            }
            override fun transformedToOriginal(offset: Int) = when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                offset <= 10 -> offset - 2
                else -> digits.length
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetTranslator)
    }
}