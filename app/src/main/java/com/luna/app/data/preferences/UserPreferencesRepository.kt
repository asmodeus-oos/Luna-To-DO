package com.luna.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.TimeZone

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "luna_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val AMOLED_ENABLED = booleanPreferencesKey("amoled_enabled")
        val SHOW_NAV_TAB_LABELS = booleanPreferencesKey("show_nav_tab_labels")
        val AUTO_DELETE_COMPLETED_DAILY = booleanPreferencesKey("auto_delete_completed_daily")
        val USER_COUNTRY = stringPreferencesKey("user_country")
        val USER_TIMEZONE = stringPreferencesKey("user_timezone")
        val LAST_CLEANUP_DAY = stringPreferencesKey("last_cleanup_day")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AVATAR_PATH = stringPreferencesKey("user_avatar_path")
        val FOCUS_WORK_SECONDS = intPreferencesKey("focus_work_seconds")
        val SHORT_BREAK_SECONDS = intPreferencesKey("short_break_seconds")
        val LONG_BREAK_SECONDS = intPreferencesKey("long_break_seconds")
        val USER_COVER_PATH = stringPreferencesKey("user_cover_path")
        val COVER_TITLE = stringPreferencesKey("cover_title")
        val SHOW_COVER_BANNER_TEXT = booleanPreferencesKey("show_cover_banner_text")
        val USER_GENDER = stringPreferencesKey("user_gender")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    private val fastPrefs = context.getSharedPreferences("luna_fast_cache", Context.MODE_PRIVATE)

    fun getInitialThemeMode(): AppThemeMode {
        val cached = fastPrefs.getString("cached_theme_mode", null)
        if (cached != null) {
            try {
                return AppThemeMode.valueOf(cached)
            } catch (_: Exception) {}
        }
        val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        return if (isSystemDark) AppThemeMode.DARK else AppThemeMode.LIGHT
    }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: getInitialThemeMode().name
        val mode = try {
            AppThemeMode.valueOf(themeString)
        } catch (_: Exception) {
            getInitialThemeMode()
        }
        fastPrefs.edit().putString("cached_theme_mode", mode.name).apply()
        mode
    }

    val amoledEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AMOLED_ENABLED] ?: (preferences[PreferencesKeys.THEME_MODE] == AppThemeMode.BLACK.name)
    }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val hapticsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
    }

    val showNavTabLabelsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_NAV_TAB_LABELS] ?: false
    }

    val autoDeleteCompletedDailyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_DELETE_COMPLETED_DAILY] ?: false
    }

    val userCountryFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_COUNTRY] ?: Locale.getDefault().displayCountry.ifBlank { "United States" }
    }

    val userTimezoneFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_TIMEZONE] ?: TimeZone.getDefault().id.ifBlank { "UTC" }
    }

    val lastCleanupDayFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_CLEANUP_DAY] ?: ""
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        fastPrefs.edit().putString("cached_theme_mode", themeMode.name).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setAmoledEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AMOLED_ENABLED] = enabled
            val currentTheme = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.LIGHT.name
            if (currentTheme == AppThemeMode.DARK.name || currentTheme == AppThemeMode.BLACK.name) {
                val newMode = if (enabled) AppThemeMode.BLACK.name else AppThemeMode.DARK.name
                preferences[PreferencesKeys.THEME_MODE] = newMode
                fastPrefs.edit().putString("cached_theme_mode", newMode).apply()
            }
        }
    }

    suspend fun toggleThemeMode(isAmoled: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.LIGHT.name
            val isDark = current == AppThemeMode.DARK.name || current == AppThemeMode.BLACK.name
            val next = if (isDark) {
                AppThemeMode.LIGHT.name
            } else {
                if (isAmoled) AppThemeMode.BLACK.name else AppThemeMode.DARK.name
            }
            preferences[PreferencesKeys.THEME_MODE] = next
            fastPrefs.edit().putString("cached_theme_mode", next).apply()
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setShowNavTabLabels(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_NAV_TAB_LABELS] = enabled
        }
    }

    suspend fun setAutoDeleteCompletedDaily(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_DELETE_COMPLETED_DAILY] = enabled
        }
    }

    suspend fun setUserCountry(country: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_COUNTRY] = country
        }
    }

    suspend fun setUserTimezone(timezone: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_TIMEZONE] = timezone
        }
    }

    suspend fun setLastCleanupDay(day: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CLEANUP_DAY] = day
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    val userAvatarPathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_AVATAR_PATH] ?: ""
    }

    val focusWorkSecondsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOCUS_WORK_SECONDS] ?: (25 * 60)
    }

    val shortBreakSecondsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHORT_BREAK_SECONDS] ?: (5 * 60)
    }

    val longBreakSecondsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LONG_BREAK_SECONDS] ?: (15 * 60)
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setUserAvatarPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_AVATAR_PATH] = path
        }
    }

    val userCoverPathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_COVER_PATH] ?: ""
    }

    val coverTitleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COVER_TITLE] ?: "Mountains"
    }

    suspend fun setUserCoverPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_COVER_PATH] = path
        }
    }

    suspend fun setCoverTitle(title: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COVER_TITLE] = title
        }
    }

    val showCoverBannerTextFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_COVER_BANNER_TEXT] ?: true
    }

    suspend fun setShowCoverBannerText(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COVER_BANNER_TEXT] = show
        }
    }

    val userGenderFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_GENDER] ?: "BOY"
    }

    suspend fun setUserGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_GENDER] = gender
        }
    }

    fun getInitialOnboardingCompleted(): Boolean {
        return fastPrefs.getBoolean("cached_onboarding_completed", false)
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val completed = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: getInitialOnboardingCompleted()
        fastPrefs.edit().putBoolean("cached_onboarding_completed", completed).apply()
        completed
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        fastPrefs.edit().putBoolean("cached_onboarding_completed", completed).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setFocusDurations(workSeconds: Int, shortBreakSeconds: Int, longBreakSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_WORK_SECONDS] = workSeconds
            preferences[PreferencesKeys.SHORT_BREAK_SECONDS] = shortBreakSeconds
            preferences[PreferencesKeys.LONG_BREAK_SECONDS] = longBreakSeconds
        }
    }
}
