package com.ians.halerchat.feature.rooms

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ians.halerchat.core.data.room.RoomsResult
import com.ians.halerchat.core.designsystem.theme.HalerChatTheme
import com.ians.halerchat.core.model.Room
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun RoomsRoute(
    onLogout: () -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    viewModel: RoomsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RoomsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onLogout = onLogout,
        onCreateRoom = onCreateRoom,
        onJoinRoom = onJoinRoom,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomsScreen(
    state: RoomsState,
    onIntent: (RoomsIntent) -> Unit,
    onLogout: () -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rooms_title)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            painter = painterResource(R.drawable.ic_logout),
                            contentDescription = stringResource(R.string.rooms_cd_logout)
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)

        when {
            state.isLoading && state.rooms.isEmpty() -> RoomsLoading(modifier)

            state.error != null && state.rooms.isEmpty() -> RoomsError(
                modifier = modifier,
                onRetry = {
                    onIntent(RoomsIntent.Retry)
                })

            state.rooms.isEmpty() -> RoomsEmpty(
                modifier = modifier,
                onCreateRoom = onCreateRoom,
                onJoinRoom = onJoinRoom,
            )

            else -> RoomsContent(state.rooms, modifier)
        }

    }
}

@Preview(showBackground = true)
@Composable
internal fun RoomsScreenPreview() {
    HalerChatTheme {
        RoomsScreen(
            state = RoomsState(
                rooms = listOf(
                    Room(
                        id = "1",
                        name = "First Room",
                        description = "Room 1 description",
                        ownerId = "me",
                        ownerName = "Denis",
                        createdAt = "Today",
                        memberCount = 2,
                        visibility = "public",
                        inviteToken = null,
                        accessCode = null
                    ),
                    Room(
                        id = "2",
                        name = "Second Room with a long ass name that cannot fit into one line",
                        description = null,
                        ownerId = "me",
                        ownerName = "Denis",
                        createdAt = "Today",
                        memberCount = 2,
                        visibility = "private",
                        inviteToken = null,
                        accessCode = null
                    )
                )
            ),
            onIntent = {},
            onLogout = {},
            onCreateRoom = {},
            onJoinRoom = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun RoomsScreenPreview_Loading() {
    HalerChatTheme {
        RoomsScreen(
            state = RoomsState(isLoading = true),
            onIntent = {},
            onLogout = {},
            onCreateRoom = {},
            onJoinRoom = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun RoomsScreenPreview_EmptyRooms() {
    HalerChatTheme {
        RoomsScreen(
            state = RoomsState(),
            onIntent = {},
            onLogout = {},
            onCreateRoom = {},
            onJoinRoom = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun RoomsScreenPreview_Error() {
    HalerChatTheme {
        RoomsScreen(
            state = RoomsState(error = RoomsResult.Unknown),
            onIntent = {},
            onLogout = {},
            onCreateRoom = {},
            onJoinRoom = {}
        )
    }
}
