package com.ians.halerchat.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ians.halerchat.core.model.User
import com.ians.halerchat.core.network.auth.AuthTokens
import com.ians.halerchat.core.network.auth.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal const val SESSION_FILE = "session.preferences_pb"

internal fun createSessionDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })

// TODO: шифрование refresh токена
internal class SessionStore(private val dataStore: DataStore<Preferences>) : TokenStorage {
    val session: Flow<Session?> = dataStore.data.map { prefs ->
        Session(
            accessToken = prefs[ACCESS_TOKEN] ?: return@map null,
            refreshToken = prefs[REFRESH_TOKEN] ?: return@map null,
            user = User(
                id = prefs[USER_ID] ?: return@map null,
                email = prefs[USER_EMAIL] ?: return@map null,
                displayName = prefs[USER_NAME] ?: return@map null
            )
        )
    }

    suspend fun save(session: Session) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = session.accessToken
            prefs[REFRESH_TOKEN] = session.refreshToken
            prefs[USER_ID] = session.user.id
            prefs[USER_EMAIL] = session.user.email
            prefs[USER_NAME] = session.user.displayName
        }
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun loadTokens(): AuthTokens? =
        session.first()?.let {
            AuthTokens(
                accessToken = it.accessToken,
                refreshToken = it.refreshToken
            )
        }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_display_name")
    }
}