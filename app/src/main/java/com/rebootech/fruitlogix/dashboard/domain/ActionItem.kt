package com.rebootech.fruitlogix.dashboard.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class ActionItemStyle {
    LIME_BUTTON,
    DARK_BUTTON
}

data class ActionItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val captionRes: Int,
    @StringRes val buttonTextRes: Int,
    @DrawableRes val iconRes: Int,
    val hasRedDot: Boolean = false,
    val style: ActionItemStyle = ActionItemStyle.LIME_BUTTON
)
