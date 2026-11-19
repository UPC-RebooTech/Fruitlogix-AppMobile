package com.rebootech.fruitlogix.shared.core.locale

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import java.util.Locale

/**
 * Single source of truth for the in-app language.
 *
 * - English is the default, regardless of the device language. Spanish is opt-in.
 * - Android 13+: uses the framework per-app language API (LocaleManager). The system
 *   recreates the activity, so every screen re-reads its strings.
 *   (AppCompatDelegate.setApplicationLocales is not used: it does nothing when the app
 *   has no AppCompatActivity, which is our case with ComponentActivity.)
 * - Android 8–12: stores the choice, recreates the activity, and [wrap] applies it in
 *   MainActivity.attachBaseContext.
 *
 * ViewModels must not hold resolved strings (use UiText / resource ids), because they
 * survive the recreation and would keep the old language.
 */
object AppLocale {
    private const val PREFS = "app_locale"
    private const val KEY_TAG = "language_tag"
    private const val DEFAULT_TAG = "en"

    fun current(context: Context): AppLanguage = fromTag(storedTag(context) ?: DEFAULT_TAG)

    /**
     * Call from MainActivity.onCreate. On Android 13+, makes sure the per-app locale is set
     * (English on first launch), so the device language does not override the app default.
     */
    fun ensureApplied(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val tag = storedTag(activity) ?: DEFAULT_TAG.also { store(activity, it) }
        val localeManager = activity.getSystemService(LocaleManager::class.java) ?: return
        val currentTag = localeManager.applicationLocales.takeIf { !it.isEmpty }?.get(0)?.language
        if (currentTag != tag) {
            localeManager.applicationLocales = LocaleList.forLanguageTags(tag)
        }
    }

    fun apply(activity: Activity, language: AppLanguage) {
        if (language == current(activity)) return
        val tag = toTag(language)
        store(activity, tag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.getSystemService(LocaleManager::class.java)?.applicationLocales =
                LocaleList.forLanguageTags(tag)
        } else {
            activity.recreate()
        }
    }

    /** Applies the stored (or default English) language on Android 12 and below. */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val locale = Locale.forLanguageTag(storedTag(base) ?: DEFAULT_TAG)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return base.createConfigurationContext(config)
    }

    fun toTag(language: AppLanguage): String = when (language) {
        AppLanguage.ES -> "es"
        AppLanguage.EN -> "en"
    }

    private fun fromTag(tag: String): AppLanguage =
        if (tag.equals("es", ignoreCase = true)) AppLanguage.ES else AppLanguage.EN

    private fun store(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_TAG, tag) }
    }

    private fun storedTag(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, null)
}
