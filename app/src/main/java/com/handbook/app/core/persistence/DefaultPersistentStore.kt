package com.handbook.app.core.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

class DefaultPersistentStore(
    private val preferences: SharedPreferences,
) : PersistentStore {
    override val deviceId: String
        get() = getAppPreferences().getString(AppEssentialKeys.DEVICE_INSTANCE_ID, "") ?: ""

    override val deviceToken: String
        get() = getAppPreferences().getString(UserPreferenceKeys.DEVICE_TOKEN, "") ?: ""

    override val tempId: String
        get() = getAppPreferences().getString(UserPreferenceKeys.TEMP_ID, "") ?: ""

    override val fcmToken: String
        get() = getAppPreferences().getString(AppEssentialKeys.FCM_TOKEN, "") ?: ""

    override val isFcmTokenSynced: Boolean
        get() = getAppPreferences().getBoolean(AppEssentialKeys.FCM_TOKEN_SYNCED, false)

    override val lastFcmTokenSyncTime: Long
        get() = getAppPreferences().getLong(AppEssentialKeys.LAST_FCM_TOKEN_SYNC_TIME, -1L)

    override val deepLinkKey: Pair<String, String>
        get() {
            val keyType = getAppPreferences().getString(UserPreferenceKeys.DEEPLINK_KEY_TYPE, "") ?: ""
            val key = getAppPreferences().getString(UserPreferenceKeys.DEEPLINK_KEY_VALUE, "") ?: ""
            return keyType to key
        }

    override val installReferrerFetched: Boolean
        get() = getAppPreferences().getBoolean(UserPreferenceKeys.INSTALL_REFERRAL_FETCHED, false)

    override val isFeedUserGuideShown: Boolean
        get() = getAppPreferences().getBoolean(UserPreferenceKeys.FEED_USER_GUIDE_SHOWN, false)

    override val isFeedWishlistGuideShown: Boolean
        get() = getAppPreferences().getBoolean(UserPreferenceKeys.WISHLIST_GUIDE_SHOWN, false)

    override fun getOrCreateDeviceId(): String {
        val id = deviceId.ifBlank {
            UUID.randomUUID().toString().also { newId ->
                getAppPreferences().edit().putString(AppEssentialKeys.DEVICE_INSTANCE_ID, newId).apply()
            }
        }
        return id
    }

    override fun setDeviceToken(token: String): String {
        getAppPreferences().edit().putString(UserPreferenceKeys.DEVICE_TOKEN, token).apply()
        return token
    }

    override fun setFcmToken(token: String) {
        getAppPreferences().edit()
            .putString(AppEssentialKeys.FCM_TOKEN, token).apply()
    }

    override fun setTempId(tempId: String) {
        getAppPreferences().edit()
            .putString(UserPreferenceKeys.TEMP_ID, tempId).apply()
    }

    override fun setFcmTokenSynced(isSynced: Boolean): Boolean {
        getAppPreferences().edit()
            .putBoolean(AppEssentialKeys.FCM_TOKEN_SYNCED, isSynced).apply()
        return true
    }

    override fun setLastTokenSyncTime(timeMillis: Long): Boolean {
        getAppPreferences().edit()
            .putLong(AppEssentialKeys.LAST_FCM_TOKEN_SYNC_TIME, timeMillis).apply()
        return true
    }

    override fun setDeepLinkKey(keyType: String, key: String) {
        getAppPreferences().edit()
            .putString(UserPreferenceKeys.DEEPLINK_KEY_TYPE, keyType)
            .putString(UserPreferenceKeys.DEEPLINK_KEY_VALUE, key)
            .apply()
    }

    override fun setInstallReferrerFetched(fetched: Boolean) {
        getAppPreferences().edit()
            .putBoolean(UserPreferenceKeys.INSTALL_REFERRAL_FETCHED, fetched)
            .apply()
    }

    override fun setFeedUserGuideShown(shown: Boolean) {
        getAppPreferences().edit()
            .putBoolean(UserPreferenceKeys.FEED_USER_GUIDE_SHOWN, shown)
            .apply()
    }

    override fun setFeedWishlistGuideShown(shown: Boolean) {
        getAppPreferences().edit()
            .putBoolean(UserPreferenceKeys.WISHLIST_GUIDE_SHOWN, shown)
            .apply()
    }

    override fun logout() {
        setDeviceToken("")
        /*setFcmToken("")
        setFcmTokenSynced(false)
        setLastTokenSyncTime(-1)*/
    }

    private fun getAppPreferences(): SharedPreferences {
        return preferences
    }

    companion object {
        @Volatile private var INSTANCE: PersistentStore? = null

        @Synchronized
        fun getInstance(application: Context): PersistentStore =
            INSTANCE ?: synchronized(this) { INSTANCE ?: createSecureInstance(application) }

        private fun createInstance(application: Context): PersistentStore {
            return DefaultPersistentStore(
                application.getSharedPreferences(
                    APP_PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )
            )
                .also { INSTANCE = it }
        }

        private fun createSecureInstance(application: Context): PersistentStore {
            val masterKey = MasterKey.Builder(application,
                SECURED_KEY_ALIAS
            )
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return DefaultPersistentStore(
                EncryptedSharedPreferences(
                    context = application,
                    "best_deals_secured_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            )
        }

        private const val APP_PREFERENCES_NAME = "best_deals_preferences"
        private const val SECURED_KEY_ALIAS = "best_deals_secured_store"

        private const val DEFAULT_USER_PREFERRED_THEME = 0

        object UserPreferenceKeys {
            const val DEVICE_TOKEN: String = "device_token"
            const val USER_ID: String = "user_id"
            const val TEMP_ID: String = "temp_id"
            const val USERNAME: String = "username"
            const val GUEST_USER_ID = "guest_user_id"
            const val PROCESSING_MODEL = "processing_model"
            const val UPLOAD_STEP_SKIPPED: String = "upload_step_skipped"
            const val USER_PREFERRED_THEME = "user_preferred_theme"
            const val DEEPLINK_KEY_TYPE = "deep_link_key_type"
            const val DEEPLINK_KEY_VALUE = "deep_link_key_value"
            const val LEGAL_AGREEMENT = "legal_agreement"
            const val LANDING_USER_GUIDE_SHOWN = "landing_user_guide_shown"
            const val HOME_USER_GUIDE_SHOWN = "home_user_guide_shown"
            const val PROFILE_CREATE_GUIDE_COUNT = "profile_create_guide_count"
            const val APP_RATING_SHOWN = "app_rating_shown"
            const val USER_IGNORED_THE_APP_UPDATE_POPUP = "app_update_popup_ignored"
            const val LAST_UPDATE_POPUP_SHOWN_TIME = "last_app_update_popup_shown_time"
            const val INSTALL_REFERRAL_FETCHED = "install_referrer_fetched"
            const val FEED_USER_GUIDE_SHOWN = "feed_user_guide_shown"
            const val WISHLIST_GUIDE_SHOWN = "feed_wishlist_guide_shown"
        }

        object AppEssentialKeys {
            const val ONBOARD_PRESENTED: String = "onboard_presented"
            const val DEVICE_INSTANCE_ID: String = "device_instance_id"
            const val FCM_TOKEN: String = "fcm_token"
            const val LAST_FCM_TOKEN_SYNC_TIME: String = "last_fcm_token_sync_time"
            const val FCM_TOKEN_SYNCED: String = "fcm_token_synced"
        }
    }

}