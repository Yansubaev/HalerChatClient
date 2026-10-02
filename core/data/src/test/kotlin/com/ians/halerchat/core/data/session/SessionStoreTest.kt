package com.ians.halerchat.core.data.session

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ians.halerchat.core.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SessionStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun createStore() = SessionStore(
        PreferenceDataStoreFactory.create(
            produceFile = { File(tmp.root, "test.preferences_pb") }
        )
    )

    private val session = Session(
        accessToken = "access_token",
        refreshToken = "refresh_token",
        user = User(
            id = "id123",
            email = "a@a.a",
            displayName = "Alice"
        )
    )

    @Test
    fun save_thenSessionEmitsSaved() = runTest {
        val store = createStore()

        store.save(session)

        assertEquals(session, store.session.first())
    }

    @Test
    fun clear_afterSave_thenSessionEmitsNull() = runTest {
        val store = createStore()

        store.save(session)

        assertNotNull(store.session.first())

        store.clear()

        assertNull(store.session.first())
    }

    @Test
    fun emptyStore_sessionIsNull() = runTest {
        val store = createStore()

        assertNull(store.session.first())
    }
}