package com.handbook.app.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.handbook.app.core.di.UserPreferences
import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.LoginUser
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.core.domain.model.UserData
import com.handbook.app.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferenceUserDataRepository @Inject constructor(
    @UserPreferences val dataStore: DataStore<Preferences>
) : UserDataRepository {

    override val userData: Flow<UserData>
        get() = dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                UserData(
                    userId = preferences[UserPreferenceKeys.USER_ID] ?: "dummy-id",
                    unreadNotificationCount = preferences[UserPreferenceKeys.UNREAD_NOTIFICATION_COUNT] ?: 0,
                    onboardStep = preferences[UserPreferenceKeys.ONBOARD_STEP] ?: "",
                    serverUnderMaintenance = preferences[UserPreferenceKeys.SERVER_UNDER_MAINTENANCE]?.toBooleanStrictOrNull() ?: false,
                    lastGreetedTime = preferences[UserPreferenceKeys.LAST_GREETED_TIME]?.toLongOrNull() ?: 0L,
                    themeBrand = ThemeBrand.valueOf(preferences[UserPreferenceKeys.THEME_BRAND] ?: ThemeBrand.default().name),
                    darkThemeConfig = DarkThemeConfig.valueOf(preferences[UserPreferenceKeys.DARK_THEME_CONFIG] ?: DarkThemeConfig.default().name),
                    useDynamicColor = preferences[UserPreferenceKeys.USE_DYNAMIC_COLOR] ?: false
                )
            }

    override suspend fun setUserData(userData: LoginUser?) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.USER_ID] = userData?.userId ?: "dummy-id"
            preferences[UserPreferenceKeys.ONBOARD_STEP] = userData?.onboardStep ?: ""
            preferences[UserPreferenceKeys.UNREAD_NOTIFICATION_COUNT] = userData?.notificationCount ?: 0
        }
    }

    override suspend fun updateUnreadNotificationCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.UNREAD_NOTIFICATION_COUNT] = count
        }
    }

    override suspend fun setServerUnderMaintenance(underMaintenance: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.SERVER_UNDER_MAINTENANCE] = underMaintenance.toString()
        }
    }

    override suspend fun setLastGreetedTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.LAST_GREETED_TIME] = timestamp.toString()
        }
    }

    override suspend fun updateOnboardStep(onboardStep: String) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.ONBOARD_STEP] = onboardStep
        }
    }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.THEME_BRAND] = themeBrand.name
        }
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.DARK_THEME_CONFIG] = darkThemeConfig.name
        }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.USE_DYNAMIC_COLOR] = useDynamicColor
        }
    }

    companion object {
        object UserPreferenceKeys {
            val USER_ID = stringPreferencesKey("user_id")
            val UNREAD_NOTIFICATION_COUNT = intPreferencesKey("unread_notification_count")
            val ONBOARD_STEP = stringPreferencesKey("onboard_step")
            val SERVER_UNDER_MAINTENANCE = stringPreferencesKey("server_under_maintenance")
            val LAST_GREETED_TIME = stringPreferencesKey("last_greeted_time")
            val THEME_BRAND = stringPreferencesKey("theme_brand")
            val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
            val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        }
    }
}