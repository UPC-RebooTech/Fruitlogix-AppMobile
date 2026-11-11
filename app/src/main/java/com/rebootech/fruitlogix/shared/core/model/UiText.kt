package com.rebootech.fruitlogix.shared.core.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Plain(val value: String) : UiText()
    data class StringResource(@param:StringRes val id: Int, val args: Array<Any> = emptyArray()) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as StringResource
            if (id != other.id) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    /** Resolves the text outside composition (e.g. for a snackbar inside LaunchedEffect). */
    fun asString(context: Context): String = when (this) {
        is Plain -> value
        is StringResource -> context.getString(id, *args)
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is Plain -> value
            is StringResource -> stringResource(id, *args)
        }
    }
}
